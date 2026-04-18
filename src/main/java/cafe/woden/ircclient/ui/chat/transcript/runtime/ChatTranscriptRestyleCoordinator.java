package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.ui.settings.UiSettings;
import java.awt.Color;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;

final class ChatTranscriptRestyleCoordinator {

  private final int restyleElementsPerSlice;
  private final ChatTranscriptRestyleSupport.Context restyleSupportContext;
  private final Supplier<UiSettings> settingsSupplier;
  private final Function<UiSettings, Color> outgoingColorResolver;
  private final Supplier<List<StyledDocument>> documentSnapshotSupplier;

  private List<StyledDocument> restylePassDocs = List.of();
  private int restylePassDocIndex = 0;
  private int restylePassDocOffset = 0;
  private boolean restylePassRunning = false;
  private boolean restylePassRestartRequested = false;

  ChatTranscriptRestyleCoordinator(
      int restyleElementsPerSlice,
      ChatTranscriptRestyleSupport.Context restyleSupportContext,
      Supplier<UiSettings> settingsSupplier,
      Function<UiSettings, Color> outgoingColorResolver,
      Supplier<List<StyledDocument>> documentSnapshotSupplier) {
    this.restyleElementsPerSlice = restyleElementsPerSlice;
    this.restyleSupportContext = restyleSupportContext;
    this.settingsSupplier = settingsSupplier;
    this.outgoingColorResolver = outgoingColorResolver;
    this.documentSnapshotSupplier = documentSnapshotSupplier;
  }

  synchronized void restyleAllDocuments() {
    UiSettings settings = settingsSupplier.get();
    Color outgoingColor = outgoingColorResolver.apply(settings);
    boolean outgoingColorEnabled = outgoingColor != null;
    for (StyledDocument doc : documentSnapshotSupplier.get()) {
      ChatTranscriptRestyleSupport.restyleDocument(
          restyleSupportContext, doc, outgoingColorEnabled, outgoingColor);
    }
  }

  void restyleAllDocumentsCoalesced() {
    boolean schedule = false;
    synchronized (this) {
      if (restylePassRunning) {
        restylePassRestartRequested = true;
      } else {
        restylePassRunning = true;
        resetRestylePassLocked();
        schedule = true;
      }
    }
    if (schedule) {
      SwingUtilities.invokeLater(this::runRestylePassSliceSafely);
    }
  }

  private void resetRestylePassLocked() {
    restylePassDocs = documentSnapshotSupplier.get();
    restylePassDocIndex = 0;
    restylePassDocOffset = 0;
  }

  private void clearRestylePassLocked() {
    restylePassRunning = false;
    restylePassRestartRequested = false;
    restylePassDocs = List.of();
    restylePassDocIndex = 0;
    restylePassDocOffset = 0;
  }

  private void runRestylePassSliceSafely() {
    try {
      runRestylePassSlice();
    } catch (Exception ignored) {
      synchronized (this) {
        clearRestylePassLocked();
      }
    }
  }

  private void runRestylePassSlice() {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(this::runRestylePassSliceSafely);
      return;
    }

    UiSettings settings = settingsSupplier.get();
    Color outgoingColor = outgoingColorResolver.apply(settings);
    boolean outgoingColorEnabled = outgoingColor != null;

    boolean scheduleNext = false;
    synchronized (this) {
      if (!restylePassRunning) return;

      if (restylePassRestartRequested) {
        restylePassRestartRequested = false;
        resetRestylePassLocked();
      }

      int budget = restyleElementsPerSlice;
      while (budget > 0 && restylePassDocIndex < restylePassDocs.size()) {
        StyledDocument doc = restylePassDocs.get(restylePassDocIndex);
        int currentOffset = restylePassDocOffset;
        ChatTranscriptRestyleSupport.SliceOutcome outcome =
            ChatTranscriptRestyleSupport.restyleDocumentSlice(
                restyleSupportContext,
                doc,
                currentOffset,
                budget,
                outgoingColorEnabled,
                outgoingColor);
        if (outcome.done() || outcome.nextOffset() <= currentOffset) {
          restylePassDocIndex++;
          restylePassDocOffset = 0;
        } else {
          restylePassDocOffset = outcome.nextOffset();
        }
        budget -= Math.max(1, outcome.processedElements());
      }

      if (restylePassDocIndex >= restylePassDocs.size()) {
        if (restylePassRestartRequested) {
          restylePassRestartRequested = false;
          resetRestylePassLocked();
          scheduleNext = true;
        } else {
          clearRestylePassLocked();
        }
      } else {
        scheduleNext = true;
      }
    }

    if (scheduleNext) {
      SwingUtilities.invokeLater(this::runRestylePassSliceSafely);
    }
  }
}

package cafe.woden.ircclient.ui.chat.transcript.filter;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.FilteredFoldComponent;
import cafe.woden.ircclient.ui.chat.fold.FilteredHintComponent;
import cafe.woden.ircclient.ui.chat.fold.FilteredLineComponent;
import cafe.woden.ircclient.ui.chat.fold.FilteredOverflowComponent;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.awt.Font;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptFilteredLinesSupport {

  @FunctionalInterface
  public interface PresenceRunBreakHandler {
    void breakRun(TargetRef ref);
  }

  @FunctionalInterface
  public interface PresenceBlockShiftHandler {
    void shift(TargetRef ref, int insertAt, int delta);
  }

  @FunctionalInterface
  public interface TranscriptLineCapEnforcer {
    int enforce(TargetRef ref, StyledDocument doc);
  }

  public static final class State {
    private FilteredRun currentFilteredRun;
    private FilteredHintRun currentFilteredHintRun;
    private FilteredRun currentFilteredRunInsert;
    private FilteredHintRun currentFilteredHintRunInsert;
    private boolean historyInsertBatchActive;
    private int historyInsertPlaceholderRunsCreated;
    private int historyInsertHintRunsCreated;
    private FilteredOverflowRun historyInsertOverflowRun;
    private boolean forceDeferRichTextDuringHistoryBatch;

    void endAppendRun() {
      currentFilteredRun = null;
      currentFilteredHintRun = null;
    }

    void endInsertRun() {
      currentFilteredRunInsert = null;
      currentFilteredHintRunInsert = null;
    }

    void beginHistoryInsertBatch(boolean forceDeferRichText) {
      endInsertRun();
      historyInsertBatchActive = true;
      historyInsertPlaceholderRunsCreated = 0;
      historyInsertHintRunsCreated = 0;
      historyInsertOverflowRun = null;
      forceDeferRichTextDuringHistoryBatch = forceDeferRichText;
    }

    void endHistoryInsertBatch() {
      endInsertRun();
      historyInsertBatchActive = false;
      historyInsertPlaceholderRunsCreated = 0;
      historyInsertHintRunsCreated = 0;
      historyInsertOverflowRun = null;
      forceDeferRichTextDuringHistoryBatch = false;
    }

    boolean historyInsertBatchActive() {
      return historyInsertBatchActive;
    }

    boolean forceDeferRichTextDuringHistoryBatch() {
      return forceDeferRichTextDuringHistoryBatch;
    }

    void reset() {
      endAppendRun();
      endHistoryInsertBatch();
    }
  }

  /**
   * Common base for filtered-line run trackers. Each run tracks a contiguous group of hidden lines
   * that share a single visible placeholder, hint, or overflow component in the transcript.
   */
  public abstract static class AbstractFilteredRun<C extends FilteredLineComponent> {
    final Position pos;
    final C component;

    FilterEngine.Match primaryMatch;
    boolean multiple;

    LineMeta lastHiddenMeta;
    final LinkedHashSet<String> unionTags = new LinkedHashSet<>();

    protected AbstractFilteredRun(Position pos, C component) {
      this.pos = pos;
      this.component = component;
    }

    public void observe(FilterEngine.Match match, LineMeta hiddenMeta) {
      if (hiddenMeta != null) {
        lastHiddenMeta = hiddenMeta;
        try {
          unionTags.addAll(hiddenMeta.tags());
        } catch (Exception ignored) {
        }
      }

      if (match == null) {
        return;
      }
      if (primaryMatch == null) {
        primaryMatch = match;
        return;
      }

      try {
        if (primaryMatch.ruleId() != null
            && match.ruleId() != null
            && !primaryMatch.ruleId().equals(match.ruleId())) {
          multiple = true;
        }
      } catch (Exception ignored) {
        multiple = true;
      }
    }
  }

  public static final class FilteredRun extends AbstractFilteredRun<FilteredFoldComponent> {
    private FilteredRun(Position pos, FilteredFoldComponent component) {
      super(pos, component);
    }
  }

  public static final class FilteredHintRun extends AbstractFilteredRun<FilteredHintComponent> {
    private FilteredHintRun(Position pos, FilteredHintComponent component) {
      super(pos, component);
    }
  }

  public static final class FilteredOverflowRun
      extends AbstractFilteredRun<FilteredOverflowComponent> {
    private FilteredOverflowRun(Position pos, FilteredOverflowComponent component) {
      super(pos, component);
    }
  }

  private final ChatStyles styles;
  private final ChatTranscriptFilteredRunSupport.Context filteredRunSupportContext;
  private final Supplier<Font> transcriptFontSupplier;
  private final BiFunction<AttributeSet, LineMeta, SimpleAttributeSet> withLineMeta;
  private final Consumer<StyledDocument> ensureAtLineStart;
  private final BiFunction<StyledDocument, Integer, Integer> normalizeInsertAtLineStart;
  private final BiFunction<StyledDocument, Integer, Integer> ensureAtLineStartForInsert;
  private final PresenceRunBreakHandler breakPresenceRun;
  private final PresenceBlockShiftHandler shiftPresenceBlock;
  private final TranscriptLineCapEnforcer enforceTranscriptLineCap;

  public ChatTranscriptFilteredLinesSupport(
      ChatStyles styles,
      ChatTranscriptFilteredRunSupport.Context filteredRunSupportContext,
      Supplier<Font> transcriptFontSupplier,
      BiFunction<AttributeSet, LineMeta, SimpleAttributeSet> withLineMeta,
      Consumer<StyledDocument> ensureAtLineStart,
      BiFunction<StyledDocument, Integer, Integer> normalizeInsertAtLineStart,
      BiFunction<StyledDocument, Integer, Integer> ensureAtLineStartForInsert,
      PresenceRunBreakHandler breakPresenceRun,
      PresenceBlockShiftHandler shiftPresenceBlock,
      TranscriptLineCapEnforcer enforceTranscriptLineCap) {
    this.styles = Objects.requireNonNull(styles, "styles");
    this.filteredRunSupportContext =
        Objects.requireNonNull(filteredRunSupportContext, "filteredRunSupportContext");
    this.transcriptFontSupplier =
        Objects.requireNonNull(transcriptFontSupplier, "transcriptFontSupplier");
    this.withLineMeta = Objects.requireNonNull(withLineMeta, "withLineMeta");
    this.ensureAtLineStart = Objects.requireNonNull(ensureAtLineStart, "ensureAtLineStart");
    this.normalizeInsertAtLineStart =
        Objects.requireNonNull(normalizeInsertAtLineStart, "normalizeInsertAtLineStart");
    this.ensureAtLineStartForInsert =
        Objects.requireNonNull(ensureAtLineStartForInsert, "ensureAtLineStartForInsert");
    this.breakPresenceRun = Objects.requireNonNull(breakPresenceRun, "breakPresenceRun");
    this.shiftPresenceBlock = Objects.requireNonNull(shiftPresenceBlock, "shiftPresenceBlock");
    this.enforceTranscriptLineCap =
        Objects.requireNonNull(enforceTranscriptLineCap, "enforceTranscriptLineCap");
  }

  public void endAppendRun(State state) {
    if (state != null) {
      state.endAppendRun();
    }
  }

  public void endInsertRun(State state) {
    if (state != null) {
      state.endInsertRun();
    }
  }

  public void beginHistoryInsertBatch(State state, boolean forceDeferRichText) {
    if (state != null) {
      state.beginHistoryInsertBatch(forceDeferRichText);
    }
  }

  public void endHistoryInsertBatch(State state) {
    if (state != null) {
      state.endHistoryInsertBatch();
    }
  }

  public boolean historyInsertBatchActive(State state) {
    return state != null && state.historyInsertBatchActive();
  }

  public boolean forceDeferRichTextDuringHistoryBatch(State state) {
    return state != null && state.forceDeferRichTextDuringHistoryBatch();
  }

  public void reset(State state) {
    if (state != null) {
      state.reset();
    }
  }

  public void onFilteredLineAppend(
      TargetRef ref,
      StyledDocument doc,
      State state,
      FilterEngine.Effective effective,
      String previewText,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    if (ref == null || doc == null || state == null || effective == null) {
      return;
    }
    if (match == null || !match.isHide()) {
      return;
    }

    if (!effective.placeholdersEnabled()) {
      onFilteredLineHintAppend(ref, doc, state, effective, hiddenMeta, match);
      return;
    }

    FilteredRun run = state.currentFilteredRun;
    FilteredFoldComponent component = (run != null) ? run.component : null;

    int maxRun = Math.max(0, effective.placeholderMaxLinesPerRun());
    if (component != null && maxRun > 0 && component.count() >= maxRun) {
      state.currentFilteredRun = null;
      run = null;
      component = null;
    }

    if (component == null) {
      breakPresenceRun.breakRun(ref);
      ensureAtLineStart.accept(doc);

      component =
          createFoldComponent(
              effective.placeholdersCollapsed(),
              effective.placeholderMaxPreviewLines(),
              effective.placeholderTooltipMaxTags());

      long tsEpochMs = effectiveEpochMs(hiddenMeta);
      LineMeta meta =
          ChatTranscriptFilteredRunSupport.buildFilteredMeta(
              hiddenMeta, tsEpochMs, false, hiddenMeta != null ? hiddenMeta.tags() : null);

      try {
        int insertAt = doc.getLength();
        SimpleAttributeSet attrs = withLineMeta.apply(styles.status(), meta);
        attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_STATUS);
        ChatTranscriptFilteredRunSupport.attachFilterMatch(attrs, match, false);
        StyleConstants.setComponent(attrs, component);

        doc.insertString(insertAt, " ", attrs);
        doc.insertString(insertAt + 1, "\n", withLineMeta.apply(styles.timestamp(), meta));

        run = new FilteredRun(doc.createPosition(insertAt), component);
        state.currentFilteredRun = run;
      } catch (Exception ignored) {
        state.currentFilteredRun = new FilteredRun(null, component);
        run = state.currentFilteredRun;
      }
    }

    if (run != null) {
      run.observe(match, hiddenMeta);
      ChatTranscriptFilteredRunSupport.updateFilteredRunAttributes(
          filteredRunSupportContext, doc, run, false);
    }

    try {
      component.addFilteredLine(previewText);
    } catch (Exception ignored) {
    }

    enforceTranscriptLineCap.enforce(ref, doc);
  }

  public int onFilteredLineInsertAt(
      TargetRef ref,
      StyledDocument doc,
      State state,
      FilterEngine.Effective effective,
      int insertAt,
      String previewText,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    if (ref == null || doc == null || state == null || effective == null) {
      return Math.max(0, insertAt);
    }
    if (match == null || !match.isHide()) {
      return Math.max(0, insertAt);
    }
    if (!effective.historyPlaceholdersEnabled()) {
      return Math.max(0, insertAt);
    }
    if (!effective.placeholdersEnabled()) {
      return onFilteredLineHintInsertAt(ref, doc, state, effective, insertAt, hiddenMeta, match);
    }

    FilteredRun run = state.currentFilteredRunInsert;
    FilteredFoldComponent component = (run != null) ? run.component : null;

    int maxRun = Math.max(0, effective.placeholderMaxLinesPerRun());
    if (component != null && maxRun > 0 && component.count() >= maxRun) {
      state.currentFilteredRunInsert = null;
      run = null;
      component = null;
    }

    if (component == null) {
      int maxBatchRuns = Math.max(0, effective.historyPlaceholderMaxRunsPerBatch());
      if (state.historyInsertBatchActive
          && maxBatchRuns > 0
          && state.historyInsertPlaceholderRunsCreated >= maxBatchRuns) {
        return onFilteredOverflowInsertAt(ref, doc, state, effective, insertAt, hiddenMeta, match);
      }

      int beforeLen = doc.getLength();
      int pos = normalizeInsertAtLineStart.apply(doc, insertAt);
      pos = ensureAtLineStartForInsert.apply(doc, pos);
      int insertionStart = pos;

      component =
          createFoldComponent(
              effective.placeholdersCollapsed(),
              effective.placeholderMaxPreviewLines(),
              effective.placeholderTooltipMaxTags());

      long tsEpochMs = effectiveEpochMs(hiddenMeta);
      LineMeta meta =
          ChatTranscriptFilteredRunSupport.buildFilteredMeta(
              hiddenMeta, tsEpochMs, false, hiddenMeta != null ? hiddenMeta.tags() : null);

      try {
        SimpleAttributeSet attrs = withLineMeta.apply(styles.status(), meta);
        attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_STATUS);
        ChatTranscriptFilteredRunSupport.attachFilterMatch(attrs, match, false);
        StyleConstants.setComponent(attrs, component);

        doc.insertString(pos, " ", attrs);
        doc.insertString(pos + 1, "\n", withLineMeta.apply(styles.timestamp(), meta));

        run = new FilteredRun(doc.createPosition(pos), component);
        state.currentFilteredRunInsert = run;
        if (state.historyInsertBatchActive) {
          state.historyInsertPlaceholderRunsCreated++;
        }
      } catch (Exception ignored) {
        state.currentFilteredRunInsert = new FilteredRun(null, component);
        run = state.currentFilteredRunInsert;
        if (state.historyInsertBatchActive) {
          state.historyInsertPlaceholderRunsCreated++;
        }
      }

      int delta = doc.getLength() - beforeLen;
      shiftPresenceBlock.shift(ref, insertionStart, delta);
      insertAt = insertionStart + delta;
    }

    if (run != null) {
      run.observe(match, hiddenMeta);
      ChatTranscriptFilteredRunSupport.updateFilteredRunAttributes(
          filteredRunSupportContext, doc, run, false);
    }

    try {
      component.addFilteredLine(previewText);
    } catch (Exception ignored) {
    }

    int trimmed = enforceTranscriptLineCap.enforce(ref, doc);
    if (trimmed > 0) {
      insertAt = Math.max(0, insertAt - trimmed);
    }
    return Math.max(0, insertAt);
  }

  private void onFilteredLineHintAppend(
      TargetRef ref,
      StyledDocument doc,
      State state,
      FilterEngine.Effective effective,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    FilteredHintRun run = state.currentFilteredHintRun;
    FilteredHintComponent component = (run != null) ? run.component : null;

    if (component == null) {
      breakPresenceRun.breakRun(ref);
      ensureAtLineStart.accept(doc);

      component = createHintComponent(effective.placeholderTooltipMaxTags());

      long tsEpochMs = effectiveEpochMs(hiddenMeta);
      LineMeta meta =
          ChatTranscriptFilteredRunSupport.buildFilteredMeta(
              hiddenMeta, tsEpochMs, true, hiddenMeta != null ? hiddenMeta.tags() : null);

      try {
        int insertAt = doc.getLength();
        SimpleAttributeSet attrs = withLineMeta.apply(styles.status(), meta);
        attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_STATUS);
        ChatTranscriptFilteredRunSupport.attachFilterMatch(attrs, match, false);
        StyleConstants.setComponent(attrs, component);

        doc.insertString(insertAt, " ", attrs);
        doc.insertString(insertAt + 1, "\n", withLineMeta.apply(styles.timestamp(), meta));

        run = new FilteredHintRun(doc.createPosition(insertAt), component);
        state.currentFilteredHintRun = run;
      } catch (Exception ignored) {
        state.currentFilteredHintRun = new FilteredHintRun(null, component);
        run = state.currentFilteredHintRun;
      }
    }

    if (run != null) {
      run.observe(match, hiddenMeta);
      ChatTranscriptFilteredRunSupport.updateFilteredRunAttributes(
          filteredRunSupportContext, doc, run, true);
    }

    try {
      component.addFilteredLine();
    } catch (Exception ignored) {
    }

    enforceTranscriptLineCap.enforce(ref, doc);
  }

  private int onFilteredLineHintInsertAt(
      TargetRef ref,
      StyledDocument doc,
      State state,
      FilterEngine.Effective effective,
      int insertAt,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    FilteredHintRun run = state.currentFilteredHintRunInsert;
    FilteredHintComponent component = (run != null) ? run.component : null;

    if (component == null) {
      int maxBatchRuns = Math.max(0, effective.historyPlaceholderMaxRunsPerBatch());
      if (state.historyInsertBatchActive
          && maxBatchRuns > 0
          && state.historyInsertHintRunsCreated >= maxBatchRuns) {
        return onFilteredOverflowInsertAt(ref, doc, state, effective, insertAt, hiddenMeta, match);
      }

      int beforeLen = doc.getLength();
      int pos = normalizeInsertAtLineStart.apply(doc, insertAt);
      pos = ensureAtLineStartForInsert.apply(doc, pos);
      int insertionStart = pos;

      component = createHintComponent(effective.placeholderTooltipMaxTags());

      long tsEpochMs = effectiveEpochMs(hiddenMeta);
      LineMeta meta =
          ChatTranscriptFilteredRunSupport.buildFilteredMeta(
              hiddenMeta, tsEpochMs, true, hiddenMeta != null ? hiddenMeta.tags() : null);

      try {
        SimpleAttributeSet attrs = withLineMeta.apply(styles.status(), meta);
        attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_STATUS);
        ChatTranscriptFilteredRunSupport.attachFilterMatch(attrs, match, false);
        StyleConstants.setComponent(attrs, component);

        doc.insertString(pos, " ", attrs);
        doc.insertString(pos + 1, "\n", withLineMeta.apply(styles.timestamp(), meta));

        run = new FilteredHintRun(doc.createPosition(pos), component);
        state.currentFilteredHintRunInsert = run;
        if (state.historyInsertBatchActive) {
          state.historyInsertHintRunsCreated++;
        }
      } catch (Exception ignored) {
        state.currentFilteredHintRunInsert = new FilteredHintRun(null, component);
        run = state.currentFilteredHintRunInsert;
        if (state.historyInsertBatchActive) {
          state.historyInsertHintRunsCreated++;
        }
      }

      int delta = doc.getLength() - beforeLen;
      shiftPresenceBlock.shift(ref, insertionStart, delta);
      insertAt = insertionStart + delta;
    }

    if (run != null) {
      run.observe(match, hiddenMeta);
      ChatTranscriptFilteredRunSupport.updateFilteredRunAttributes(
          filteredRunSupportContext, doc, run, true);
    }

    try {
      component.addFilteredLine();
    } catch (Exception ignored) {
    }

    int trimmed = enforceTranscriptLineCap.enforce(ref, doc);
    if (trimmed > 0) {
      insertAt = Math.max(0, insertAt - trimmed);
    }
    return Math.max(0, insertAt);
  }

  private int onFilteredOverflowInsertAt(
      TargetRef ref,
      StyledDocument doc,
      State state,
      FilterEngine.Effective effective,
      int insertAt,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    FilteredOverflowRun run = state.historyInsertOverflowRun;
    FilteredOverflowComponent component = (run != null) ? run.component : null;

    if (component == null) {
      int beforeLen = doc.getLength();
      int pos = normalizeInsertAtLineStart.apply(doc, insertAt);
      pos = ensureAtLineStartForInsert.apply(doc, pos);
      int insertionStart = pos;

      component = createOverflowComponent(effective.placeholderTooltipMaxTags());

      long tsEpochMs = effectiveEpochMs(hiddenMeta);
      LineMeta meta =
          ChatTranscriptFilteredRunSupport.buildFilteredOverflowMeta(
              hiddenMeta, tsEpochMs, hiddenMeta != null ? hiddenMeta.tags() : null);

      try {
        SimpleAttributeSet attrs = withLineMeta.apply(styles.status(), meta);
        attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_STATUS);
        ChatTranscriptFilteredRunSupport.attachFilterMatch(attrs, match, false);
        StyleConstants.setComponent(attrs, component);

        doc.insertString(pos, " ", attrs);
        doc.insertString(pos + 1, "\n", withLineMeta.apply(styles.timestamp(), meta));

        run = new FilteredOverflowRun(doc.createPosition(pos), component);
        state.historyInsertOverflowRun = run;
      } catch (Exception ignored) {
        run = new FilteredOverflowRun(null, component);
        state.historyInsertOverflowRun = run;
      }

      int delta = doc.getLength() - beforeLen;
      shiftPresenceBlock.shift(ref, insertionStart, delta);
      insertAt = insertionStart + delta;
    }

    if (run != null) {
      run.observe(match, hiddenMeta);
      ChatTranscriptFilteredRunSupport.updateFilteredOverflowRunAttributes(
          filteredRunSupportContext, doc, run);
    }

    try {
      component.addFilteredLine();
    } catch (Exception ignored) {
    }

    int trimmed = enforceTranscriptLineCap.enforce(ref, doc);
    if (trimmed > 0) {
      insertAt = Math.max(0, insertAt - trimmed);
    }
    return Math.max(0, insertAt);
  }

  private FilteredFoldComponent createFoldComponent(
      boolean collapsed, int maxPreviewLines, int tooltipMaxTags) {
    FilteredFoldComponent component = new FilteredFoldComponent(collapsed, maxPreviewLines);
    try {
      component.setMaxTagsInTooltip(tooltipMaxTags);
    } catch (Exception ignored) {
    }
    applyTranscriptFont(component);
    return component;
  }

  private FilteredHintComponent createHintComponent(int tooltipMaxTags) {
    FilteredHintComponent component = new FilteredHintComponent();
    try {
      component.setMaxTagsInTooltip(tooltipMaxTags);
    } catch (Exception ignored) {
    }
    applyTranscriptFont(component);
    return component;
  }

  private FilteredOverflowComponent createOverflowComponent(int tooltipMaxTags) {
    FilteredOverflowComponent component = new FilteredOverflowComponent();
    try {
      component.setMaxTagsInTooltip(tooltipMaxTags);
    } catch (Exception ignored) {
    }
    applyTranscriptFont(component);
    return component;
  }

  private void applyTranscriptFont(FilteredLineComponent component) {
    if (component == null) {
      return;
    }
    Font font = transcriptFontSupplier.get();
    if (font == null) {
      return;
    }
    if (component instanceof FilteredFoldComponent foldComponent) {
      foldComponent.setTranscriptFont(font);
    } else if (component instanceof FilteredHintComponent hintComponent) {
      hintComponent.setTranscriptFont(font);
    } else if (component instanceof FilteredOverflowComponent overflowComponent) {
      overflowComponent.setTranscriptFont(font);
    }
  }

  private static long effectiveEpochMs(LineMeta hiddenMeta) {
    return (hiddenMeta != null && hiddenMeta.epochMs() != null && hiddenMeta.epochMs() > 0)
        ? hiddenMeta.epochMs()
        : System.currentTimeMillis();
  }
}

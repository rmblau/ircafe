package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.config.api.UiSettingsRuntimeConfigPort;
import cafe.woden.ircclient.logging.history.ChatHistoryTranscriptPort;
import cafe.woden.ircclient.logging.history.LoadOlderControlState;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.BooleanSupplier;
import javax.swing.text.StyledDocument;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SecondaryAdapter
@InterfaceLayer
@Lazy
public class ChatHistoryTranscriptPortAdapter implements ChatHistoryTranscriptPort {

  private final ChatHistoryTranscriptAdapters adapters;

  public ChatHistoryTranscriptPortAdapter(
      ChatTranscriptStore transcripts,
      UiSettingsBus settingsBus,
      UiSettingsRuntimeConfigPort runtimeConfig) {
    this.adapters = ChatHistoryTranscriptAdapters.create(transcripts, settingsBus, runtimeConfig);
  }

  @Override
  public StyledDocument document(TargetRef ref) {
    return adapters.document().document(ref);
  }

  @Override
  public OptionalLong earliestTimestampEpochMs(TargetRef ref) {
    return adapters.document().earliestTimestampEpochMs(ref);
  }

  @Override
  public java.awt.Component ensureLoadOlderMessagesControl(TargetRef ref) {
    return adapters.loadOlderControls().ensure(ref);
  }

  @Override
  public void setLoadOlderMessagesControlState(TargetRef ref, LoadOlderControlState state) {
    adapters.loadOlderControls().setState(ref, state);
  }

  @Override
  public void setLoadOlderMessagesControlHandler(TargetRef ref, BooleanSupplier onLoad) {
    adapters.loadOlderControls().setHandler(ref, onLoad);
  }

  @Override
  public void beginHistoryInsertBatch(TargetRef ref) {
    adapters.batch().begin(ref);
  }

  @Override
  public void beginHistoryInsertBatch(TargetRef ref, boolean forceDeferRichText) {
    adapters.batch().begin(ref, forceDeferRichText);
  }

  @Override
  public void endHistoryInsertBatch(TargetRef ref) {
    adapters.batch().end(ref);
  }

  @Override
  public int loadOlderInsertOffset(TargetRef ref) {
    return adapters.batch().loadOlderInsertOffset(ref);
  }

  @Override
  public boolean hasContentAfterOffset(TargetRef ref, int offset) {
    return adapters.batch().hasContentAfterOffset(ref, offset);
  }

  @Override
  public void ensureHistoryDivider(TargetRef ref, int insertAt, String labelText) {
    adapters.batch().ensureHistoryDivider(ref, insertAt, labelText);
  }

  @Override
  public void markHistoryDividerPending(TargetRef ref, String labelText) {
    adapters.batch().markHistoryDividerPending(ref, labelText);
  }

  @Override
  public int insertChatFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs) {
    return adapters.insert().insertChat(ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs);
  }

  @Override
  public int insertChatFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    return adapters.insert().insertChat(
        ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public int insertActionFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs) {
    return adapters.insert().insertAction(ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs);
  }

  @Override
  public int insertActionFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    return adapters.insert().insertAction(
        ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public int insertNoticeFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return adapters.insert().insertNotice(ref, insertAt, from, text, tsEpochMs);
  }

  @Override
  public int insertNoticeFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    return adapters.insert().insertNotice(
        ref, insertAt, from, text, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public int insertStatusFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return adapters.insert().insertStatus(ref, insertAt, from, text, tsEpochMs);
  }

  @Override
  public int insertErrorFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return adapters.insert().insertError(ref, insertAt, from, text, tsEpochMs);
  }

  @Override
  public int insertPresenceFromHistoryAt(TargetRef ref, int insertAt, String text, long tsEpochMs) {
    return adapters.insert().insertPresence(ref, insertAt, text, tsEpochMs);
  }

  @Override
  public int insertSpoilerChatFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return adapters.insert().insertSpoilerChat(ref, insertAt, from, text, tsEpochMs);
  }

  @Override
  public void appendChatFromHistory(
      TargetRef ref, String from, String text, boolean outgoingLocalEcho, long tsEpochMs) {
    adapters.append().appendChat(ref, from, text, outgoingLocalEcho, tsEpochMs);
  }

  @Override
  public void appendChatFromHistory(
      TargetRef ref,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    adapters.append().appendChat(
        ref, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public void appendActionFromHistory(
      TargetRef ref, String from, String text, boolean outgoingLocalEcho, long tsEpochMs) {
    adapters.append().appendAction(ref, from, text, outgoingLocalEcho, tsEpochMs);
  }

  @Override
  public void appendActionFromHistory(
      TargetRef ref,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    adapters.append().appendAction(
        ref, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public void appendNoticeFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    adapters.append().appendNotice(ref, from, text, tsEpochMs);
  }

  @Override
  public void appendNoticeFromHistory(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    adapters.append().appendNotice(ref, from, text, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public void appendStatusFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    adapters.append().appendStatus(ref, from, text, tsEpochMs);
  }

  @Override
  public void appendErrorFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    adapters.append().appendError(ref, from, text, tsEpochMs);
  }

  @Override
  public void appendPresenceFromHistory(TargetRef ref, String text, long tsEpochMs) {
    adapters.append().appendPresence(ref, text, tsEpochMs);
  }

  @Override
  public void appendSpoilerChatFromHistory(
      TargetRef ref, String from, String text, long tsEpochMs) {
    adapters.append().appendSpoilerChat(ref, from, text, tsEpochMs);
  }

  @Override
  public int chatHistoryInitialLoadLines() {
    return adapters.settings().initialLoadLines();
  }

  @Override
  public int chatHistoryPageSize() {
    return adapters.settings().pageSize();
  }

  @Override
  public int chatHistoryAutoLoadWheelDebounceMs() {
    return adapters.settings().autoLoadWheelDebounceMs();
  }

  @Override
  public int chatHistoryLoadOlderChunkSize() {
    return adapters.settings().loadOlderChunkSize();
  }

  @Override
  public int chatHistoryLoadOlderChunkDelayMs() {
    return adapters.settings().loadOlderChunkDelayMs();
  }

  @Override
  public int chatHistoryLoadOlderChunkEdtBudgetMs() {
    return adapters.settings().loadOlderChunkEdtBudgetMs();
  }

  @Override
  public boolean chatHistoryLockViewportDuringLoadOlder() {
    return adapters.settings().lockViewportDuringLoadOlder();
  }

  @Override
  public int chatHistoryRemoteRequestTimeoutSeconds() {
    return adapters.settings().remoteRequestTimeoutSeconds();
  }

  @Override
  public int chatHistoryRemoteZncPlaybackTimeoutSeconds() {
    return adapters.settings().remoteZncPlaybackTimeoutSeconds();
  }

  @Override
  public int chatHistoryRemoteZncPlaybackWindowMinutes() {
    return adapters.settings().remoteZncPlaybackWindowMinutes();
  }
}

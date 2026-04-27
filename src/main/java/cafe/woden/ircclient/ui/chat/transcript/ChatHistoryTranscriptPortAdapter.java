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
    return document().document(ref);
  }

  @Override
  public OptionalLong earliestTimestampEpochMs(TargetRef ref) {
    return document().earliestTimestampEpochMs(ref);
  }

  @Override
  public java.awt.Component ensureLoadOlderMessagesControl(TargetRef ref) {
    return loadOlderControls().ensure(ref);
  }

  @Override
  public void setLoadOlderMessagesControlState(TargetRef ref, LoadOlderControlState state) {
    loadOlderControls().setState(ref, state);
  }

  @Override
  public void setLoadOlderMessagesControlHandler(TargetRef ref, BooleanSupplier onLoad) {
    loadOlderControls().setHandler(ref, onLoad);
  }

  @Override
  public void beginHistoryInsertBatch(TargetRef ref) {
    batch().begin(ref);
  }

  @Override
  public void beginHistoryInsertBatch(TargetRef ref, boolean forceDeferRichText) {
    batch().begin(ref, forceDeferRichText);
  }

  @Override
  public void endHistoryInsertBatch(TargetRef ref) {
    batch().end(ref);
  }

  @Override
  public int loadOlderInsertOffset(TargetRef ref) {
    return batch().loadOlderInsertOffset(ref);
  }

  @Override
  public boolean hasContentAfterOffset(TargetRef ref, int offset) {
    return batch().hasContentAfterOffset(ref, offset);
  }

  @Override
  public void ensureHistoryDivider(TargetRef ref, int insertAt, String labelText) {
    batch().ensureHistoryDivider(ref, insertAt, labelText);
  }

  @Override
  public void markHistoryDividerPending(TargetRef ref, String labelText) {
    batch().markHistoryDividerPending(ref, labelText);
  }

  @Override
  public int insertChatFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs) {
    return insert().insertChat(ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs);
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
    return insert().insertChat(
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
    return insert().insertAction(ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs);
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
    return insert().insertAction(
        ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public int insertNoticeFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return insert().insertNotice(ref, insertAt, from, text, tsEpochMs);
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
    return insert().insertNotice(
        ref, insertAt, from, text, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public int insertStatusFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return insert().insertStatus(ref, insertAt, from, text, tsEpochMs);
  }

  @Override
  public int insertErrorFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return insert().insertError(ref, insertAt, from, text, tsEpochMs);
  }

  @Override
  public int insertPresenceFromHistoryAt(TargetRef ref, int insertAt, String text, long tsEpochMs) {
    return insert().insertPresence(ref, insertAt, text, tsEpochMs);
  }

  @Override
  public int insertSpoilerChatFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return insert().insertSpoilerChat(ref, insertAt, from, text, tsEpochMs);
  }

  @Override
  public void appendChatFromHistory(
      TargetRef ref, String from, String text, boolean outgoingLocalEcho, long tsEpochMs) {
    append().appendChat(ref, from, text, outgoingLocalEcho, tsEpochMs);
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
    append().appendChat(
        ref, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public void appendActionFromHistory(
      TargetRef ref, String from, String text, boolean outgoingLocalEcho, long tsEpochMs) {
    append().appendAction(ref, from, text, outgoingLocalEcho, tsEpochMs);
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
    append().appendAction(
        ref, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public void appendNoticeFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    append().appendNotice(ref, from, text, tsEpochMs);
  }

  @Override
  public void appendNoticeFromHistory(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    append().appendNotice(ref, from, text, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public void appendStatusFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    append().appendStatus(ref, from, text, tsEpochMs);
  }

  @Override
  public void appendErrorFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    append().appendError(ref, from, text, tsEpochMs);
  }

  @Override
  public void appendPresenceFromHistory(TargetRef ref, String text, long tsEpochMs) {
    append().appendPresence(ref, text, tsEpochMs);
  }

  @Override
  public void appendSpoilerChatFromHistory(
      TargetRef ref, String from, String text, long tsEpochMs) {
    append().appendSpoilerChat(ref, from, text, tsEpochMs);
  }

  @Override
  public int chatHistoryInitialLoadLines() {
    return settings().initialLoadLines();
  }

  @Override
  public int chatHistoryPageSize() {
    return settings().pageSize();
  }

  @Override
  public int chatHistoryAutoLoadWheelDebounceMs() {
    return settings().autoLoadWheelDebounceMs();
  }

  @Override
  public int chatHistoryLoadOlderChunkSize() {
    return settings().loadOlderChunkSize();
  }

  @Override
  public int chatHistoryLoadOlderChunkDelayMs() {
    return settings().loadOlderChunkDelayMs();
  }

  @Override
  public int chatHistoryLoadOlderChunkEdtBudgetMs() {
    return settings().loadOlderChunkEdtBudgetMs();
  }

  @Override
  public boolean chatHistoryLockViewportDuringLoadOlder() {
    return settings().lockViewportDuringLoadOlder();
  }

  @Override
  public int chatHistoryRemoteRequestTimeoutSeconds() {
    return settings().remoteRequestTimeoutSeconds();
  }

  @Override
  public int chatHistoryRemoteZncPlaybackTimeoutSeconds() {
    return settings().remoteZncPlaybackTimeoutSeconds();
  }

  @Override
  public int chatHistoryRemoteZncPlaybackWindowMinutes() {
    return settings().remoteZncPlaybackWindowMinutes();
  }

  private ChatHistoryTranscriptDocumentAdapter document() {
    return adapters.document();
  }

  private ChatHistoryTranscriptLoadOlderControlAdapter loadOlderControls() {
    return adapters.loadOlderControls();
  }

  private ChatHistoryTranscriptBatchAdapter batch() {
    return adapters.batch();
  }

  private ChatHistoryTranscriptSettingsReader settings() {
    return adapters.settings();
  }

  private ChatHistoryTranscriptInsertAdapter insert() {
    return adapters.messages().insert();
  }

  private ChatHistoryTranscriptAppendAdapter append() {
    return adapters.messages().append();
  }

}

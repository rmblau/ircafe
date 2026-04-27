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

  private final ChatTranscriptStore transcripts;
  private final ChatHistoryTranscriptLoadOlderControlAdapter loadOlderControls;
  private final ChatHistoryTranscriptAppendAdapter appendAdapter;
  private final ChatHistoryTranscriptSettingsReader settingsReader;

  public ChatHistoryTranscriptPortAdapter(
      ChatTranscriptStore transcripts,
      UiSettingsBus settingsBus,
      UiSettingsRuntimeConfigPort runtimeConfig) {
    this.transcripts = transcripts;
    this.loadOlderControls = new ChatHistoryTranscriptLoadOlderControlAdapter(transcripts);
    this.appendAdapter = new ChatHistoryTranscriptAppendAdapter(transcripts);
    this.settingsReader = new ChatHistoryTranscriptSettingsReader(settingsBus, runtimeConfig);
  }

  @Override
  public StyledDocument document(TargetRef ref) {
    return transcripts.document(ref);
  }

  @Override
  public OptionalLong earliestTimestampEpochMs(TargetRef ref) {
    return transcripts.earliestTimestampEpochMs(ref);
  }

  @Override
  public java.awt.Component ensureLoadOlderMessagesControl(TargetRef ref) {
    return loadOlderControls.ensure(ref);
  }

  @Override
  public void setLoadOlderMessagesControlState(TargetRef ref, LoadOlderControlState state) {
    loadOlderControls.setState(ref, state);
  }

  @Override
  public void setLoadOlderMessagesControlHandler(TargetRef ref, BooleanSupplier onLoad) {
    loadOlderControls.setHandler(ref, onLoad);
  }

  @Override
  public void beginHistoryInsertBatch(TargetRef ref) {
    transcripts.beginHistoryInsertBatch(ref);
  }

  @Override
  public void beginHistoryInsertBatch(TargetRef ref, boolean forceDeferRichText) {
    transcripts.beginHistoryInsertBatch(ref, forceDeferRichText);
  }

  @Override
  public void endHistoryInsertBatch(TargetRef ref) {
    transcripts.endHistoryInsertBatch(ref);
  }

  @Override
  public int loadOlderInsertOffset(TargetRef ref) {
    return transcripts.loadOlderInsertOffset(ref);
  }

  @Override
  public boolean hasContentAfterOffset(TargetRef ref, int offset) {
    return transcripts.hasContentAfterOffset(ref, offset);
  }

  @Override
  public void ensureHistoryDivider(TargetRef ref, int insertAt, String labelText) {
    transcripts.ensureHistoryDivider(ref, insertAt, labelText);
  }

  @Override
  public void markHistoryDividerPending(TargetRef ref, String labelText) {
    transcripts.markHistoryDividerPending(ref, labelText);
  }

  @Override
  public int insertChatFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs) {
    return transcripts.insertChatFromHistoryAt(
        ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs);
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
    return transcripts.insertChatFromHistoryAt(
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
    return transcripts.insertActionFromHistoryAt(
        ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs);
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
    return transcripts.insertActionFromHistoryAt(
        ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public int insertNoticeFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return transcripts.insertNoticeFromHistoryAt(ref, insertAt, from, text, tsEpochMs);
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
    return transcripts.insertNoticeFromHistoryAt(
        ref, insertAt, from, text, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public int insertStatusFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return transcripts.insertStatusFromHistoryAt(ref, insertAt, from, text, tsEpochMs);
  }

  @Override
  public int insertErrorFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return transcripts.insertErrorFromHistoryAt(ref, insertAt, from, text, tsEpochMs);
  }

  @Override
  public int insertPresenceFromHistoryAt(TargetRef ref, int insertAt, String text, long tsEpochMs) {
    return transcripts.insertPresenceFromHistoryAt(ref, insertAt, text, tsEpochMs);
  }

  @Override
  public int insertSpoilerChatFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return transcripts.insertSpoilerChatFromHistoryAt(ref, insertAt, from, text, tsEpochMs);
  }

  @Override
  public void appendChatFromHistory(
      TargetRef ref, String from, String text, boolean outgoingLocalEcho, long tsEpochMs) {
    appendAdapter.appendChat(ref, from, text, outgoingLocalEcho, tsEpochMs);
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
    appendAdapter.appendChat(
        ref, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public void appendActionFromHistory(
      TargetRef ref, String from, String text, boolean outgoingLocalEcho, long tsEpochMs) {
    appendAdapter.appendAction(ref, from, text, outgoingLocalEcho, tsEpochMs);
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
    appendAdapter.appendAction(
        ref, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public void appendNoticeFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    appendAdapter.appendNotice(ref, from, text, tsEpochMs);
  }

  @Override
  public void appendNoticeFromHistory(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    appendAdapter.appendNotice(ref, from, text, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public void appendStatusFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    appendAdapter.appendStatus(ref, from, text, tsEpochMs);
  }

  @Override
  public void appendErrorFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    appendAdapter.appendError(ref, from, text, tsEpochMs);
  }

  @Override
  public void appendPresenceFromHistory(TargetRef ref, String text, long tsEpochMs) {
    appendAdapter.appendPresence(ref, text, tsEpochMs);
  }

  @Override
  public void appendSpoilerChatFromHistory(
      TargetRef ref, String from, String text, long tsEpochMs) {
    appendAdapter.appendSpoilerChat(ref, from, text, tsEpochMs);
  }

  @Override
  public int chatHistoryInitialLoadLines() {
    return settingsReader.initialLoadLines();
  }

  @Override
  public int chatHistoryPageSize() {
    return settingsReader.pageSize();
  }

  @Override
  public int chatHistoryAutoLoadWheelDebounceMs() {
    return settingsReader.autoLoadWheelDebounceMs();
  }

  @Override
  public int chatHistoryLoadOlderChunkSize() {
    return settingsReader.loadOlderChunkSize();
  }

  @Override
  public int chatHistoryLoadOlderChunkDelayMs() {
    return settingsReader.loadOlderChunkDelayMs();
  }

  @Override
  public int chatHistoryLoadOlderChunkEdtBudgetMs() {
    return settingsReader.loadOlderChunkEdtBudgetMs();
  }

  @Override
  public boolean chatHistoryLockViewportDuringLoadOlder() {
    return settingsReader.lockViewportDuringLoadOlder();
  }

  @Override
  public int chatHistoryRemoteRequestTimeoutSeconds() {
    return settingsReader.remoteRequestTimeoutSeconds();
  }

  @Override
  public int chatHistoryRemoteZncPlaybackTimeoutSeconds() {
    return settingsReader.remoteZncPlaybackTimeoutSeconds();
  }

  @Override
  public int chatHistoryRemoteZncPlaybackWindowMinutes() {
    return settingsReader.remoteZncPlaybackWindowMinutes();
  }
}

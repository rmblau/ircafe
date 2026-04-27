package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.config.api.UiSettingsRuntimeConfigPort;
import cafe.woden.ircclient.logging.history.ChatHistoryTranscriptPort;
import cafe.woden.ircclient.logging.history.LoadOlderControlState;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.BooleanSupplier;
import java.util.function.ToIntFunction;
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
  private final UiSettingsBus settingsBus;
  private final UiSettingsRuntimeConfigPort runtimeConfig;

  public ChatHistoryTranscriptPortAdapter(
      ChatTranscriptStore transcripts,
      UiSettingsBus settingsBus,
      UiSettingsRuntimeConfigPort runtimeConfig) {
    this.transcripts = transcripts;
    this.settingsBus = settingsBus;
    this.runtimeConfig = runtimeConfig;
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
    return transcripts.ensureLoadOlderMessagesControl(ref);
  }

  @Override
  public void setLoadOlderMessagesControlState(TargetRef ref, LoadOlderControlState state) {
    transcripts.setLoadOlderMessagesControlState(
        ref, ChatHistoryTranscriptLoadOlderStateMapper.toUiState(state));
  }

  @Override
  public void setLoadOlderMessagesControlHandler(TargetRef ref, BooleanSupplier onLoad) {
    transcripts.setLoadOlderMessagesControlHandler(ref, onLoad);
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
    transcripts.appendChatFromHistory(ref, from, text, outgoingLocalEcho, tsEpochMs);
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
    transcripts.appendChatFromHistory(
        ref, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public void appendActionFromHistory(
      TargetRef ref, String from, String text, boolean outgoingLocalEcho, long tsEpochMs) {
    transcripts.appendActionFromHistory(ref, from, text, outgoingLocalEcho, tsEpochMs);
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
    transcripts.appendActionFromHistory(
        ref, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public void appendNoticeFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    transcripts.appendNoticeFromHistory(ref, from, text, tsEpochMs);
  }

  @Override
  public void appendNoticeFromHistory(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    transcripts.appendNoticeFromHistory(ref, from, text, tsEpochMs, messageId, ircv3Tags);
  }

  @Override
  public void appendStatusFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    transcripts.appendStatusFromHistory(ref, from, text, tsEpochMs);
  }

  @Override
  public void appendErrorFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    transcripts.appendErrorFromHistory(ref, from, text, tsEpochMs);
  }

  @Override
  public void appendPresenceFromHistory(TargetRef ref, String text, long tsEpochMs) {
    transcripts.appendPresenceFromHistory(ref, text, tsEpochMs);
  }

  @Override
  public void appendSpoilerChatFromHistory(
      TargetRef ref, String from, String text, long tsEpochMs) {
    transcripts.appendSpoilerChatFromHistory(ref, from, text, tsEpochMs);
  }

  @Override
  public int chatHistoryInitialLoadLines() {
    return intSetting(UiSettings::chatHistoryInitialLoadLines);
  }

  @Override
  public int chatHistoryPageSize() {
    return intSetting(UiSettings::chatHistoryPageSize);
  }

  @Override
  public int chatHistoryAutoLoadWheelDebounceMs() {
    return intSetting(UiSettings::chatHistoryAutoLoadWheelDebounceMs);
  }

  @Override
  public int chatHistoryLoadOlderChunkSize() {
    return intSetting(UiSettings::chatHistoryLoadOlderChunkSize);
  }

  @Override
  public int chatHistoryLoadOlderChunkDelayMs() {
    return intSetting(UiSettings::chatHistoryLoadOlderChunkDelayMs);
  }

  @Override
  public int chatHistoryLoadOlderChunkEdtBudgetMs() {
    return intSetting(UiSettings::chatHistoryLoadOlderChunkEdtBudgetMs);
  }

  @Override
  public boolean chatHistoryLockViewportDuringLoadOlder() {
    return booleanRuntimeConfig(
        UiSettingsRuntimeConfigPort::readChatHistoryLockViewportDuringLoadOlder, true);
  }

  @Override
  public int chatHistoryRemoteRequestTimeoutSeconds() {
    return intSetting(UiSettings::chatHistoryRemoteRequestTimeoutSeconds);
  }

  @Override
  public int chatHistoryRemoteZncPlaybackTimeoutSeconds() {
    return intSetting(UiSettings::chatHistoryRemoteZncPlaybackTimeoutSeconds);
  }

  @Override
  public int chatHistoryRemoteZncPlaybackWindowMinutes() {
    return intSetting(UiSettings::chatHistoryRemoteZncPlaybackWindowMinutes);
  }

  private int intSetting(ToIntFunction<UiSettings> extractor) {
    UiSettings s = settingsBus != null ? settingsBus.get() : null;
    return s != null ? extractor.applyAsInt(s) : 0;
  }

  private boolean booleanRuntimeConfig(RuntimeBooleanReader reader, boolean defaultValue) {
    return runtimeConfig != null ? reader.read(runtimeConfig, defaultValue) : defaultValue;
  }

  @FunctionalInterface
  private interface RuntimeBooleanReader {
    boolean read(UiSettingsRuntimeConfigPort config, boolean defaultValue);
  }
}

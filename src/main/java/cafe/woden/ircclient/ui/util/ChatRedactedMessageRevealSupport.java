package cafe.woden.ircclient.ui.util;

import cafe.woden.ircclient.logging.viewer.ChatRedactionAuditRecord;
import cafe.woden.ircclient.logging.viewer.ChatRedactionAuditService;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.chat.transcript.message.RedactedMessageContent;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.util.VirtualThreads;
import java.awt.Component;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class ChatRedactedMessageRevealSupport {

  private static final DateTimeFormatter TS_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private ChatRedactedMessageRevealSupport() {}

  public static void reveal(
      Component owner,
      TargetRef target,
      String messageId,
      ChatTranscriptStore transcripts,
      ChatRedactionAuditService auditService) {
    if (target == null || transcripts == null) return;
    String msgId = Objects.toString(messageId, "").trim();
    if (msgId.isEmpty()) return;

    RedactedMessageContent live = transcripts.redactedOriginalById(target, msgId);
    if (live != null) {
      ChatLineInspectorDialog.showReadOnlyTextDialog(
          owner,
          MESSAGES.text("chatTranscript.redactedReveal.title"),
          formatLiveRevealText(target, live));
      return;
    }

    if (auditService == null || !auditService.enabled()) {
      showUnavailable(owner);
      return;
    }

    VirtualThreads.start(
        "ircafe-reveal-redacted-message",
        () -> {
          Optional<ChatRedactionAuditRecord> audit = auditService.findLatest(target, msgId);
          SwingUtilities.invokeLater(
              () -> {
                if (audit.isPresent()) {
                  ChatLineInspectorDialog.showReadOnlyTextDialog(
                      owner,
                      MESSAGES.text("chatTranscript.redactedReveal.title"),
                      formatAuditRevealText(audit.get()));
                } else {
                  showUnavailable(owner);
                }
              });
        });
  }

  private static String formatLiveRevealText(TargetRef target, RedactedMessageContent content) {
    StringBuilder sb = new StringBuilder();
    sb.append(MESSAGES.text("chatTranscript.redactedReveal.source.live")).append('\n');
    appendCommonHeader(
        sb,
        target.serverId(),
        target.target(),
        content.messageId(),
        content.originalKind() == null ? "" : content.originalKind().name(),
        content.originalFromNick(),
        content.originalEpochMs(),
        content.redactedBy(),
        content.redactedAtEpochMs());
    sb.append('\n').append(Objects.toString(content.originalText(), ""));
    return sb.toString();
  }

  private static String formatAuditRevealText(ChatRedactionAuditRecord record) {
    StringBuilder sb = new StringBuilder();
    sb.append(MESSAGES.text("chatTranscript.redactedReveal.source.audit")).append('\n');
    appendCommonHeader(
        sb,
        record.serverId(),
        record.target(),
        record.messageId(),
        record.originalKind() == null ? "" : record.originalKind().name(),
        record.originalFromNick(),
        record.originalEpochMs(),
        record.redactedBy(),
        record.redactedAtEpochMs());
    sb.append('\n').append(Objects.toString(record.originalText(), ""));
    return sb.toString();
  }

  private static void appendCommonHeader(
      StringBuilder sb,
      String serverId,
      String target,
      String messageId,
      String kind,
      String fromNick,
      Long originalEpochMs,
      String redactedBy,
      Long redactedAtEpochMs) {
    if (!Objects.toString(serverId, "").isBlank()) {
      sb.append(MESSAGES.text("chatTranscript.redactedReveal.field.server", serverId)).append('\n');
    }
    if (!Objects.toString(target, "").isBlank()) {
      sb.append(MESSAGES.text("chatTranscript.redactedReveal.field.target", target)).append('\n');
    }
    if (!Objects.toString(messageId, "").isBlank()) {
      sb.append(MESSAGES.text("chatTranscript.redactedReveal.field.messageId", messageId))
          .append('\n');
    }
    if (!Objects.toString(kind, "").isBlank()) {
      sb.append(MESSAGES.text("chatTranscript.redactedReveal.field.kind", kind)).append('\n');
    }
    if (!Objects.toString(fromNick, "").isBlank()) {
      sb.append(MESSAGES.text("chatTranscript.redactedReveal.field.originalFrom", fromNick))
          .append('\n');
    }
    if (originalEpochMs != null && originalEpochMs > 0) {
      sb.append(
              MESSAGES.text(
                  "chatTranscript.redactedReveal.field.originalTime",
                  TS_FMT.format(Instant.ofEpochMilli(originalEpochMs))))
          .append('\n');
    }
    if (!Objects.toString(redactedBy, "").isBlank()) {
      sb.append(MESSAGES.text("chatTranscript.redactedReveal.field.redactedBy", redactedBy))
          .append('\n');
    }
    if (redactedAtEpochMs != null && redactedAtEpochMs > 0) {
      sb.append(
              MESSAGES.text(
                  "chatTranscript.redactedReveal.field.redactedAt",
                  TS_FMT.format(Instant.ofEpochMilli(redactedAtEpochMs))))
          .append('\n');
    }
  }

  private static void showUnavailable(Component owner) {
    JOptionPane.showMessageDialog(
        owner,
        MESSAGES.text("chatTranscript.redactedReveal.unavailable"),
        MESSAGES.text("chatTranscript.redactedReveal.title"),
        JOptionPane.INFORMATION_MESSAGE);
  }
}

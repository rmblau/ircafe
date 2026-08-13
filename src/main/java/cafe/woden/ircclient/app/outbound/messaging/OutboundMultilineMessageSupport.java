package cafe.woden.ircclient.app.outbound.messaging;

import cafe.woden.ircclient.app.api.Ircv3MultilineFeatureSupport;
import cafe.woden.ircclient.app.api.UiPort;
import cafe.woden.ircclient.irc.ircv3.Ircv3MultilinePayload;
import cafe.woden.ircclient.model.TargetRef;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Shared multiline payload planning and fallback handling for outbound message sends. */
@Component
@ApplicationLayer
@RequiredArgsConstructor
final class OutboundMultilineMessageSupport {

  @NonNull private final Ircv3MultilineFeatureSupport multilineFeatureSupport;
  @NonNull private final UiPort ui;

  MultilineSendPlan plan(TargetRef target, String message, String statusPrefix) {
    String payload = Objects.toString(message, "").trim();
    Ircv3MultilinePayload analyzed = Ircv3MultilinePayload.from(payload);
    List<String> lines = analyzed.lines();
    if (target == null || !analyzed.isMultiline()) {
      return MultilineSendPlan.send(payload);
    }

    int lineCount = analyzed.lineCount();
    long payloadUtf8Bytes = analyzed.utf8Bytes();
    String reason =
        multilineFeatureSupport.unavailableOrLimitReason(
            target.serverId(), lineCount, payloadUtf8Bytes);
    if (reason.isBlank()) {
      return MultilineSendPlan.send(analyzed.joinedText());
    }

    boolean sendSplit = false;
    try {
      sendSplit = ui.confirmMultilineSplitFallback(target, lineCount, payloadUtf8Bytes, reason);
    } catch (Exception ignored) {
      sendSplit = false;
    }

    if (!sendSplit) {
      ui.appendStatus(target, statusPrefix, "Send canceled.");
      return MultilineSendPlan.cancel();
    }

    ui.appendStatus(target, statusPrefix, reason + " Sending as " + lineCount + " separate lines.");
    return MultilineSendPlan.split(lines);
  }

  record MultilineSendPlan(Decision decision, String payload, List<String> lines) {

    static MultilineSendPlan send(String payload) {
      return new MultilineSendPlan(Decision.SEND, Objects.toString(payload, ""), List.of());
    }

    static MultilineSendPlan split(List<String> lines) {
      return new MultilineSendPlan(
          Decision.SPLIT_LINES, "", lines == null ? List.of() : List.copyOf(lines));
    }

    static MultilineSendPlan cancel() {
      return new MultilineSendPlan(Decision.CANCEL, "", List.of());
    }

    boolean shouldCancel() {
      return decision == Decision.CANCEL;
    }

    boolean shouldSplitLines() {
      return decision == Decision.SPLIT_LINES;
    }
  }

  enum Decision {
    SEND,
    SPLIT_LINES,
    CANCEL
  }
}

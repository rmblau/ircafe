package cafe.woden.ircclient.irc.ircv3;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Canonical connection-state family for tracked IRCv3 capabilities and aliases. */
public enum Ircv3TrackedCapability {
  ZNC_PLAYBACK,
  BATCH,
  CHAT_HISTORY,
  SOJU_BOUNCER_NETWORKS,
  SERVER_TIME,
  STANDARD_REPLIES,
  ECHO_MESSAGE,
  CAP_NOTIFY,
  LABELED_RESPONSE,
  SETNAME,
  CHGHOST,
  STS,
  MULTILINE,
  DRAFT_MULTILINE,
  MESSAGE_EDIT,
  MESSAGE_REDACTION,
  MESSAGE_TAGS,
  READ_MARKER,
  MONITOR,
  EXTENDED_MONITOR;

  public static Optional<Ircv3TrackedCapability> resolve(String capabilityName) {
    String normalized = Objects.toString(capabilityName, "").trim().toLowerCase(Locale.ROOT);
    return switch (normalized) {
      case "znc.in/playback" -> Optional.of(ZNC_PLAYBACK);
      case "batch" -> Optional.of(BATCH);
      case "chathistory", "draft/chathistory" -> Optional.of(CHAT_HISTORY);
      case "soju.im/bouncer-networks" -> Optional.of(SOJU_BOUNCER_NETWORKS);
      case "server-time" -> Optional.of(SERVER_TIME);
      case "standard-replies" -> Optional.of(STANDARD_REPLIES);
      case "echo-message" -> Optional.of(ECHO_MESSAGE);
      case "cap-notify" -> Optional.of(CAP_NOTIFY);
      case "labeled-response" -> Optional.of(LABELED_RESPONSE);
      case "setname" -> Optional.of(SETNAME);
      case "chghost" -> Optional.of(CHGHOST);
      case "sts" -> Optional.of(STS);
      case "multiline" -> Optional.of(MULTILINE);
      case "draft/multiline" -> Optional.of(DRAFT_MULTILINE);
      case "message-edit", "draft/message-edit" -> Optional.of(MESSAGE_EDIT);
      case "message-redaction", "draft/message-redaction" -> Optional.of(MESSAGE_REDACTION);
      case "message-tags" -> Optional.of(MESSAGE_TAGS);
      case "read-marker", "draft/read-marker" -> Optional.of(READ_MARKER);
      case "monitor" -> Optional.of(MONITOR);
      case "extended-monitor", "draft/extended-monitor" -> Optional.of(EXTENDED_MONITOR);
      default -> Optional.empty();
    };
  }
}

package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Transport-neutral values emitted by parsed inbound IRC command providers. */
public sealed interface Ircv3InboundCommandSignal
    permits Ircv3InboundCommandSignal.HostmaskObserved,
        Ircv3InboundCommandSignal.UserAwayObserved,
        Ircv3InboundCommandSignal.SelfAwayObserved,
        Ircv3InboundCommandSignal.AccountObserved,
        Ircv3InboundCommandSignal.SetNameObserved,
        Ircv3InboundCommandSignal.HostChangedObserved,
        Ircv3InboundCommandSignal.InviteObserved,
        Ircv3InboundCommandSignal.StandardReplyObserved,
        Ircv3InboundCommandSignal.MonitorStatusObserved,
        Ircv3InboundCommandSignal.MonitorListObserved,
        Ircv3InboundCommandSignal.MonitorListEnded,
        Ircv3InboundCommandSignal.MonitorListFull,
        Ircv3InboundCommandSignal.ChannelHostmaskObserved,
        Ircv3InboundCommandSignal.WhoisEndedObserved,
        Ircv3InboundCommandSignal.WhoxSchemaObserved,
        Ircv3InboundCommandSignal.ReadMarkerObserved,
        Ircv3InboundCommandSignal.MessageRedactionObserved,
        Ircv3InboundCommandSignal.HistoryBatchStarted,
        Ircv3InboundCommandSignal.HistoryBatchEnded,
        Ircv3InboundCommandSignal.HistoryBatchIgnored,
        Ircv3InboundCommandSignal.ZncDetectedObserved,
        Ircv3InboundCommandSignal.MultilineLimitsObserved,
        Ircv3InboundCommandSignal.CapabilityChangeObserved,
        Ircv3InboundCommandSignal.CapabilityFallbackPlanned,
        Ircv3InboundCommandSignal.IsupportTokenObserved,
        Ircv3InboundCommandSignal.WhoxSupportObserved,
        Ircv3InboundCommandSignal.MonitorSupportObserved,
        Ircv3InboundCommandSignal.ClientTagPolicyObserved,
        Ircv3InboundCommandSignal.StsPolicyObserved,
        Ircv3InboundCommandSignal.SaslCapabilityObserved,
        Ircv3InboundCommandSignal.SaslServerMessageObserved,
        Ircv3InboundCommandSignal.SaslFailureObserved {

  enum AccountState {
    LOGGED_IN,
    LOGGED_OUT
  }

  enum SetNameSource {
    EXTENDED_JOIN,
    SETNAME
  }

  enum StandardReplyKind {
    FAIL,
    WARN,
    NOTE
  }

  enum SaslCapabilityPhase {
    LIST,
    ACK,
    NAK
  }

  enum StsPolicyOutcome {
    IGNORE_MISSING_HOST,
    IGNORE_EMPTY_VALUE,
    IGNORE_INSECURE_CONNECTION,
    IGNORE_INVALID_DIRECTIVE,
    CLEAR,
    LEARN
  }

  record HostmaskObserved(String nick, String hostmask) implements Ircv3InboundCommandSignal {
    public HostmaskObserved {
      nick = Objects.toString(nick, "").trim();
      hostmask = Objects.toString(hostmask, "").trim();
    }
  }

  record UserAwayObserved(String nick, boolean away, String message)
      implements Ircv3InboundCommandSignal {
    public UserAwayObserved {
      nick = Objects.toString(nick, "").trim();
      message = message == null ? null : message.trim();
    }
  }

  record SelfAwayObserved(boolean away, String server, String message)
      implements Ircv3InboundCommandSignal {
    public SelfAwayObserved {
      server = Objects.toString(server, "").trim();
      message = message == null ? null : message.trim();
    }
  }

  record AccountObserved(String nick, AccountState state, String accountName)
      implements Ircv3InboundCommandSignal {
    public AccountObserved {
      nick = Objects.toString(nick, "").trim();
      state = Objects.requireNonNull(state, "state");
      accountName = state == AccountState.LOGGED_OUT ? null : normalizeNullable(accountName);
    }
  }

  record SetNameObserved(String nick, String channel, String realName, SetNameSource source)
      implements Ircv3InboundCommandSignal {
    public SetNameObserved {
      nick = Objects.toString(nick, "").trim();
      channel = Objects.toString(channel, "").trim();
      realName = Objects.toString(realName, "").trim();
      source = Objects.requireNonNull(source, "source");
    }
  }

  record HostChangedObserved(String nick, String user, String host, String hostmask)
      implements Ircv3InboundCommandSignal {
    public HostChangedObserved {
      nick = Objects.toString(nick, "").trim();
      user = Objects.toString(user, "").trim();
      host = Objects.toString(host, "").trim();
      hostmask = Objects.toString(hostmask, "").trim();
    }
  }

  record InviteObserved(String fromNick, String inviteeNick, String channel, String reason)
      implements Ircv3InboundCommandSignal {
    public InviteObserved {
      fromNick = Objects.toString(fromNick, "").trim();
      inviteeNick = Objects.toString(inviteeNick, "").trim();
      channel = Objects.toString(channel, "").trim();
      reason = Objects.toString(reason, "").trim();
    }
  }

  record StandardReplyObserved(
      StandardReplyKind kind, String command, String code, String context, String description)
      implements Ircv3InboundCommandSignal {
    public StandardReplyObserved {
      kind = Objects.requireNonNull(kind, "kind");
      command = Objects.toString(command, "").trim();
      code = Objects.toString(code, "").trim();
      context = Objects.toString(context, "").trim();
      description = Objects.toString(description, "").trim();
    }
  }

  record MonitorStatusEntry(String nick, String hostmask) {
    public MonitorStatusEntry {
      nick = Objects.toString(nick, "").trim();
      hostmask = Objects.toString(hostmask, "").trim();
    }
  }

  record MonitorStatusObserved(boolean online, List<MonitorStatusEntry> entries)
      implements Ircv3InboundCommandSignal {
    public MonitorStatusObserved {
      entries = List.copyOf(Objects.requireNonNullElse(entries, List.of()));
    }
  }

  record MonitorListObserved(List<String> nicks) implements Ircv3InboundCommandSignal {
    public MonitorListObserved {
      nicks = List.copyOf(Objects.requireNonNullElse(nicks, List.of()));
    }
  }

  record MonitorListEnded() implements Ircv3InboundCommandSignal {}

  record MonitorListFull(int limit, List<String> nicks, String message)
      implements Ircv3InboundCommandSignal {
    public MonitorListFull {
      if (limit < 0) limit = 0;
      nicks = List.copyOf(Objects.requireNonNullElse(nicks, List.of()));
      message = Objects.toString(message, "").trim();
    }
  }

  record ChannelHostmaskObserved(String channel, String nick, String hostmask)
      implements Ircv3InboundCommandSignal {
    public ChannelHostmaskObserved {
      channel = Objects.toString(channel, "").trim();
      nick = Objects.toString(nick, "").trim();
      hostmask = Objects.toString(hostmask, "").trim();
    }
  }

  record WhoisEndedObserved(String nick) implements Ircv3InboundCommandSignal {
    public WhoisEndedObserved {
      nick = Objects.toString(nick, "").trim();
    }
  }

  record WhoxSchemaObserved(boolean compatible, String reason)
      implements Ircv3InboundCommandSignal {
    public WhoxSchemaObserved {
      reason = Objects.toString(reason, "").trim();
    }
  }

  record ReadMarkerObserved(String target, String marker) implements Ircv3InboundCommandSignal {
    public ReadMarkerObserved {
      target = Objects.toString(target, "").trim();
      marker = Objects.toString(marker, "").trim();
    }
  }

  record MessageRedactionObserved(String target, String messageId)
      implements Ircv3InboundCommandSignal {
    public MessageRedactionObserved {
      target = Objects.toString(target, "").trim();
      messageId = Objects.toString(messageId, "").trim();
    }
  }

  record HistoryBatchStarted(String batchId, String type, String target)
      implements Ircv3InboundCommandSignal {
    public HistoryBatchStarted {
      batchId = Objects.toString(batchId, "").trim();
      type = Objects.toString(type, "").trim();
      target = Objects.toString(target, "").trim();
    }
  }

  record HistoryBatchEnded(String batchId) implements Ircv3InboundCommandSignal {
    public HistoryBatchEnded {
      batchId = Objects.toString(batchId, "").trim();
    }
  }

  record HistoryBatchIgnored() implements Ircv3InboundCommandSignal {}

  record ZncDetectedObserved(String source, String evidence) implements Ircv3InboundCommandSignal {
    public ZncDetectedObserved {
      source = Objects.toString(source, "").trim();
      evidence = Objects.toString(evidence, "").trim();
    }
  }

  record MultilineLimitsObserved(
      boolean draftCapability,
      long offeredMaxBytes,
      long offeredMaxLines,
      long negotiatedMaxBytes,
      long negotiatedMaxLines)
      implements Ircv3InboundCommandSignal {
    public MultilineLimitsObserved {
      offeredMaxBytes = Math.max(0L, offeredMaxBytes);
      offeredMaxLines = Math.max(0L, offeredMaxLines);
      negotiatedMaxBytes = Math.max(0L, negotiatedMaxBytes);
      negotiatedMaxLines = Math.max(0L, negotiatedMaxLines);
    }
  }

  record CapabilityChangeObserved(
      String action, String capabilityName, boolean enabled, boolean updateState)
      implements Ircv3InboundCommandSignal {
    public CapabilityChangeObserved {
      action = Objects.toString(action, "").trim().toUpperCase(Locale.ROOT);
      capabilityName = Objects.toString(capabilityName, "").trim().toLowerCase(Locale.ROOT);
    }
  }

  record CapabilityFallbackPlanned(
      boolean requestMessageTags, boolean requestBatch, String historyCapability)
      implements Ircv3InboundCommandSignal {
    public CapabilityFallbackPlanned {
      historyCapability = Objects.toString(historyCapability, "").trim().toLowerCase(Locale.ROOT);
    }
  }

  record IsupportTokenObserved(String key, String value, boolean removed)
      implements Ircv3InboundCommandSignal {
    public IsupportTokenObserved {
      key = Objects.toString(key, "").trim();
      value = Objects.toString(value, "").trim();
    }
  }

  record WhoxSupportObserved(boolean supported) implements Ircv3InboundCommandSignal {}

  record MonitorSupportObserved(boolean supported, int limit) implements Ircv3InboundCommandSignal {
    public MonitorSupportObserved {
      if (limit < 0) limit = 0;
      if (!supported) limit = 0;
    }
  }

  record ClientTagPolicyObserved(String tagName, boolean allowed, String rawDenyValue)
      implements Ircv3InboundCommandSignal {
    public ClientTagPolicyObserved {
      tagName = Objects.toString(tagName, "").trim();
      rawDenyValue = Objects.toString(rawDenyValue, "").trim();
    }
  }

  record StsPolicyObserved(
      StsPolicyOutcome outcome,
      String host,
      String rawValue,
      long expiresAtEpochMilli,
      Integer port,
      boolean preload,
      long durationSeconds)
      implements Ircv3InboundCommandSignal {
    public StsPolicyObserved {
      outcome = Objects.requireNonNull(outcome, "outcome");
      host = Objects.toString(host, "").trim();
      rawValue = Objects.toString(rawValue, "").trim();
    }
  }

  record SaslCapabilityObserved(
      SaslCapabilityPhase phase,
      boolean continuationOnly,
      boolean saslOffered,
      List<String> mechanismsUpper)
      implements Ircv3InboundCommandSignal {
    public SaslCapabilityObserved {
      phase = Objects.requireNonNull(phase, "phase");
      LinkedHashSet<String> normalized = new LinkedHashSet<>();
      for (String mechanism : Objects.requireNonNullElse(mechanismsUpper, List.<String>of())) {
        String value = Objects.toString(mechanism, "").trim().toUpperCase(Locale.ROOT);
        if (!value.isEmpty()) {
          normalized.add(value);
        }
      }
      mechanismsUpper = List.copyOf(normalized);
      if (continuationOnly || !saslOffered) {
        mechanismsUpper = List.of();
      }
    }
  }

  record SaslServerMessageObserved(String command, String trailing, Integer numeric)
      implements Ircv3InboundCommandSignal {
    public SaslServerMessageObserved {
      command = Objects.toString(command, "").trim().toUpperCase(Locale.ROOT);
      trailing = Objects.toString(trailing, "").trim();
      if (numeric != null && (numeric < 0 || numeric > 999)) {
        numeric = null;
      }
    }
  }

  record SaslFailureObserved(
      int numeric, String trailingMessage, String detail, String disconnectReason)
      implements Ircv3InboundCommandSignal {
    public SaslFailureObserved {
      trailingMessage = normalizeNullable(trailingMessage);
      detail = Objects.toString(detail, "").trim();
      disconnectReason = Objects.toString(disconnectReason, "").trim();
    }
  }

  private static String normalizeNullable(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}

package cafe.woden.ircclient.irc.ircv3;

/** Immutable read view over negotiated IRCv3 capability state for one connection. */
public record Ircv3CapabilitySnapshot(
    boolean zncPlaybackCapAcked,
    boolean batchCapAcked,
    boolean chatHistoryCapAcked,
    boolean echoMessageCapAcked,
    boolean capNotifyCapAcked,
    boolean labeledResponseCapAcked,
    boolean setnameCapAcked,
    boolean chghostCapAcked,
    boolean stsCapAcked,
    boolean multilineCapAcked,
    boolean draftMultilineCapAcked,
    long multilineMaxBytes,
    long multilineMaxLines,
    long draftMultilineMaxBytes,
    long draftMultilineMaxLines,
    boolean draftMessageEditCapAcked,
    boolean draftMessageRedactionCapAcked,
    boolean messageTagsCapAcked,
    boolean typingClientTagAllowed,
    boolean typingClientTagPolicyKnown,
    boolean readMarkerCapAcked,
    boolean monitorCapAcked,
    boolean extendedMonitorCapAcked,
    boolean sojuBouncerNetworksCapAcked,
    boolean serverTimeCapAcked,
    boolean standardRepliesCapAcked,
    boolean monitorSupported,
    long monitorMaxTargets) {

  public Ircv3CapabilitySnapshot {
    multilineMaxBytes = Math.max(0L, multilineMaxBytes);
    multilineMaxLines = Math.max(0L, multilineMaxLines);
    draftMultilineMaxBytes = Math.max(0L, draftMultilineMaxBytes);
    draftMultilineMaxLines = Math.max(0L, draftMultilineMaxLines);
    monitorMaxTargets = Math.max(0L, monitorMaxTargets);
  }

  public boolean multilineAvailable() {
    return multilineCapAcked || draftMultilineCapAcked;
  }

  public long negotiatedMultilineMaxBytes() {
    if (multilineCapAcked) {
      return multilineMaxBytes;
    }
    if (draftMultilineCapAcked) {
      return draftMultilineMaxBytes;
    }
    return 0L;
  }

  public long negotiatedMultilineMaxLines() {
    if (multilineCapAcked) {
      return multilineMaxLines;
    }
    if (draftMultilineCapAcked) {
      return draftMultilineMaxLines;
    }
    return 0L;
  }

  public boolean typingAllowedByPolicy() {
    return !typingClientTagPolicyKnown || typingClientTagAllowed;
  }

  public boolean typingAvailable() {
    return messageTagsCapAcked && typingAllowedByPolicy();
  }

  public boolean chatHistoryAvailable() {
    return chatHistoryCapAcked && batchCapAcked;
  }

  public boolean monitorAvailable() {
    return monitorSupported || monitorCapAcked;
  }
}

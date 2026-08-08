package cafe.woden.ircclient.irc.ircv3;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Thread-safe negotiated capability state for one IRC connection. */
public final class Ircv3CapabilityState {

  private final AtomicBoolean zncPlaybackCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean batchCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean chatHistoryCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean echoMessageCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean capNotifyCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean labeledResponseCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean setnameCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean chghostCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean stsCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean multilineCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean draftMultilineCapAcked = new AtomicBoolean(false);
  private final AtomicLong multilineMaxBytes = new AtomicLong(0L);
  private final AtomicLong multilineMaxLines = new AtomicLong(0L);
  private final AtomicLong draftMultilineMaxBytes = new AtomicLong(0L);
  private final AtomicLong draftMultilineMaxLines = new AtomicLong(0L);
  private final AtomicLong multilineOfferedMaxBytes = new AtomicLong(0L);
  private final AtomicLong multilineOfferedMaxLines = new AtomicLong(0L);
  private final AtomicLong draftMultilineOfferedMaxBytes = new AtomicLong(0L);
  private final AtomicLong draftMultilineOfferedMaxLines = new AtomicLong(0L);
  private final AtomicBoolean draftMessageEditCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean draftMessageRedactionCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean messageTagsCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean typingClientTagAllowed = new AtomicBoolean(true);
  private final AtomicBoolean typingClientTagPolicyKnown = new AtomicBoolean(false);
  private final AtomicBoolean readMarkerCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean monitorCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean extendedMonitorCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean sojuBouncerNetworksCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean serverTimeCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean standardRepliesCapAcked = new AtomicBoolean(false);
  private final AtomicBoolean monitorSupported = new AtomicBoolean(false);
  private final AtomicLong monitorMaxTargets = new AtomicLong(0L);

  private final AtomicBoolean messageTagsFallbackRequest = new AtomicBoolean(false);
  private final AtomicBoolean batchFallbackRequest = new AtomicBoolean(false);
  private final AtomicBoolean chatHistoryFallbackRequest = new AtomicBoolean(false);
  private final AtomicBoolean capabilitySummaryObserved = new AtomicBoolean(false);
  private final AtomicBoolean missingServerTimeWarned = new AtomicBoolean(false);
  private final AtomicBoolean unavailableTypingWarned = new AtomicBoolean(false);

  public Ircv3CapabilitySnapshot snapshot() {
    return new Ircv3CapabilitySnapshot(
        zncPlaybackCapAcked.get(),
        batchCapAcked.get(),
        chatHistoryCapAcked.get(),
        echoMessageCapAcked.get(),
        capNotifyCapAcked.get(),
        labeledResponseCapAcked.get(),
        setnameCapAcked.get(),
        chghostCapAcked.get(),
        stsCapAcked.get(),
        multilineCapAcked.get(),
        draftMultilineCapAcked.get(),
        multilineMaxBytes.get(),
        multilineMaxLines.get(),
        draftMultilineMaxBytes.get(),
        draftMultilineMaxLines.get(),
        draftMessageEditCapAcked.get(),
        draftMessageRedactionCapAcked.get(),
        messageTagsCapAcked.get(),
        typingClientTagAllowed.get(),
        typingClientTagPolicyKnown.get(),
        readMarkerCapAcked.get(),
        monitorCapAcked.get(),
        extendedMonitorCapAcked.get(),
        sojuBouncerNetworksCapAcked.get(),
        serverTimeCapAcked.get(),
        standardRepliesCapAcked.get(),
        monitorSupported.get(),
        monitorMaxTargets.get());
  }

  public boolean updateTrackedCapability(String capabilityName, boolean enabled) {
    Ircv3TrackedCapability capability =
        Ircv3TrackedCapability.resolve(capabilityName).orElse(null);
    if (capability == null) {
      return false;
    }

    return switch (capability) {
      case ZNC_PLAYBACK -> update(zncPlaybackCapAcked, enabled);
      case BATCH -> update(batchCapAcked, enabled);
      case CHAT_HISTORY -> update(chatHistoryCapAcked, enabled);
      case SOJU_BOUNCER_NETWORKS -> update(sojuBouncerNetworksCapAcked, enabled);
      case SERVER_TIME -> update(serverTimeCapAcked, enabled);
      case STANDARD_REPLIES -> update(standardRepliesCapAcked, enabled);
      case ECHO_MESSAGE -> update(echoMessageCapAcked, enabled);
      case CAP_NOTIFY -> update(capNotifyCapAcked, enabled);
      case LABELED_RESPONSE -> update(labeledResponseCapAcked, enabled);
      case SETNAME -> update(setnameCapAcked, enabled);
      case CHGHOST -> update(chghostCapAcked, enabled);
      case STS -> update(stsCapAcked, enabled);
      case MULTILINE ->
          updateWithLimitReset(
              multilineCapAcked, multilineMaxBytes, multilineMaxLines, enabled);
      case DRAFT_MULTILINE ->
          updateWithLimitReset(
              draftMultilineCapAcked,
              draftMultilineMaxBytes,
              draftMultilineMaxLines,
              enabled);
      case MESSAGE_EDIT -> update(draftMessageEditCapAcked, enabled);
      case MESSAGE_REDACTION -> update(draftMessageRedactionCapAcked, enabled);
      case MESSAGE_TAGS -> update(messageTagsCapAcked, enabled);
      case READ_MARKER -> update(readMarkerCapAcked, enabled);
      case MONITOR -> update(monitorCapAcked, enabled);
      case EXTENDED_MONITOR -> update(extendedMonitorCapAcked, enabled);
    };
  }

  public boolean zncPlaybackCapAcked() {
    return zncPlaybackCapAcked.get();
  }

  public boolean batchCapAcked() {
    return batchCapAcked.get();
  }

  public boolean chatHistoryCapAcked() {
    return chatHistoryCapAcked.get();
  }

  public boolean messageTagsCapAcked() {
    return messageTagsCapAcked.get();
  }

  public boolean sojuBouncerNetworksCapAcked() {
    return sojuBouncerNetworksCapAcked.get();
  }

  public void setZncPlaybackCapAcked(boolean acked) {
    zncPlaybackCapAcked.set(acked);
  }

  public void setBatchCapAcked(boolean acked) {
    batchCapAcked.set(acked);
  }

  public void setChatHistoryCapAcked(boolean acked) {
    chatHistoryCapAcked.set(acked);
  }

  public void setEchoMessageCapAcked(boolean acked) {
    echoMessageCapAcked.set(acked);
  }

  public void setMultilineCapAcked(boolean acked) {
    multilineCapAcked.set(acked);
  }

  public void setDraftMultilineCapAcked(boolean acked) {
    draftMultilineCapAcked.set(acked);
  }

  public void setMultilineLimits(long maxBytes, long maxLines) {
    multilineMaxBytes.set(normalizeLimit(maxBytes));
    multilineMaxLines.set(normalizeLimit(maxLines));
  }

  public void setDraftMultilineLimits(long maxBytes, long maxLines) {
    draftMultilineMaxBytes.set(normalizeLimit(maxBytes));
    draftMultilineMaxLines.set(normalizeLimit(maxLines));
  }

  public void setMessageTagsCapAcked(boolean acked) {
    messageTagsCapAcked.set(acked);
  }

  public void setReadMarkerCapAcked(boolean acked) {
    readMarkerCapAcked.set(acked);
  }

  public void setSojuBouncerNetworksCapAcked(boolean acked) {
    sojuBouncerNetworksCapAcked.set(acked);
  }

  public long multilineOfferedMaxBytes(boolean draft) {
    return normalizeLimit(
        draft ? draftMultilineOfferedMaxBytes.get() : multilineOfferedMaxBytes.get());
  }

  public long multilineOfferedMaxLines(boolean draft) {
    return normalizeLimit(
        draft ? draftMultilineOfferedMaxLines.get() : multilineOfferedMaxLines.get());
  }

  public void setMultilineOfferedMaxBytes(boolean draft, long maxBytes) {
    (draft ? draftMultilineOfferedMaxBytes : multilineOfferedMaxBytes)
        .set(normalizeLimit(maxBytes));
  }

  public void setMultilineOfferedMaxLines(boolean draft, long maxLines) {
    (draft ? draftMultilineOfferedMaxLines : multilineOfferedMaxLines)
        .set(normalizeLimit(maxLines));
  }

  public void setNegotiatedMultilineMaxBytes(boolean draft, long maxBytes) {
    (draft ? draftMultilineMaxBytes : multilineMaxBytes).set(normalizeLimit(maxBytes));
  }

  public void setNegotiatedMultilineMaxLines(boolean draft, long maxLines) {
    (draft ? draftMultilineMaxLines : multilineMaxLines).set(normalizeLimit(maxLines));
  }

  public boolean updateMonitorSupport(boolean supported, long limit) {
    long normalizedLimit = normalizeLimit(limit);
    boolean previousSupported = monitorSupported.getAndSet(supported);
    long previousLimit = monitorMaxTargets.getAndSet(normalizedLimit);
    return previousSupported != supported || previousLimit != normalizedLimit;
  }

  public boolean updateTypingClientTagPolicy(boolean allowed) {
    typingClientTagPolicyKnown.set(true);
    return typingClientTagAllowed.getAndSet(allowed) != allowed;
  }

  public boolean beginMessageTagsFallbackRequest() {
    return messageTagsFallbackRequest.compareAndSet(false, true);
  }

  public void clearMessageTagsFallbackRequest() {
    messageTagsFallbackRequest.set(false);
  }

  public boolean beginBatchFallbackRequest() {
    return batchFallbackRequest.compareAndSet(false, true);
  }

  public void clearBatchFallbackRequest() {
    batchFallbackRequest.set(false);
  }

  public boolean beginChatHistoryFallbackRequest() {
    return chatHistoryFallbackRequest.compareAndSet(false, true);
  }

  public void clearChatHistoryFallbackRequest() {
    chatHistoryFallbackRequest.set(false);
  }

  public boolean beginCapabilitySummaryObservation() {
    return !capabilitySummaryObserved.getAndSet(true);
  }

  public boolean shouldWarnMissingServerTime() {
    return missingServerTimeWarned.compareAndSet(false, true);
  }

  public boolean shouldWarnUnavailableTyping() {
    return unavailableTypingWarned.compareAndSet(false, true);
  }

  public void resetConnectionSession() {
    zncPlaybackCapAcked.set(false);
    batchCapAcked.set(false);
    chatHistoryCapAcked.set(false);
    echoMessageCapAcked.set(false);
    capNotifyCapAcked.set(false);
    labeledResponseCapAcked.set(false);
    setnameCapAcked.set(false);
    chghostCapAcked.set(false);
    stsCapAcked.set(false);
    multilineCapAcked.set(false);
    draftMultilineCapAcked.set(false);
    multilineMaxBytes.set(0L);
    multilineMaxLines.set(0L);
    draftMultilineMaxBytes.set(0L);
    draftMultilineMaxLines.set(0L);
    multilineOfferedMaxBytes.set(0L);
    multilineOfferedMaxLines.set(0L);
    draftMultilineOfferedMaxBytes.set(0L);
    draftMultilineOfferedMaxLines.set(0L);
    draftMessageEditCapAcked.set(false);
    draftMessageRedactionCapAcked.set(false);
    messageTagsCapAcked.set(false);
    typingClientTagAllowed.set(true);
    typingClientTagPolicyKnown.set(false);
    messageTagsFallbackRequest.set(false);
    batchFallbackRequest.set(false);
    chatHistoryFallbackRequest.set(false);
    readMarkerCapAcked.set(false);
    monitorCapAcked.set(false);
    extendedMonitorCapAcked.set(false);
    sojuBouncerNetworksCapAcked.set(false);
    serverTimeCapAcked.set(false);
    standardRepliesCapAcked.set(false);
    monitorSupported.set(false);
    monitorMaxTargets.set(0L);
    capabilitySummaryObserved.set(false);
    unavailableTypingWarned.set(false);
  }

  private static boolean update(AtomicBoolean target, boolean enabled) {
    return target.getAndSet(enabled) != enabled;
  }

  private static boolean updateWithLimitReset(
      AtomicBoolean target, AtomicLong maxBytes, AtomicLong maxLines, boolean enabled) {
    boolean changed = update(target, enabled);
    if (!enabled) {
      maxBytes.set(0L);
      maxLines.set(0L);
    }
    return changed;
  }

  private static long normalizeLimit(long value) {
    return Math.max(0L, value);
  }
}

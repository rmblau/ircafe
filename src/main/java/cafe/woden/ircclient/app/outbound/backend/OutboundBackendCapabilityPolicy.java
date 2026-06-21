package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.api.AvailableBackendIdsPort;
import cafe.woden.ircclient.app.api.BackendAvailabilityReasonFormatter;
import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureContext;
import cafe.woden.ircclient.app.outbound.support.CommandTargetPolicy;
import cafe.woden.ircclient.irc.backend.IrcBackendAvailabilityPort;
import cafe.woden.ircclient.irc.port.IrcNegotiatedFeaturePort;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Backend capability facade used by outbound command services. */
@Component
@ApplicationLayer
@RequiredArgsConstructor
public final class OutboundBackendCapabilityPolicy {
  @NonNull private final CommandTargetPolicy commandTargetPolicy;
  @NonNull private final OutboundBackendFeatureRegistry outboundBackendFeatureRegistry;

  @Qualifier("ircNegotiatedFeaturePort")
  @NonNull
  private final IrcNegotiatedFeaturePort irc;

  @Qualifier("ircClientService")
  @NonNull
  private final IrcBackendAvailabilityPort backendAvailability;

  @NonNull private final AvailableBackendIdsPort backendMetadata;

  public String backendIdForServer(String serverId) {
    return commandTargetPolicy.backendIdForServer(serverId);
  }

  public boolean supportsSemanticUpload(String serverId) {
    return supportsSemanticUploadByBackendId(backendIdForServer(serverId));
  }

  public boolean supportsQuasselCoreCommands(String serverId) {
    return supportsQuasselCoreCommandsByBackendId(backendIdForServer(serverId));
  }

  public boolean persistsJoinedChannelsLocally(String serverId) {
    return persistsJoinedChannelsLocallyByBackendId(backendIdForServer(serverId));
  }

  public boolean supportsSemanticUploadByBackendId(String backendId) {
    return outboundBackendFeatureRegistry.adapterFor(backendId).supportsSemanticUpload();
  }

  public boolean supportsQuasselCoreCommandsByBackendId(String backendId) {
    return outboundBackendFeatureRegistry.adapterFor(backendId).supportsQuasselCoreCommands();
  }

  public boolean persistsJoinedChannelsLocallyByBackendId(String backendId) {
    return outboundBackendFeatureRegistry.adapterFor(backendId).persistsJoinedChannelsLocally();
  }

  public boolean supportsReadMarker(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return false;
    return outboundBackendFeatureRegistry
        .adapterFor(backendIdForServer(sid))
        .supportsReadMarker(featureContext(sid));
  }

  public boolean supportsMonitor(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return false;
    return outboundBackendFeatureRegistry
        .adapterFor(backendIdForServer(sid))
        .supportsMonitor(featureContext(sid));
  }

  public boolean supportsLabeledResponse(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return false;
    return outboundBackendFeatureRegistry
        .adapterFor(backendIdForServer(sid))
        .supportsLabeledResponse(featureContext(sid));
  }

  public boolean supportsMultiline(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return false;
    return outboundBackendFeatureRegistry
        .adapterFor(backendIdForServer(sid))
        .supportsMultiline(featureContext(sid));
  }

  public boolean supportsMessageTags(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return false;
    return outboundBackendFeatureRegistry
        .adapterFor(backendIdForServer(sid))
        .supportsMessageTags(featureContext(sid));
  }

  public boolean supportsDraftReply(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return false;
    return outboundBackendFeatureRegistry
        .adapterFor(backendIdForServer(sid))
        .supportsDraftReply(featureContext(sid));
  }

  public boolean supportsDraftReact(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return false;
    return outboundBackendFeatureRegistry
        .adapterFor(backendIdForServer(sid))
        .supportsDraftReact(featureContext(sid));
  }

  public boolean supportsDraftUnreact(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return false;
    return outboundBackendFeatureRegistry
        .adapterFor(backendIdForServer(sid))
        .supportsDraftUnreact(featureContext(sid));
  }

  public boolean supportsExperimentalMessageEdit(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return false;
    return outboundBackendFeatureRegistry
        .adapterFor(backendIdForServer(sid))
        .supportsExperimentalMessageEdit(featureContext(sid));
  }

  public boolean supportsMessageRedaction(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return false;
    return outboundBackendFeatureRegistry
        .adapterFor(backendIdForServer(sid))
        .supportsMessageRedaction(featureContext(sid));
  }

  public String backendAvailabilityReason(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return "";
    try {
      return BackendAvailabilityReasonFormatter.decorate(
          backendIdForServer(sid),
          backendAvailability.backendAvailabilityReason(sid),
          backendMetadata);
    } catch (Exception ignored) {
      return "";
    }
  }

  public String unavailableReasonForHelp(String serverId, String fallbackReason) {
    String backendReason = backendAvailabilityReason(serverId);
    if (!backendReason.isEmpty()) return backendReason;
    return Objects.toString(fallbackReason, "").trim();
  }

  public String featureUnavailableMessage(String serverId, String fallbackMessage) {
    String backendReason = backendAvailabilityReason(serverId);
    if (!backendReason.isEmpty()) return ensureTerminalPunctuation(backendReason);
    return Objects.toString(fallbackMessage, "").trim();
  }

  private static String ensureTerminalPunctuation(String message) {
    String text = Objects.toString(message, "").trim();
    if (text.isEmpty()) return "";
    char last = text.charAt(text.length() - 1);
    if (last == '.' || last == '!' || last == '?') return text;
    return text + ".";
  }

  private static String normalizeServerId(String serverId) {
    return Objects.toString(serverId, "").trim();
  }

  private OutboundBackendFeatureContext featureContext(String serverId) {
    return new IrcNegotiatedFeatureContext(irc, serverId);
  }

  private record IrcNegotiatedFeatureContext(IrcNegotiatedFeaturePort irc, String serverId)
      implements OutboundBackendFeatureContext {

    private IrcNegotiatedFeatureContext {
      serverId = normalizeServerId(serverId);
    }

    @Override
    public boolean readMarkerAvailable() {
      return irc != null && irc.isReadMarkerAvailable(serverId);
    }

    @Override
    public boolean monitorAvailable() {
      return irc != null && irc.isMonitorAvailable(serverId);
    }

    @Override
    public boolean labeledResponseAvailable() {
      return irc != null && irc.isLabeledResponseAvailable(serverId);
    }

    @Override
    public boolean multilineAvailable() {
      return irc != null && irc.isMultilineAvailable(serverId);
    }

    @Override
    public boolean messageTagsAvailable() {
      return irc != null && irc.isMessageTagsAvailable(serverId);
    }

    @Override
    public boolean draftReplyAvailable() {
      return irc != null && irc.isDraftReplyAvailable(serverId);
    }

    @Override
    public boolean draftReactAvailable() {
      return irc != null && irc.isDraftReactAvailable(serverId);
    }

    @Override
    public boolean draftUnreactAvailable() {
      return irc != null && irc.isDraftUnreactAvailable(serverId);
    }

    @Override
    public boolean experimentalMessageEditAvailable() {
      return irc != null && irc.isExperimentalMessageEditAvailable(serverId);
    }

    @Override
    public boolean messageRedactionAvailable() {
      return irc != null && irc.isMessageRedactionAvailable(serverId);
    }
  }
}

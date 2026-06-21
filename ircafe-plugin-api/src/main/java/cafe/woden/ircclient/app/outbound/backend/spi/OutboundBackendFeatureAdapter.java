package cafe.woden.ircclient.app.outbound.backend.spi;

/** Backend-specific outbound command feature adapter. */
public interface OutboundBackendFeatureAdapter {

  default String backendId() {
    return "";
  }

  default boolean supportsSemanticUpload() {
    return false;
  }

  default boolean supportsQuasselCoreCommands() {
    return false;
  }

  default boolean persistsJoinedChannelsLocally() {
    return true;
  }

  default boolean supportsReadMarker(OutboundBackendFeatureContext context) {
    return context != null && context.readMarkerAvailable();
  }

  default boolean supportsMonitor(OutboundBackendFeatureContext context) {
    return context != null && context.monitorAvailable();
  }

  default boolean supportsLabeledResponse(OutboundBackendFeatureContext context) {
    return context != null && context.labeledResponseAvailable();
  }

  default boolean supportsMultiline(OutboundBackendFeatureContext context) {
    return context != null && context.multilineAvailable();
  }

  default boolean supportsMessageTags(OutboundBackendFeatureContext context) {
    return context != null && context.messageTagsAvailable();
  }

  default boolean supportsDraftReply(OutboundBackendFeatureContext context) {
    return context != null && context.draftReplyAvailable();
  }

  default boolean supportsDraftReact(OutboundBackendFeatureContext context) {
    return context != null && context.draftReactAvailable();
  }

  default boolean supportsDraftUnreact(OutboundBackendFeatureContext context) {
    return context != null && context.draftUnreactAvailable();
  }

  default boolean supportsExperimentalMessageEdit(OutboundBackendFeatureContext context) {
    return context != null && context.experimentalMessageEditAvailable();
  }

  default boolean supportsMessageRedaction(OutboundBackendFeatureContext context) {
    return context != null && context.messageRedactionAvailable();
  }
}

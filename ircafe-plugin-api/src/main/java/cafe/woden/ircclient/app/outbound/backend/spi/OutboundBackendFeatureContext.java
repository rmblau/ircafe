package cafe.woden.ircclient.app.outbound.backend.spi;

/** Plugin-facing negotiated feature view for backend outbound capability decisions. */
public interface OutboundBackendFeatureContext {

  String serverId();

  boolean readMarkerAvailable();

  boolean monitorAvailable();

  boolean labeledResponseAvailable();

  boolean multilineAvailable();

  boolean messageTagsAvailable();

  boolean draftReplyAvailable();

  boolean draftReactAvailable();

  boolean draftUnreactAvailable();

  boolean experimentalMessageEditAvailable();

  boolean messageRedactionAvailable();
}

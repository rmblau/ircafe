package cafe.woden.ircclient.app.outbound;

import cafe.woden.ircclient.app.outbound.backend.OutboundBackendCapabilityPolicy;
import cafe.woden.ircclient.app.outbound.support.OutboundRawLineCorrelationService;
import cafe.woden.ircclient.irc.ircv3.Ircv3ChatHistoryRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3LabeledResponseRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageMutationRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3MonitorCommandRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3RuntimeCatalogs;
import cafe.woden.ircclient.state.api.LabeledResponseRoutingPort;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/** Explicit application-classpath IRCv3 bootstrap for outbound unit tests. */
public final class TestIrcv3RuntimeSupport {

  private static final Ircv3RuntimeCatalogs CATALOGS = Ircv3RuntimeCatalogs.applicationClasspath();

  private TestIrcv3RuntimeSupport() {}

  public static Ircv3MessageMutationRuntimeCatalog messageMutations() {
    return CATALOGS.messageMutations();
  }

  public static Ircv3ChatHistoryRuntimeSupport chatHistory() {
    return new Ircv3ChatHistoryRuntimeSupport(CATALOGS.outboundCommands());
  }

  public static Ircv3MonitorCommandRuntimeSupport monitor() {
    return new Ircv3MonitorCommandRuntimeSupport(CATALOGS.outboundCommands());
  }

  public static OutboundRawLineCorrelationService rawLineCorrelation(
      OutboundBackendCapabilityPolicy backendCapabilityPolicy,
      LabeledResponseRoutingPort labeledResponseRoutingState) {
    AtomicLong sequence = new AtomicLong(System.currentTimeMillis());
    return rawLineCorrelation(
        backendCapabilityPolicy, labeledResponseRoutingState, sequence::incrementAndGet);
  }

  public static OutboundRawLineCorrelationService rawLineCorrelation(
      OutboundBackendCapabilityPolicy backendCapabilityPolicy,
      LabeledResponseRoutingPort labeledResponseRoutingState,
      LongSupplier labelSequence) {
    return new OutboundRawLineCorrelationService(
        backendCapabilityPolicy,
        labeledResponseRoutingState,
        labelSequence,
        CATALOGS.outboundCommands(),
        new Ircv3LabeledResponseRuntimeSupport(CATALOGS.inboundTags()));
  }
}

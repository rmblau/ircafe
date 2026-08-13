package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.List;
import java.util.Set;

/**
 * ServiceLoader-backed runtime interpreter for parsed inbound IRC command operations.
 *
 * <p>Higher-priority providers replace lower-priority providers for an operation; equal-priority
 * conflicts are rejected.
 */
public interface Ircv3InboundCommandSignalProvider {

  String providerId();

  default int inboundCommandPriority() {
    return 0;
  }

  Set<Ircv3InboundCommandOperation> inboundCommandOperations();

  List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request);
}

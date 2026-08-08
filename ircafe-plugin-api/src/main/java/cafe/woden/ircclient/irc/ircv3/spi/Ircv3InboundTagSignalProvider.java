package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.List;
import java.util.Set;

/**
 * ServiceLoader-backed runtime interpreter for known inbound IRCv3 tagged-message operations.
 *
 * <p>Higher-priority providers replace lower-priority providers for an operation; equal-priority
 * conflicts are rejected.
 */
public interface Ircv3InboundTagSignalProvider {

  String providerId();

  default int inboundTagPriority() {
    return 0;
  }

  Set<Ircv3InboundTagOperation> inboundTagOperations();

  List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request);
}

package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.List;
import java.util.Set;

/**
 * ServiceLoader-backed runtime renderer for outbound IRCv3 protocol commands.
 *
 * <p>A provider declares the operations it owns and returns zero or more raw IRC lines. Higher
 * priority providers replace lower-priority providers for an operation; equal-priority conflicts
 * are rejected.
 */
public interface Ircv3OutboundCommandProvider {

  String providerId();

  default int priority() {
    return 0;
  }

  Set<Ircv3OutboundCommandOperation> operations();

  List<String> build(Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request);
}

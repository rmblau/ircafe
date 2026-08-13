package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.Set;

/**
 * ServiceLoader-backed runtime renderer for outbound IRCv3 message mutations.
 *
 * <p>A provider declares the operations it owns and returns an IRC raw line for valid requests.
 * Returning an empty string rejects an incomplete or unsupported request. Higher-priority providers
 * replace lower-priority providers for the same operation; equal-priority conflicts are rejected.
 */
public interface Ircv3MessageMutationProvider {

  String providerId();

  default int priority() {
    return 0;
  }

  Set<Ircv3MessageMutationOperation> operations();

  String build(Ircv3MessageMutationOperation operation, Ircv3MessageMutationRequest request);
}

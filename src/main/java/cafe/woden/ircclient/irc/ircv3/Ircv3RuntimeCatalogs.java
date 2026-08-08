package cafe.woden.ircclient.irc.ircv3;

import java.util.Objects;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.stereotype.Component;

/** Canonical Spring-managed bundle of the installed-provider-aware IRCv3 runtime catalogs. */
@Component
@InfrastructureLayer
public record Ircv3RuntimeCatalogs(
    Ircv3InboundCommandSignalRuntimeCatalog inboundCommands,
    Ircv3InboundTagSignalRuntimeCatalog inboundTags,
    Ircv3OutboundCommandRuntimeCatalog outboundCommands,
    Ircv3MessageMutationRuntimeCatalog messageMutations,
    Ircv3MessageTagsRuntimeCatalog messageTags) {

  public Ircv3RuntimeCatalogs {
    Objects.requireNonNull(inboundCommands, "inboundCommands");
    Objects.requireNonNull(inboundTags, "inboundTags");
    Objects.requireNonNull(outboundCommands, "outboundCommands");
    Objects.requireNonNull(messageMutations, "messageMutations");
    Objects.requireNonNull(messageTags, "messageTags");
  }

  /** Compatibility bootstrap for non-Spring callers and focused tests. */
  public static Ircv3RuntimeCatalogs applicationClasspath() {
    return new Ircv3RuntimeCatalogs(
        Ircv3InboundCommandSignalRuntimeCatalog.applicationClasspath(),
        Ircv3InboundTagSignalRuntimeCatalog.applicationClasspath(),
        Ircv3OutboundCommandRuntimeCatalog.applicationClasspath(),
        Ircv3MessageMutationRuntimeCatalog.applicationClasspath(),
        Ircv3MessageTagsRuntimeCatalog.applicationClasspath());
  }
}

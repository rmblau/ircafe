package cafe.woden.ircclient.irc.pircbotx.client;

import cafe.woden.ircclient.irc.ircv3.Ircv3MessageIdRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageMutationRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageTagsRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3RuntimeCatalogs;
import cafe.woden.ircclient.irc.ircv3.Ircv3ServerTimeRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3StsPolicyService;

/** Explicit installed-provider runtime wiring for PircBotX functional launchers. */
final class PircbotxFunctionalRuntimeFixtures {

  private PircbotxFunctionalRuntimeFixtures() {}

  static Runtime runtime() {
    Ircv3RuntimeCatalogs catalogs = Ircv3RuntimeCatalogs.applicationClasspath();
    Ircv3MessageIdRuntimeSupport messageId =
        new Ircv3MessageIdRuntimeSupport(catalogs.inboundTags());
    Ircv3MessageTagsRuntimeSupport messageTags =
        new Ircv3MessageTagsRuntimeSupport(catalogs.messageTags(), messageId);
    Ircv3MessageMutationRuntimeSupport messageMutation =
        new Ircv3MessageMutationRuntimeSupport(
            catalogs.messageMutations(), catalogs.inboundTags(), catalogs.inboundCommands());
    Ircv3ServerTimeRuntimeSupport serverTime =
        new Ircv3ServerTimeRuntimeSupport(catalogs.inboundTags(), messageTags);
    return new Runtime(catalogs, serverTime, messageTags, messageMutation, messageId);
  }

  record Runtime(
      Ircv3RuntimeCatalogs catalogs,
      Ircv3ServerTimeRuntimeSupport serverTime,
      Ircv3MessageTagsRuntimeSupport messageTags,
      Ircv3MessageMutationRuntimeSupport messageMutation,
      Ircv3MessageIdRuntimeSupport messageId) {
    Ircv3StsPolicyService stsPolicyService() {
      return new Ircv3StsPolicyService(null, catalogs.inboundCommands());
    }
  }
}

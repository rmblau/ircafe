package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class Ircv3RuntimeCatalogsTest {

  @Test
  void preservesTheExplicitInstalledRuntimeCatalogSet() {
    Ircv3InboundCommandSignalRuntimeCatalog inboundCommands =
        mock(Ircv3InboundCommandSignalRuntimeCatalog.class);
    Ircv3InboundTagSignalRuntimeCatalog inboundTags =
        mock(Ircv3InboundTagSignalRuntimeCatalog.class);
    Ircv3OutboundCommandRuntimeCatalog outboundCommands =
        mock(Ircv3OutboundCommandRuntimeCatalog.class);
    Ircv3MessageMutationRuntimeCatalog messageMutations =
        mock(Ircv3MessageMutationRuntimeCatalog.class);
    Ircv3MessageTagsRuntimeCatalog messageTags = mock(Ircv3MessageTagsRuntimeCatalog.class);

    Ircv3RuntimeCatalogs catalogs =
        new Ircv3RuntimeCatalogs(
            inboundCommands, inboundTags, outboundCommands, messageMutations, messageTags);

    assertSame(inboundCommands, catalogs.inboundCommands());
    assertSame(inboundTags, catalogs.inboundTags());
    assertSame(outboundCommands, catalogs.outboundCommands());
    assertSame(messageMutations, catalogs.messageMutations());
    assertSame(messageTags, catalogs.messageTags());
  }
}

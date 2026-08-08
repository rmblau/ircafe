package cafe.woden.ircclient.irc.pircbotx.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.irc.*;
import cafe.woden.ircclient.irc.backend.*;
import cafe.woden.ircclient.irc.ircv3.*;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import cafe.woden.ircclient.irc.playback.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.pircbotx.User;

class PircbotxEventMetadataTest {

  @Test
  void withObservedHostmaskTagAddsUsefulHostmask() {
    User user = mock(User.class);
    when(user.getNick()).thenReturn("alice");
    when(user.getLogin()).thenReturn("ident");
    when(user.getHostname()).thenReturn("host.example");

    Map<String, String> tags = PircbotxEventMetadata.withObservedHostmaskTag(new HashMap<>(), user);

    assertEquals("alice!ident@host.example", tags.get("ircafe/hostmask"));
  }

  @Test
  void ircv3MessageIdPrefersStandardMsgidTag() {
    String messageId =
        PircbotxEventMetadata.ircv3MessageId(
            Map.of("znc.in/msgid", "legacy", "draft/msgid", "draft", "msgid", "modern"),
            Ircv3RuntimeTestFixtures.runtime().messageId());

    assertEquals("modern", messageId);
  }
  @Test
  void ircv3MessageIdUsesRuntimeProviderOverride() {
    Ircv3InboundTagSignalProvider provider =
        new Ircv3InboundTagSignalProvider() {
          @Override
          public String providerId() {
            return "custom-message-id";
          }

          @Override
          public Set<Ircv3InboundTagOperation> inboundTagOperations() {
            return Set.of(Ircv3InboundTagOperation.MESSAGE_ID);
          }

          @Override
          public List<Ircv3InboundTagSignal> parse(
              Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
            return List.of(
                Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.MESSAGE_ID, "custom"));
          }
        };
    Ircv3MessageIdRuntimeSupport support =
        new Ircv3MessageIdRuntimeSupport(
            Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of(provider)));

    assertEquals(
        "custom",
        PircbotxEventMetadata.ircv3MessageId(Map.of("msgid", "built-in"), support));
  }

}

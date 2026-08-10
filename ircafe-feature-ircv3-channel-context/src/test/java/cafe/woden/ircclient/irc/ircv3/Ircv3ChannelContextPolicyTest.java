package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3ChannelContextPolicyTest {

  @Test
  void channelContextOverridesDirectMessageTarget() {
    assertEquals(
        "#ircafe",
        Ircv3ChannelContextPolicy.resolveTarget(
            Map.of("+draft/channel-context", "#ircafe"), "me", "alice"));
  }

  @Test
  void channelTargetsRemainChannelsAndDirectMessagesResolveToSender() {
    assertEquals(
        "#ircafe", Ircv3ChannelContextPolicy.resolveConversationTarget("#ircafe", "alice"));
    assertEquals("alice", Ircv3ChannelContextPolicy.resolveConversationTarget("me", "alice"));
    assertEquals("me", Ircv3ChannelContextPolicy.resolveConversationTarget("me", ""));
  }

  @Test
  void recognizesStandardChannelPrefixes() {
    assertTrue(Ircv3ChannelContextPolicy.isChannelName("#ircafe"));
    assertTrue(Ircv3ChannelContextPolicy.isChannelName("&local"));
    assertTrue(Ircv3ChannelContextPolicy.isChannelName("!safe"));
    assertTrue(Ircv3ChannelContextPolicy.isChannelName("+modeless"));
    assertFalse(Ircv3ChannelContextPolicy.isChannelName("alice"));
  }
}

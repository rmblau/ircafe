package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3MessageIdTagPolicyTest {

  @Test
  void selectsStableDraftClientAndBackendAliasesInPriorityOrder() {
    assertEquals("stable", Ircv3MessageIdTagPolicy.firstMessageId(Map.of("msgid", "stable")));
    assertEquals("draft", Ircv3MessageIdTagPolicy.firstMessageId(Map.of("draft/msgid", "draft")));
    assertEquals(
        "client", Ircv3MessageIdTagPolicy.firstMessageId(Map.of("+draft/msgid", "client")));
    assertEquals(
        "backend", Ircv3MessageIdTagPolicy.firstMessageId(Map.of("znc.in/msgid", "backend")));

    LinkedHashMap<String, String> aliases = new LinkedHashMap<>();
    aliases.put("draft/msgid", "draft");
    aliases.put("msgid", "stable");
    assertEquals("stable", Ircv3MessageIdTagPolicy.firstMessageId(aliases));
  }

  @Test
  void returnsBlankWhenNoMessageIdAliasIsPresent() {
    assertEquals("", Ircv3MessageIdTagPolicy.firstMessageId(Map.of("time", "now")));
    assertEquals("", Ircv3MessageIdTagPolicy.firstMessageId(null));
  }
}

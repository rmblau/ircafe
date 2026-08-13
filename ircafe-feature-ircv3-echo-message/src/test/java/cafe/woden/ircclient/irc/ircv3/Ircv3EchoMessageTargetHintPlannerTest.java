package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3EchoMessageTargetHintPlannerTest {

  @Test
  void plansPrivateMessageHintWithDecodedMessageId() {
    Ircv3EchoMessageTargetHintPlanner.TargetHint hint =
        Ircv3EchoMessageTargetHintPlanner.plan(
                "WodenCafe",
                "alice",
                "PRIVMSG",
                "@msgid=abc\\s123 :WodenCafe!u@h PRIVMSG alice :hello there",
                List.of("alice", ":hello there"),
                Map.of("msgid", "abc\\s123"),
                List.of("wodencafe"))
            .orElseThrow();

    assertEquals("WodenCafe", hint.fromNick());
    assertEquals("alice", hint.target());
    assertEquals("PRIVMSG", hint.kind());
    assertEquals("hello there", hint.payload());
    assertEquals("abc 123", hint.messageId());
  }

  @Test
  void plansCtcpActionHint() {
    Ircv3EchoMessageTargetHintPlanner.TargetHint hint =
        Ircv3EchoMessageTargetHintPlanner.plan(
                "me",
                "bob",
                "privmsg",
                ":me!u@h PRIVMSG bob :\u0001ACTION waves\u0001",
                List.of("bob", ":\u0001ACTION waves\u0001"),
                Map.of(),
                List.of("ME"))
            .orElseThrow();

    assertEquals("ACTION", hint.kind());
    assertEquals("waves", hint.payload());
  }

  @Test
  void ignoresNonSelfChannelAndSelfTargets() {
    assertTrue(
        Ircv3EchoMessageTargetHintPlanner.plan(
                "alice", "bob", "PRIVMSG", "", List.of("bob", ":hi"), Map.of(), List.of("me"))
            .isEmpty());
    assertTrue(
        Ircv3EchoMessageTargetHintPlanner.plan(
                "me", "#ircafe", "PRIVMSG", "", List.of("#ircafe", ":hi"), Map.of(), List.of("me"))
            .isEmpty());
    assertTrue(
        Ircv3EchoMessageTargetHintPlanner.plan(
                "me", "ME", "PRIVMSG", "", List.of("ME", ":hi"), Map.of(), List.of("me"))
            .isEmpty());
  }

  @Test
  void fallsBackToTrailingRawPayload() {
    Ircv3EchoMessageTargetHintPlanner.TargetHint hint =
        Ircv3EchoMessageTargetHintPlanner.plan(
                "me",
                "carol",
                "PRIVMSG",
                ":me!u@h PRIVMSG carol :fallback text",
                List.of("carol"),
                Map.of(),
                List.of("me"))
            .orElseThrow();

    assertEquals("fallback text", hint.payload());
  }
}

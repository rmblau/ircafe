package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class SlashCommandParseResultMapperTest {

  private final SlashCommandParseResultMapper.CommandFactory<Invocation> factory =
      recordingFactory();

  @Test
  void mapsNormalizedKindAliases() {
    assertEquals(
        new Invocation("backendNamed", List.of("qsetup", "backend")),
        map(SlashCommandParseResult.command("backendnamed", "qsetup", "backend")));
    assertEquals(
        new Invocation("inviteList", List.of("libera")),
        map(SlashCommandParseResult.command("invites", "libera")));
    assertEquals(
        new Invocation("replyMessage", List.of("msgid", "text")),
        map(SlashCommandParseResult.command("reply", "msgid", "text")));
  }

  @Test
  void appliesDefaultArguments() {
    assertEquals(
        new Invocation("chatHistoryLatest", List.of(25, "*")),
        map(SlashCommandParseResult.command("chat-history-latest", "25")));
    assertEquals(
        new Invocation("kick", List.of("#ircafe", "nick", "")),
        map(SlashCommandParseResult.command("kick", "#ircafe", "nick")));
  }

  @Test
  void coercesInvalidIntegerArgumentsToZero() {
    assertEquals(
        new Invocation("whowas", List.of("nick", 0)),
        map(SlashCommandParseResult.command("whowas", "nick", "not-a-number")));
  }

  @Test
  void trimsTailArgumentsAndDropsBlankValues() {
    assertEquals(
        new Invocation("op", List.of("#ircafe", List.of("alice", "bob"))),
        map(SlashCommandParseResult.command("op", "#ircafe", "alice", " ", "bob")));
  }

  @Test
  void returnsNullForUnsupportedKinds() {
    assertNull(map(SlashCommandParseResult.command("unsupported-command", "ignored")));
    assertNull(SlashCommandParseResultMapper.map(null, factory));
  }

  private Invocation map(SlashCommandParseResult result) {
    return SlashCommandParseResultMapper.map(result, factory);
  }

  @SuppressWarnings("unchecked")
  private static SlashCommandParseResultMapper.CommandFactory<Invocation> recordingFactory() {
    return (SlashCommandParseResultMapper.CommandFactory<Invocation>)
        Proxy.newProxyInstance(
            SlashCommandParseResultMapper.CommandFactory.class.getClassLoader(),
            new Class<?>[] {SlashCommandParseResultMapper.CommandFactory.class},
            (proxy, method, args) ->
                new Invocation(
                    method.getName(),
                    List.copyOf(Arrays.asList(args == null ? new Object[0] : args))));
  }

  private record Invocation(String method, List<Object> args) {}
}

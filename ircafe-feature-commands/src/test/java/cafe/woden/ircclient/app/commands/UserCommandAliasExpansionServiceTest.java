package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class UserCommandAliasExpansionServiceTest {

  private final UserCommandAliasExpansionService service = new UserCommandAliasExpansionService();
  private final UserCommandAliasExpansionContext context =
      new UserCommandAliasExpansionContext(
          "libera", "#ircafe", "#ircafe", "me", "Mon Jan 01 00:00:00 2026", "IRCafe test", "Linux");

  @Test
  void expandsPositionalRangeAndContextPlaceholders() {
    UserCommandAliasExpansionResult result =
        service.expand(
            "/slap bob a large trout",
            List.of(
                new UserCommandAliasDefinition(
                    true, "slap", "/me slaps %1 with %2- in %c as %n")),
            context);

    assertEquals(List.of("/me slaps bob with a large trout in #ircafe as me"), result.lines());
    assertTrue(result.warnings().isEmpty());
  }

  @Test
  void expandsMultiCommandAliasesUsingSemicolonAndNewline() {
    UserCommandAliasExpansionResult result =
        service.expand(
            "/hi alice",
            List.of(
                new UserCommandAliasDefinition(
                    true, "hi", "/msg %1 hello; /notice %1 wave\\nand smile")),
            context);

    assertEquals(List.of("/msg alice hello", "/notice alice wave", "and smile"), result.lines());
  }

  @Test
  void expandsHexChatCompatibilityPlaceholders() {
    UserCommandAliasExpansionResult result =
        service.expand(
            "/meta bob alpha omega",
            List.of(
                new UserCommandAliasDefinition(
                    true,
                    "meta",
                    "/msg %1 t=%hexchat_time v=%hexchat_version m=%hexchat_machine s=%e end=&1 first=&3")),
            context);

    assertEquals(
        List.of(
            "/msg bob t=Mon Jan 01 00:00:00 2026 v=IRCafe test m=Linux s=libera end=omega first=bob"),
        result.lines());
  }

  @Test
  void detectsRecursiveAliases() {
    UserCommandAliasExpansionResult result =
        service.expand(
            "/loop",
            List.of(new UserCommandAliasDefinition(true, "loop", "/loop")),
            context);

    assertTrue(result.lines().isEmpty());
    assertTrue(result.warnings().stream().anyMatch(warning -> warning.contains("recursion")));
  }

  @Test
  void splitExpandedCommandsSupportsEscapedSemicolon() {
    assertEquals(
        List.of("/msg a one;two", "/msg b three"),
        UserCommandAliasExpansionService.splitExpandedCommands("/msg a one\\;two; /msg b three"));
  }
}

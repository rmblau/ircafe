package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SlashCommandLineSupportTest {

  @Test
  void matchesCommandCaseInsensitivelyAtTokenBoundary() {
    assertTrue(SlashCommandLineSupport.matchesCommand("/FILTER help", "/filter"));
    assertTrue(SlashCommandLineSupport.matchesCommand("/filter", "/filter"));
    assertFalse(SlashCommandLineSupport.matchesCommand("/filtering", "/filter"));
    assertFalse(SlashCommandLineSupport.matchesCommand(" /filter", "/filter"));
  }

  @Test
  void returnsTrimmedTailAfterCommandToken() {
    assertEquals("help", SlashCommandLineSupport.argAfter("/filter   help", "/filter"));
    assertEquals("", SlashCommandLineSupport.argAfter("/filter", "/filter"));
    assertEquals("", SlashCommandLineSupport.argAfter(null, "/filter"));
  }
}

package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class FilterRulePatchParserTest {

  private final FilterRulePatchParser parser = new FilterRulePatchParser();

  @Test
  void parsesWeeChatPositionalPatchWhenRegexContainsEquals() {
    FilterRulePatchSpec patch =
        parser.parseAddPatch(
            List.of("/filter", "add", "eqrule", "irc.libera.#Chan", "irc_privmsg", "foo=bar"),
            3,
            false);

    assertEquals("libera/#chan", patch.scope());
    assertTrue(patch.scopeSpecified());
    assertEquals(EnumSet.of(FilterRulePatchSpec.Kind.CHAT), patch.kinds());
    assertTrue(patch.kindsSpecified());
    assertEquals("irc_privmsg", patch.tagsExpression());
    assertTrue(patch.tagsSpecified());
    assertEquals("foo=bar", patch.textRegex().pattern());
    assertTrue(patch.textSpecified());
  }

  @Test
  void parsesKeyValuePatchIntoFeatureSafeValues() {
    FilterRulePatchSpec patch =
        parser.parseKeyValuePatch(
            List.of(
                "/filter",
                "set",
                "deploy",
                "scope=#Ops",
                "enabled=on",
                "action=highlight",
                "dir=inbound",
                "kinds=notice,error",
                "from=Alice,Bob",
                "tags=irc_notice",
                "text=/deploy\\/now/ims"),
            3);

    assertEquals("*/#ops", patch.scope());
    assertEquals(Boolean.TRUE, patch.enabled());
    assertEquals(FilterRulePatchSpec.Action.HIGHLIGHT, patch.action());
    assertEquals(FilterRulePatchSpec.Direction.IN, patch.direction());
    assertEquals(
        EnumSet.of(FilterRulePatchSpec.Kind.NOTICE, FilterRulePatchSpec.Kind.ERROR), patch.kinds());
    assertEquals(List.of("Alice", "Bob"), patch.from());
    assertEquals("irc_notice", patch.tagsExpression());
    assertEquals("deploy/now", patch.textRegex().pattern());
    assertEquals(
        EnumSet.of(
            FilterRulePatchSpec.RegexFlag.I,
            FilterRulePatchSpec.RegexFlag.M,
            FilterRulePatchSpec.RegexFlag.S),
        patch.textRegex().flags());
  }

  @Test
  void parsesGlobAndClearValuesWithoutRootRegexTypes() {
    FilterRulePatchSpec glob =
        parser.parseKeyValuePatch(List.of("/filter", "set", "g", "glob=foo*bar?"), 3);
    assertEquals("foo.*bar.", glob.textRegex().pattern());
    assertEquals(EnumSet.of(FilterRulePatchSpec.RegexFlag.I), glob.textRegex().flags());

    FilterRulePatchSpec cleared =
        parser.parseKeyValuePatch(
            List.of("/filter", "set", "g", "kind=", "from=", "text="), 3);
    assertTrue(cleared.kinds().isEmpty());
    assertTrue(cleared.kindsSpecified());
    assertTrue(cleared.from().isEmpty());
    assertTrue(cleared.fromSpecified());
    assertEquals("", cleared.textRegex().pattern());
    assertTrue(cleared.textSpecified());
  }

  @Test
  void rejectsInvalidPatchTokensWithExistingMessages() {
    IllegalArgumentException invalidBoolean =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                parser.parseKeyValuePatch(
                    List.of("/filter", "set", "bad", "enabled=perhaps"), 3));
    assertEquals("Invalid boolean for enabled=: 'perhaps'", invalidBoolean.getMessage());

    IllegalArgumentException unknownKey =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                parser.parseKeyValuePatch(
                    List.of("/filter", "set", "bad", "colour=blue"), 3));
    assertTrue(unknownKey.getMessage().startsWith("Unknown key: 'colour'."));
  }

  @Test
  void rejectsAmbiguousAddShapeWithCommandSpecificUsage() {
    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                parser.parseAddPatch(
                    List.of("/filter", "addreplace", "rule", "scope", "tag"), 3, true));

    assertTrue(error.getMessage().startsWith("Usage: /filter addreplace"));
    assertFalse(error.getMessage().contains("key=value ... (or:"));
  }
}

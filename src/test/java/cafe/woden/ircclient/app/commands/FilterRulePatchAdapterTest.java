package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.FilterDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.RegexFlag;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class FilterRulePatchAdapterTest {

  private final FilterRulePatchAdapter adapter = new FilterRulePatchAdapter();

  @Test
  void mapsFeaturePatchValuesIntoRootFilterModel() {
    FilterRulePatchSpec source =
        new FilterRulePatchSpec(
            "libera/#irc",
            true,
            Boolean.TRUE,
            true,
            FilterRulePatchSpec.Action.DIM,
            true,
            FilterRulePatchSpec.Direction.OUT,
            true,
            EnumSet.of(FilterRulePatchSpec.Kind.CHAT, FilterRulePatchSpec.Kind.ACTION),
            true,
            List.of("alice"),
            true,
            "irc_privmsg",
            true,
            new FilterRulePatchSpec.RegexPattern(
                "ping", EnumSet.of(FilterRulePatchSpec.RegexFlag.I)),
            true);

    FilterCommand.FilterRulePatch mapped = adapter.toRoot(source);

    assertEquals("libera/#irc", mapped.scope());
    assertEquals(Boolean.TRUE, mapped.enabled());
    assertEquals(FilterAction.DIM, mapped.action());
    assertEquals(FilterDirection.OUT, mapped.direction());
    assertEquals(EnumSet.of(LogKind.CHAT, LogKind.ACTION), mapped.kinds());
    assertEquals(List.of("alice"), mapped.from());
    assertEquals("irc_privmsg", mapped.tagsExpr());
    assertEquals("ping", mapped.textRegex().pattern());
    assertEquals(EnumSet.of(RegexFlag.I), mapped.textRegex().flags());
  }

  @Test
  void mapsNullSourceToEmptyPatch() {
    FilterCommand.FilterRulePatch mapped = adapter.toRoot(null);

    assertEquals("", mapped.scope());
    assertTrue(mapped.kinds().isEmpty());
    assertTrue(mapped.from().isEmpty());
    assertNull(mapped.action());
    assertNull(mapped.direction());
    assertNull(mapped.textRegex());
  }
}

package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.FilterAction;
import org.junit.jupiter.api.Test;

class FilterCommandParserTest {

  private final FilterCommandParser parser = new FilterCommandParser();

  @Test
  void parsesWeeChatPositionalAddWhenRegexContainsEquals() {
    FilterCommand cmd = parser.parse("/filter add eqrule irc.libera.#chan irc_privmsg foo=bar");
    FilterCommand.Add add = assertInstanceOf(FilterCommand.Add.class, cmd);

    assertEquals("eqrule", add.name());
    assertTrue(add.patch().scopeSpecified());
    assertEquals("libera/#chan", add.patch().scope());
    assertTrue(add.patch().textSpecified());
    assertEquals("foo=bar", add.patch().textRegex().pattern());
  }

  @Test
  void stillParsesKeyValueAddForm() {
    FilterCommand cmd =
        parser.parse("/filter add named scope=libera/#chan tags=irc_privmsg text=foo");
    FilterCommand.Add add = assertInstanceOf(FilterCommand.Add.class, cmd);

    assertEquals("named", add.name());
    assertTrue(add.patch().scopeSpecified());
    assertEquals("libera/#chan", add.patch().scope());
    assertTrue(add.patch().tagsSpecified());
    assertEquals("irc_privmsg", add.patch().tagsExpr());
    assertTrue(add.patch().textSpecified());
    assertEquals("foo", add.patch().textRegex().pattern());
  }

  @Test
  void parsesExtendedFilterActions() {
    FilterCommand dimCmd = parser.parse("/filter add dimmer scope=*/#irc action=dim text=ping");
    FilterCommand.Add dimAdd = assertInstanceOf(FilterCommand.Add.class, dimCmd);
    assertTrue(dimAdd.patch().actionSpecified());
    assertEquals(FilterAction.DIM, dimAdd.patch().action());

    FilterCommand hlCmd =
        parser.parse("/filter add attention scope=*/#irc action=highlight text=deploy");
    FilterCommand.Add hlAdd = assertInstanceOf(FilterCommand.Add.class, hlCmd);
    assertTrue(hlAdd.patch().actionSpecified());
    assertEquals(FilterAction.HIGHLIGHT, hlAdd.patch().action());
  }

  @Test
  void clampsPlaceholderTuningCommands() {
    FilterCommand preview = parser.parse("/filter placeholder-preview 99");
    FilterCommand.PlaceholderPreview placeholderPreview =
        assertInstanceOf(FilterCommand.PlaceholderPreview.class, preview);
    assertEquals(25, placeholderPreview.maxLines());

    FilterCommand defaults =
        parser.parse("/filter defaults preview=-1 maxrun=999999 maxtags=999 maxbatch=9999");
    FilterCommand.Defaults parsedDefaults =
        assertInstanceOf(FilterCommand.Defaults.class, defaults);
    assertEquals(0, parsedDefaults.placeholderMaxPreviewLines());
    assertEquals(50_000, parsedDefaults.placeholderMaxLinesPerRun());
    assertEquals(500, parsedDefaults.placeholderTooltipMaxTags());
    assertEquals(5_000, parsedDefaults.historyPlaceholderMaxRunsPerBatch());
  }
}

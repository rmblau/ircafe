package cafe.woden.ircclient.notify.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NotificationTextMatchSnippetPlannerTest {

  @Test
  void returnsEmptySnippetForNullMessage() {
    assertEquals("", NotificationTextMatchSnippetPlanner.snippetAround(null, 0, 4));
  }

  @Test
  void bracketsMatchedTextAndKeepsNearbyContext() {
    assertEquals(
        "alice says [ping] in #ircafe",
        NotificationTextMatchSnippetPlanner.snippetAround("alice says ping in #ircafe", 11, 15));
  }

  @Test
  void clampsInvalidMatchBoundsToTheMessage() {
    assertEquals("[short]", NotificationTextMatchSnippetPlanner.snippetAround("short", -10, 99));
    assertEquals("short[]", NotificationTextMatchSnippetPlanner.snippetAround("short", 99, 2));
  }

  @Test
  void truncatesLongContextWithEllipses() {
    String message = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    assertEquals(
        "…vwxyz[ABC]DEFGH…",
        NotificationTextMatchSnippetPlanner.snippetAround(message, 36, 39, 5));
  }

  @Test
  void collapsesWhitespaceInsideSnippetSegments() {
    String message = "alpha\n\tbeta   ping\r\n gamma\t\tdelta";
    int start = message.indexOf("ping");

    assertEquals(
        "alpha beta [ping] gamma delta",
        NotificationTextMatchSnippetPlanner.snippetAround(message, start, start + "ping".length()));
  }
}

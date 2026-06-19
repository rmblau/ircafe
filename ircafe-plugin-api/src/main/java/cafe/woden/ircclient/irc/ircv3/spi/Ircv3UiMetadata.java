package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.Objects;

/** Plugin-facing UI metadata for an IRCv3 extension contribution. */
public record Ircv3UiMetadata(
    String label, Ircv3UiGroup group, int sortOrder, String impactSummary) {

  public Ircv3UiMetadata {
    label = Objects.toString(label, "").trim();
    group = Objects.requireNonNullElse(group, Ircv3UiGroup.OTHER);
    impactSummary = Objects.toString(impactSummary, "").trim();
  }
}

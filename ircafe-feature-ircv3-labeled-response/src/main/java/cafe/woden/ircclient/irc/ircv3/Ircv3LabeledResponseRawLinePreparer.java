package cafe.woden.ircclient.irc.ircv3;

import java.util.Objects;
import java.util.function.Supplier;

/** Adds or preserves the IRCv3 {@code label=} tag on an outbound raw IRC line. */
public final class Ircv3LabeledResponseRawLinePreparer {

  private Ircv3LabeledResponseRawLinePreparer() {}

  public static PreparedRawLine prepare(String rawLine, Supplier<String> generatedLabelSupplier) {
    String line = Objects.toString(rawLine, "").trim();
    if (line.isEmpty()) return new PreparedRawLine("", "", false);

    if (line.charAt(0) != '@') {
      String label = nextLabel(generatedLabelSupplier);
      if (label.isEmpty()) return new PreparedRawLine(line, "", false);
      return new PreparedRawLine(
          "@label=" + Ircv3CommandValuePolicy.escapeTagValue(label) + " " + line, label, true);
    }

    int space = line.indexOf(' ');
    if (space <= 1) return new PreparedRawLine(line, "", false);

    String existing = Ircv3Tags.firstTagValue(Ircv3Tags.fromRawLine(line), "label", "+label");
    existing = Ircv3LabeledResponseValues.normalizeLabel(existing);
    if (!existing.isEmpty()) return new PreparedRawLine(line, existing, false);

    String label = nextLabel(generatedLabelSupplier);
    if (label.isEmpty()) return new PreparedRawLine(line, "", false);
    String withLabel =
        "@"
            + line.substring(1, space)
            + ";label="
            + Ircv3CommandValuePolicy.escapeTagValue(label)
            + line.substring(space);
    return new PreparedRawLine(withLabel, label, true);
  }

  private static String nextLabel(Supplier<String> supplier) {
    if (supplier == null) return "";
    return Ircv3LabeledResponseValues.normalizeLabel(supplier.get());
  }

  public record PreparedRawLine(String line, String label, boolean injected) {
    public PreparedRawLine {
      line = Objects.toString(line, "").trim();
      label = Ircv3LabeledResponseValues.normalizeLabel(label);
    }
  }
}

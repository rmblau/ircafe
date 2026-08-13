package cafe.woden.ircclient.app.commands;

import java.util.ArrayList;
import java.util.List;

/** Shared shell-like tokenizer for slash command input. */
public final class CommandLineTokenizer {
  private CommandLineTokenizer() {}

  /** Tokenizes with support for single and double quotes and backslash escapes. */
  public static List<String> tokenize(String line) {
    List<String> out = new ArrayList<>();
    StringBuilder cur = new StringBuilder();
    boolean inSingle = false;
    boolean inDouble = false;
    boolean escaping = false;

    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);

      if (escaping) {
        switch (c) {
          case 'n' -> cur.append('\n');
          case 't' -> cur.append('\t');
          case '\\' -> cur.append('\\');
          case '\'' -> cur.append('\'');
          case '"' -> cur.append('"');
          default -> cur.append(c);
        }
        escaping = false;
        continue;
      }

      if (c == '\\') {
        escaping = true;
        continue;
      }

      if (inSingle) {
        if (c == '\'') {
          inSingle = false;
        } else {
          cur.append(c);
        }
        continue;
      }
      if (inDouble) {
        if (c == '"') {
          inDouble = false;
        } else {
          cur.append(c);
        }
        continue;
      }

      if (c == '\'') {
        inSingle = true;
        continue;
      }
      if (c == '"') {
        inDouble = true;
        continue;
      }

      if (Character.isWhitespace(c)) {
        if (!cur.isEmpty()) {
          out.add(cur.toString());
          cur.setLength(0);
        }
        continue;
      }

      cur.append(c);
    }

    if (escaping) {
      throw new IllegalArgumentException("Dangling escape at end of line.");
    }
    if (inSingle || inDouble) {
      throw new IllegalArgumentException("Unterminated quoted string.");
    }

    if (!cur.isEmpty()) out.add(cur.toString());
    return out;
  }
}

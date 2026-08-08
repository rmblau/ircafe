package cafe.woden.ircclient.irc.ircv3;

/** Parses a raw IRC line into the pieces needed by the SASL capability handler. */
public record Ircv3SaslIrcLine(String command, String trailing) {

  public boolean isNumeric() {
    return command != null
        && command.length() == 3
        && Character.isDigit(command.charAt(0))
        && Character.isDigit(command.charAt(1))
        && Character.isDigit(command.charAt(2));
  }

  public int numeric() {
    return Integer.parseInt(command);
  }

  public static Ircv3SaslIrcLine parse(String raw) {
    if (raw == null) {
      return null;
    }

    String line = raw;
    if (line.startsWith("@")) {
      int sp = line.indexOf(' ');
      if (sp > 0 && sp + 1 < line.length()) {
        line = line.substring(sp + 1);
      }
    }

    if (line.startsWith(":")) {
      int sp = line.indexOf(' ');
      if (sp > 0) {
        line = line.substring(sp + 1);
      }
    }
    line = line.trim();
    if (line.isEmpty()) {
      return null;
    }

    String trailing = "";
    int trailIdx = line.indexOf(" :");
    if (trailIdx >= 0) {
      trailing = line.substring(trailIdx + 2);
      line = line.substring(0, trailIdx);
    }

    String[] parts = line.split("\\s+");
    if (parts.length == 0) {
      return null;
    }

    String cmd = parts[0];
    if ("AUTHENTICATE".equalsIgnoreCase(cmd)) {
      String arg = (parts.length >= 2) ? parts[1] : "";
      return new Ircv3SaslIrcLine(cmd, arg);
    }

    return new Ircv3SaslIrcLine(cmd, trailing);
  }
}

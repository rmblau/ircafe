package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Validates runtime-provider MONITOR commands before application transport writes. */
@Component
@InfrastructureLayer
public final class Ircv3MonitorCommandRuntimeSupport {

  private static final int MAX_RAW_LINE_LENGTH = 4096;

  private final Ircv3OutboundCommandRuntimeCatalog catalog;

  @Autowired
  public Ircv3MonitorCommandRuntimeSupport(Ircv3OutboundCommandRuntimeCatalog catalog) {
    this.catalog = Objects.requireNonNull(catalog, "catalog");
  }

  public String listCommand() {
    return buildSingle(Ircv3OutboundCommandOperation.MONITOR_LIST);
  }

  public String statusCommand() {
    return buildSingle(Ircv3OutboundCommandOperation.MONITOR_STATUS);
  }

  public String clearCommand() {
    return buildSingle(Ircv3OutboundCommandOperation.MONITOR_CLEAR);
  }

  public List<String> addCommands(List<String> nicks, int negotiatedLimit) {
    return buildModification(Ircv3OutboundCommandOperation.MONITOR_ADD, nicks, negotiatedLimit);
  }

  public List<String> removeCommands(List<String> nicks, int negotiatedLimit) {
    return buildModification(Ircv3OutboundCommandOperation.MONITOR_REMOVE, nicks, negotiatedLimit);
  }

  private String buildSingle(Ircv3OutboundCommandOperation operation) {
    List<String> lines = validatedLines(operation, List.of(), 0);
    return lines.size() == 1 ? lines.getFirst() : "";
  }

  private List<String> buildModification(
      Ircv3OutboundCommandOperation operation, List<String> nicks, int negotiatedLimit) {
    List<String> normalizedNicks = normalizeNicks(nicks);
    if (normalizedNicks.isEmpty()) {
      return List.of();
    }
    List<String> lines = validatedLines(operation, normalizedNicks, negotiatedLimit);
    return lines.size() <= normalizedNicks.size() ? lines : List.of();
  }

  private List<String> validatedLines(
      Ircv3OutboundCommandOperation operation, List<String> nicks, int negotiatedLimit) {
    List<String> rendered =
        catalog.build(operation, Ircv3OutboundCommandRequest.monitor(nicks, negotiatedLimit));
    if (rendered.isEmpty()) {
      return List.of();
    }
    ArrayList<String> accepted = new ArrayList<>(rendered.size());
    for (String rawLine : rendered) {
      String line = Objects.toString(rawLine, "");
      if (!validRawLine(line)) {
        return List.of();
      }
      accepted.add(line);
    }
    return List.copyOf(accepted);
  }

  private static List<String> normalizeNicks(List<String> rawNicks) {
    if (rawNicks == null || rawNicks.isEmpty()) {
      return List.of();
    }
    ArrayList<String> nicks = new ArrayList<>(rawNicks.size());
    for (String rawNick : rawNicks) {
      String nick = Objects.toString(rawNick, "").trim();
      if (!nick.isEmpty()) {
        nicks.add(nick);
      }
    }
    return List.copyOf(nicks);
  }

  private static boolean validRawLine(String rawLine) {
    if (rawLine.isBlank() || rawLine.length() > MAX_RAW_LINE_LENGTH) {
      return false;
    }
    for (int i = 0; i < rawLine.length(); i++) {
      char ch = rawLine.charAt(i);
      if (ch == '\r' || ch == '\n' || ch == '\0') {
        return false;
      }
    }
    return true;
  }
}

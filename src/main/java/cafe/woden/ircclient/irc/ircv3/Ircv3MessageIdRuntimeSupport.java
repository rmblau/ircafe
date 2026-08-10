package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validates runtime-provider message IDs before root transports or application state consume them.
 */
@Component
@InfrastructureLayer
public final class Ircv3MessageIdRuntimeSupport {

  private static final int MAX_MESSAGE_ID_LENGTH = 1024;

  private final Ircv3InboundTagSignalRuntimeCatalog catalog;

  @Autowired
  public Ircv3MessageIdRuntimeSupport(Ircv3InboundTagSignalRuntimeCatalog catalog) {
    this.catalog = Objects.requireNonNull(catalog, "catalog");
  }

  public String resolve(Map<String, String> tags) {
    return resolve(tags, "");
  }

  public String resolve(Map<String, String> tags, String fallbackMessageId) {
    Ircv3InboundTagRequest request =
        new Ircv3InboundTagRequest("", "", "", List.of(), tags == null ? Map.of() : tags);
    String accepted = "";
    for (Ircv3InboundTagSignal signal :
        catalog.parse(Ircv3InboundTagOperation.MESSAGE_ID, request)) {
      if (signal.type() != Ircv3InboundTagSignalType.MESSAGE_ID) {
        continue;
      }
      String candidate = normalize(signal.primaryValue());
      if (candidate.isEmpty() || !accepted.isEmpty()) {
        return "";
      }
      accepted = candidate;
    }
    return accepted.isEmpty() ? normalize(fallbackMessageId) : accepted;
  }

  private static String normalize(String raw) {
    String value = Objects.toString(raw, "").trim();
    if (value.isEmpty() || value.length() > MAX_MESSAGE_ID_LENGTH) {
      return "";
    }
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (Character.isWhitespace(ch) || Character.isISOControl(ch)) {
        return "";
      }
    }
    return value;
  }
}

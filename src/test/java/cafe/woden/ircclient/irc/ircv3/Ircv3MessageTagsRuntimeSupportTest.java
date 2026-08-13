package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParseRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParseResult;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParserProvider;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3MessageTagsRuntimeSupportTest {

  @Test
  void eventAdapterSuppliesTransportMapAndRawLineToRuntimeProvider() {
    Ircv3MessageTagParserProvider provider =
        new Ircv3MessageTagParserProvider() {
          @Override
          public String providerId() {
            return "test";
          }

          @Override
          public Ircv3MessageTagParseResult parse(Ircv3MessageTagParseRequest request) {
            return new Ircv3MessageTagParseResult(
                Map.of(
                    "map-msgid", request.transportTags().getOrDefault("@MsgId", ""),
                    "raw", request.rawLine()));
          }
        };
    Ircv3MessageTagsRuntimeSupport support =
        new Ircv3MessageTagsRuntimeSupport(
            Ircv3MessageTagsRuntimeCatalog.fromProviders(List.of(provider)),
            new Ircv3MessageIdRuntimeSupport(
                Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of())));

    Map<String, String> tags = support.fromEvent(new TaggedEvent());

    assertEquals("event-7", tags.get("map-msgid"));
    assertEquals("@label=raw-7 :server NOTICE nick :hello", tags.get("raw"));
  }

  private static final class TaggedEvent {
    public Map<String, String> getTags() {
      LinkedHashMap<String, String> tags = new LinkedHashMap<>();
      tags.put("@MsgId", "event-7");
      return tags;
    }

    public String getRawLine() {
      return "@label=raw-7 :server NOTICE nick :hello";
    }
  }
}

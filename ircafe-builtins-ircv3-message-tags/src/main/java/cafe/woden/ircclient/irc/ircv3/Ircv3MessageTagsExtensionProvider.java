package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParseRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParseResult;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParserProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Map;

/** Built-in metadata and transport parser for the IRCv3 message-tags capability. */
@AutoService({Ircv3ExtensionProvider.class, Ircv3MessageTagParserProvider.class})
public final class Ircv3MessageTagsExtensionProvider
    implements Ircv3ExtensionProvider, Ircv3MessageTagParserProvider {

  private static final String CAPABILITY = "message-tags";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 100;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        capability(
            CAPABILITY,
            "Message tags",
            10,
            "Foundation for many IRCv3 features: carries structured metadata on messages."));
  }

  @Override
  public Ircv3MessageTagParseResult parse(Ircv3MessageTagParseRequest request) {
    if (request == null) {
      return Ircv3MessageTagParseResult.empty();
    }
    Map<String, String> tags = Ircv3Tags.fromMap(request.transportTags());
    if (tags.isEmpty()) {
      tags = Ircv3Tags.fromRawLine(request.rawLine());
    }
    return new Ircv3MessageTagParseResult(tags);
  }

  private static Ircv3ExtensionContribution capability(
      String id, String label, int sortOrder, String impactSummary) {
    return new Ircv3ExtensionContribution(
        id,
        Ircv3ExtensionKind.CAPABILITY,
        Ircv3SpecStatus.STABLE,
        List.of(),
        id,
        id,
        new Ircv3UiMetadata(label, Ircv3UiGroup.CORE, sortOrder, impactSummary));
  }
}

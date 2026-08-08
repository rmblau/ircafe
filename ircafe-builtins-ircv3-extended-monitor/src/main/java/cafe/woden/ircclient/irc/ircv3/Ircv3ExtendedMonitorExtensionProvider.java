package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import com.google.auto.service.AutoService;
import java.util.List;

/** Built-in metadata provider for the IRCv3 extended-monitor capability. */
@AutoService(Ircv3ExtensionProvider.class)
public final class Ircv3ExtendedMonitorExtensionProvider implements Ircv3ExtensionProvider {

  private static final String CAPABILITY = "extended-monitor";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 131;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        new Ircv3ExtensionContribution(
            CAPABILITY,
            Ircv3ExtensionKind.CAPABILITY,
            Ircv3SpecStatus.STABLE,
            List.of("draft/extended-monitor"),
            CAPABILITY,
            CAPABILITY,
            new Ircv3UiMetadata(
                "Extended MONITOR",
                Ircv3UiGroup.CORE,
                160,
                "Extends MONITOR presence notifications to additional events.")));
  }
}

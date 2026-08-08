package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Set;

/** Built-in metadata and runtime provider for setname. */
@AutoService({Ircv3ExtensionProvider.class, Ircv3InboundCommandSignalProvider.class})
public final class Ircv3SetnameExtensionProvider
    implements Ircv3ExtensionProvider, Ircv3InboundCommandSignalProvider {

  private static final String CAPABILITY = "setname";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 124;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        capability(
            CAPABILITY,
            "Setname updates",
            120,
            "Receives user real-name changes without extra lookups."));
  }

  @Override
  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
    return Set.of(Ircv3InboundCommandOperation.SETNAME);
  }

  @Override
  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (operation != Ircv3InboundCommandOperation.SETNAME || request == null) {
      return List.of();
    }

    return Ircv3SetnameParser.parse(request.command(), request.parameters())
        .map(
            change ->
                List.<Ircv3InboundCommandSignal>of(
                    new Ircv3InboundCommandSignal.SetNameObserved(
                        request.sourceNick(),
                        "",
                        change.realName(),
                        Ircv3InboundCommandSignal.SetNameSource.SETNAME)))
        .orElseGet(List::of);
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

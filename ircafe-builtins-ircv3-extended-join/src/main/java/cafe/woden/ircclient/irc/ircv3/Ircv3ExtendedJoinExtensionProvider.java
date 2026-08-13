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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Built-in metadata and runtime provider for extended-join. */
@AutoService({Ircv3ExtensionProvider.class, Ircv3InboundCommandSignalProvider.class})
public final class Ircv3ExtendedJoinExtensionProvider
    implements Ircv3ExtensionProvider, Ircv3InboundCommandSignalProvider {

  private static final String CAPABILITY = "extended-join";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 122;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        capability(
            CAPABILITY,
            "Extended join data",
            100,
            "Adds account/realname metadata to join events when available."));
  }

  @Override
  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
    return Set.of(Ircv3InboundCommandOperation.EXTENDED_JOIN);
  }

  @Override
  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (operation != Ircv3InboundCommandOperation.EXTENDED_JOIN || request == null) {
      return List.of();
    }

    return Ircv3ExtendedJoinSignalParser.parse(
            request.sourceNick(), request.command(), request.parameters())
        .map(
            observed -> {
              ArrayList<Ircv3InboundCommandSignal> signals = new ArrayList<>(2);
              Ircv3InboundCommandSignal.AccountState state =
                  observed.accountState() == Ircv3ExtendedJoinSignalParser.AccountState.LOGGED_IN
                      ? Ircv3InboundCommandSignal.AccountState.LOGGED_IN
                      : Ircv3InboundCommandSignal.AccountState.LOGGED_OUT;
              signals.add(
                  new Ircv3InboundCommandSignal.AccountObserved(
                      observed.nick(), state, observed.accountName()));
              if (observed.realName() != null) {
                signals.add(
                    new Ircv3InboundCommandSignal.SetNameObserved(
                        observed.nick(),
                        observed.channel(),
                        observed.realName(),
                        Ircv3InboundCommandSignal.SetNameSource.EXTENDED_JOIN));
              }
              return List.copyOf(signals);
            })
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

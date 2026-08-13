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

/** Built-in metadata and runtime provider for account-notify. */
@AutoService({Ircv3ExtensionProvider.class, Ircv3InboundCommandSignalProvider.class})
public final class Ircv3AccountNotifyExtensionProvider
    implements Ircv3ExtensionProvider, Ircv3InboundCommandSignalProvider {

  private static final String CAPABILITY = "account-notify";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 120;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        capability(
            CAPABILITY,
            "Account status updates",
            80,
            "Tracks account login/logout changes for users."));
  }

  @Override
  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
    return Set.of(Ircv3InboundCommandOperation.ACCOUNT_NOTIFY);
  }

  @Override
  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (operation != Ircv3InboundCommandOperation.ACCOUNT_NOTIFY || request == null) {
      return List.of();
    }

    return Ircv3AccountNotifySignalParser.parse(
            request.sourceNick(), request.command(), request.rawLine(), request.parameters())
        .map(
            observed -> {
              ArrayList<Ircv3InboundCommandSignal> signals = new ArrayList<>(2);
              if (!observed.hostmask().isBlank()) {
                signals.add(
                    new Ircv3InboundCommandSignal.HostmaskObserved(
                        observed.nick(), observed.hostmask()));
              }
              Ircv3InboundCommandSignal.AccountState state =
                  observed.state() == Ircv3AccountNotifySignalParser.AccountState.LOGGED_IN
                      ? Ircv3InboundCommandSignal.AccountState.LOGGED_IN
                      : Ircv3InboundCommandSignal.AccountState.LOGGED_OUT;
              signals.add(
                  new Ircv3InboundCommandSignal.AccountObserved(
                      observed.nick(), state, observed.accountName()));
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

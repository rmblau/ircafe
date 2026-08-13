package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3AccountTagRuntimeSupportTest {

  @Test
  void acceptsOneSafeObservationThatPreservesTheSourceNick() {
    Ircv3AccountTagRuntimeSupport support = support(account("alice", "plugin-account"));

    assertEquals(
        new Ircv3AccountTagRuntimeSupport.Observation("alice", "plugin-account"),
        support
            .observe(
                new Ircv3InboundTagRequest(
                    "PRIVMSG", "alice", "#ircafe", List.of(), Map.of("account", "wire-account")))
            .orElseThrow());
  }

  @Test
  void preservesBlankLogoutValues() {
    Ircv3AccountTagRuntimeSupport support = support(account("alice", ""));

    assertEquals(
        "",
        support
            .observe(
                new Ircv3InboundTagRequest(
                    "PRIVMSG", "alice", "#ircafe", List.of(), Map.of("account", "")))
            .orElseThrow()
            .rawAccount());
  }

  @Test
  void rejectsMissingTagsChangedNicksUnsafeAccountsAndAmbiguousOutput() {
    Ircv3InboundTagRequest request =
        new Ircv3InboundTagRequest(
            "PRIVMSG", "alice", "#ircafe", List.of(), Map.of("account", "wire"));

    assertTrue(
        support(account("alice", "plugin"))
            .observe(
                new Ircv3InboundTagRequest(
                    "PRIVMSG", "alice", "#ircafe", List.of(), Map.of("msgid", "1")))
            .isEmpty());
    assertTrue(support(account("mallory", "plugin")).observe(request).isEmpty());
    assertTrue(support(account("alice", "bad\naccount")).observe(request).isEmpty());
    assertTrue(
        support(List.of(accountSignal("alice", "one"), accountSignal("alice", "two")))
            .observe(request)
            .isEmpty());
  }

  private static Ircv3AccountTagRuntimeSupport support(List<Ircv3InboundTagSignal> signals) {
    Ircv3InboundTagSignalProvider provider =
        new Ircv3InboundTagSignalProvider() {
          @Override
          public String providerId() {
            return "account-tag-test";
          }

          @Override
          public Set<Ircv3InboundTagOperation> inboundTagOperations() {
            return Set.of(Ircv3InboundTagOperation.ACCOUNT_TAG);
          }

          @Override
          public List<Ircv3InboundTagSignal> parse(
              Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
            return signals;
          }
        };
    return new Ircv3AccountTagRuntimeSupport(
        Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of(provider)));
  }

  private static List<Ircv3InboundTagSignal> account(String nick, String account) {
    return List.of(accountSignal(nick, account));
  }

  private static Ircv3InboundTagSignal accountSignal(String nick, String account) {
    return new Ircv3InboundTagSignal(Ircv3InboundTagSignalType.ACCOUNT_TAG, nick, account);
  }
}

package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParserProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3BuiltInProviderContractTest {

  @Test
  void applicationClasspathProvidersSatisfySharedContracts() {
    assertProviderIds(Ircv3ExtensionRegistry.defaultProviders());
    assertProviderIds(Ircv3OutboundCommandRuntimeCatalog.loadApplicationProviders());
    assertProviderIds(Ircv3InboundTagSignalRuntimeCatalog.loadApplicationProviders());
    assertProviderIds(Ircv3InboundCommandSignalRuntimeCatalog.loadApplicationProviders());
    assertProviderIds(Ircv3MessageMutationRuntimeCatalog.loadApplicationProviders());
    assertProviderIds(Ircv3MessageTagsRuntimeCatalog.loadApplicationProviders());

    for (Ircv3ExtensionProvider provider : Ircv3ExtensionRegistry.defaultProviders()) {
      List<Ircv3ExtensionContribution> contributions = provider.extensions();
      assertNotNull(contributions, provider.providerId() + " extensions");
      for (Ircv3ExtensionContribution contribution : contributions) {
        assertNotNull(contribution, provider.providerId() + " contribution");
        assertFalse(contribution.id().isBlank(), provider.providerId() + " contribution id");
        assertNotNull(contribution.uiMetadata(), provider.providerId() + " UI metadata");
      }
    }

    for (Ircv3OutboundCommandProvider provider :
        Ircv3OutboundCommandRuntimeCatalog.loadApplicationProviders()) {
      assertOperations(provider.providerId(), provider.operations());
    }
    for (Ircv3InboundTagSignalProvider provider :
        Ircv3InboundTagSignalRuntimeCatalog.loadApplicationProviders()) {
      assertOperations(provider.providerId(), provider.inboundTagOperations());
    }
    for (Ircv3InboundCommandSignalProvider provider :
        Ircv3InboundCommandSignalRuntimeCatalog.loadApplicationProviders()) {
      assertOperations(provider.providerId(), provider.inboundCommandOperations());
    }
    for (Ircv3MessageMutationProvider provider :
        Ircv3MessageMutationRuntimeCatalog.loadApplicationProviders()) {
      assertOperations(provider.providerId(), provider.operations());
    }
  }

  private static void assertProviderIds(List<?> providers) {
    Set<String> providerClasses = new HashSet<>();
    Set<String> providerIds = new HashSet<>();
    for (Object provider : providers) {
      assertNotNull(provider, "provider");
      assertTrue(providerClasses.add(provider.getClass().getName()), "duplicate provider class");
      String providerId =
          switch (provider) {
            case Ircv3ExtensionProvider value -> value.providerId();
            case Ircv3OutboundCommandProvider value -> value.providerId();
            case Ircv3InboundTagSignalProvider value -> value.providerId();
            case Ircv3InboundCommandSignalProvider value -> value.providerId();
            case Ircv3MessageMutationProvider value -> value.providerId();
            case Ircv3MessageTagParserProvider value -> value.providerId();
            default -> "";
          };
      assertFalse(providerId == null || providerId.isBlank(), provider.getClass().getName());
      assertTrue(
          providerIds.add(providerId.trim().toLowerCase(Locale.ROOT)),
          "duplicate provider id: " + providerId);
    }
  }

  private static void assertOperations(String providerId, Set<?> operations) {
    assertNotNull(operations, providerId + " operations");
    assertFalse(operations.isEmpty(), providerId + " operations");
    assertFalse(operations.stream().anyMatch(Objects::isNull), providerId + " operations");
  }
}

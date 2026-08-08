package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3ChatHistoryRuntimeSupportTest {

  @Test
  void builtInProviderNormalizesAllOperations() {
    Ircv3ChatHistoryRuntimeSupport support = Ircv3RuntimeTestFixtures.chatHistory();

    Ircv3ChatHistoryRuntimeSupport.Plan before =
        support.before("#ircafe", "", 500, Instant.parse("2026-07-13T12:34:56Z"));
    Ircv3ChatHistoryRuntimeSupport.Plan latest = support.latest("#ircafe", "", 25);
    Ircv3ChatHistoryRuntimeSupport.Plan between =
        support.between("#ircafe", "MSGID=one", "*", 40);
    Ircv3ChatHistoryRuntimeSupport.Plan around =
        support.around("#ircafe", "TIMESTAMP=2026-07-13T12:34:56Z", 30);

    assertEquals(
        "CHATHISTORY BEFORE #ircafe timestamp=2026-07-13T12:34:56.000Z 200",
        before.rawLine());
    assertEquals("timestamp=2026-07-13T12:34:56.000Z", before.primarySelector());
    assertEquals(200, before.limit());
    assertEquals("CHATHISTORY LATEST #ircafe * 25", latest.rawLine());
    assertEquals("CHATHISTORY BETWEEN #ircafe msgid=one * 40", between.rawLine());
    assertEquals("msgid=one .. *", between.selectorSummary());
    assertEquals(
        "CHATHISTORY AROUND #ircafe timestamp=2026-07-13T12:34:56Z 30",
        around.rawLine());
    assertTrue(support.available());
  }

  @Test
  void higherPriorityProviderCanReplaceOneOperation() {
    Ircv3OutboundCommandProvider builtIn =
        new cafe.woden.ircclient.irc.ircv3.Ircv3ChatHistoryExtensionProvider();
    Ircv3OutboundCommandProvider replacement =
        new Ircv3OutboundCommandProvider() {
          @Override
          public String providerId() {
            return "test-history";
          }

          @Override
          public int priority() {
            return 100;
          }

          @Override
          public Set<Ircv3OutboundCommandOperation> operations() {
            return Set.of(Ircv3OutboundCommandOperation.CHAT_HISTORY_LATEST);
          }

          @Override
          public List<String> build(
              Ircv3OutboundCommandOperation operation,
              Ircv3OutboundCommandRequest request) {
            return List.of("CHATHISTORY LATEST " + request.target() + " msgid=plugin 17");
          }
        };
    Ircv3ChatHistoryRuntimeSupport support =
        new Ircv3ChatHistoryRuntimeSupport(
            Ircv3OutboundCommandRuntimeCatalog.fromProviders(List.of(builtIn, replacement)));

    Ircv3ChatHistoryRuntimeSupport.Plan plan = support.latest("#ircafe", "*", 50);

    assertEquals("msgid=plugin", plan.primarySelector());
    assertEquals(17, plan.limit());
  }

  @Test
  void rejectsUnsupportedRequestedSelectorAsCallerInput() {
    Ircv3ChatHistoryRuntimeSupport support = Ircv3RuntimeTestFixtures.chatHistory();

    assertThrows(
        IllegalArgumentException.class,
        () -> support.around("#ircafe", "other=value", 20));
  }

  @Test
  void rejectsUnsafeOrMismatchedProviderOutput() {
    Ircv3OutboundCommandProvider provider =
        new Ircv3OutboundCommandProvider() {
          @Override
          public String providerId() {
            return "bad-history";
          }

          @Override
          public Set<Ircv3OutboundCommandOperation> operations() {
            return Set.of(Ircv3OutboundCommandOperation.CHAT_HISTORY_AROUND);
          }

          @Override
          public List<String> build(
              Ircv3OutboundCommandOperation operation,
              Ircv3OutboundCommandRequest request) {
            return List.of("CHATHISTORY AROUND #other msgid=one 20");
          }
        };
    Ircv3ChatHistoryRuntimeSupport support =
        new Ircv3ChatHistoryRuntimeSupport(
            Ircv3OutboundCommandRuntimeCatalog.fromProviders(List.of(provider)));

    assertThrows(
        IllegalStateException.class,
        () -> support.around("#ircafe", "msgid=one", 20));
  }
}

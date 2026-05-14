package cafe.woden.ircclient.irc.pircbotx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.IrcPropertiesTestFixtures;
import cafe.woden.ircclient.config.SojuProperties;
import cafe.woden.ircclient.irc.*;
import cafe.woden.ircclient.irc.backend.*;
import cafe.woden.ircclient.irc.ircv3.*;
import cafe.woden.ircclient.irc.playback.*;
import cafe.woden.ircclient.net.ProxyPlan;
import cafe.woden.ircclient.net.ServerProxyResolver;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.Test;
import org.pircbotx.Configuration;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.managers.ThreadedListenerManager;

class PircbotxBotFactoryListenerManagerTest {

  @Test
  void buildUsesSingleThreadedListenerManagerForDeterministicEventOrdering() throws Exception {
    ServerProxyResolver proxyResolver = mock(ServerProxyResolver.class);
    when(proxyResolver.planForServer("libera")).thenReturn(ProxyPlan.direct());

    PircbotxBotFactory factory =
        new PircbotxBotFactory(proxyResolver, new SojuProperties(Map.of(), null), null);

    IrcProperties.Server server =
        IrcPropertiesTestFixtures.serverBuilder("libera")
            .host("irc.libera.chat")
            .sasl(new IrcProperties.Server.Sasl(false, "", "", "PLAIN", null))
            .nickserv(new IrcProperties.Server.Nickserv(false, "", "", false))
            .autoJoin(List.of("#ircafe"))
            .build();

    Configuration cfg =
        factory.build(server, "IRCafe test", new ListenerAdapter() {}).getConfiguration();

    ThreadedListenerManager manager =
        assertInstanceOf(ThreadedListenerManager.class, cfg.getListenerManager());

    Field poolField = ThreadedListenerManager.class.getDeclaredField("pool");
    poolField.setAccessible(true);
    ExecutorService pool = assertInstanceOf(ExecutorService.class, poolField.get(manager));
    ThreadPoolExecutor executor = assertInstanceOf(ThreadPoolExecutor.class, pool);

    assertEquals(1, executor.getCorePoolSize());
    assertEquals(1, executor.getMaximumPoolSize());
    assertTrue(executor.allowsCoreThreadTimeOut());

    manager.shutdown();
  }
}

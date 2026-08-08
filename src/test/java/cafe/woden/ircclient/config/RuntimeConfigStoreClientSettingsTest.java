package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuntimeConfigStoreClientSettingsTest {

  @TempDir Path tempDir;

  @Test
  void persistsNormalizedClientHeartbeatAndProxySettings() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigStore store = RuntimeConfigStoreTestFixtures.store(cfg);

    store.rememberClientHeartbeat(new IrcProperties.Heartbeat(false, 500, 2_000));
    store.rememberClientProxy(
        new IrcProperties.Proxy(false, " proxy.example ", -1, " alice ", null, false, 0, -1));

    String yaml = Files.readString(cfg);
    assertTrue(yaml.contains("heartbeat:"));
    assertTrue(yaml.contains("enabled: false"));
    assertTrue(yaml.contains("checkPeriodMs: 1000"));
    assertTrue(yaml.contains("timeoutMs: 2000"));
    assertTrue(yaml.contains("proxy:"));
    assertTrue(yaml.contains("host: proxy.example"));
    assertTrue(yaml.contains("port: 0"));
    assertTrue(yaml.contains("username: alice"));
    assertTrue(yaml.contains("password: ''") || yaml.contains("password: \"\""));
    assertTrue(yaml.contains("remoteDns: false"));
    assertTrue(yaml.contains("connectTimeoutMs: 20000"));
    assertTrue(yaml.contains("readTimeoutMs: 30000"));
  }
}

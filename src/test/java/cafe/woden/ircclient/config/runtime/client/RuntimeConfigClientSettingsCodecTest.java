package cafe.woden.ircclient.config.runtime.client;

import static cafe.woden.ircclient.config.runtime.client.RuntimeConfigClientSettingsCodec.normalizeHeartbeat;
import static cafe.woden.ircclient.config.runtime.client.RuntimeConfigClientSettingsCodec.normalizeProxy;
import static cafe.woden.ircclient.config.runtime.client.RuntimeConfigClientSettingsCodec.normalizeTranslation;
import static cafe.woden.ircclient.config.runtime.client.RuntimeConfigClientSettingsCodec.putOptionalString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import cafe.woden.ircclient.config.IrcProperties;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigClientSettingsCodecTest {

  @Test
  void normalizeHeartbeatDefaultsNullAndClampsSmallPeriods() {
    RuntimeConfigClientSettingsCodec.HeartbeatSettings defaults = normalizeHeartbeat(null);
    assertEquals(true, defaults.enabled());
    assertEquals(15_000, defaults.checkPeriodMs());
    assertEquals(360_000, defaults.timeoutMs());

    RuntimeConfigClientSettingsCodec.HeartbeatSettings normalized =
        normalizeHeartbeat(new IrcProperties.Heartbeat(false, 500, 2_000));
    assertEquals(false, normalized.enabled());
    assertEquals(1_000, normalized.checkPeriodMs());
    assertEquals(2_000, normalized.timeoutMs());
  }

  @Test
  void normalizeProxyDefaultsNullAndTrimsPersistedStrings() {
    RuntimeConfigClientSettingsCodec.ProxySettings defaults = normalizeProxy(null);
    assertEquals(false, defaults.enabled());
    assertEquals("", defaults.host());
    assertEquals(0, defaults.port());
    assertEquals("", defaults.username());
    assertEquals("", defaults.password());
    assertEquals(true, defaults.remoteDns());
    assertEquals(20_000, defaults.connectTimeoutMs());
    assertEquals(30_000, defaults.readTimeoutMs());

    RuntimeConfigClientSettingsCodec.ProxySettings normalized =
        normalizeProxy(
            new IrcProperties.Proxy(false, " proxy.example ", -1, " alice ", null, false, 0, -1));
    assertEquals("proxy.example", normalized.host());
    assertEquals(0, normalized.port());
    assertEquals("alice", normalized.username());
    assertEquals("", normalized.password());
    assertEquals(20_000, normalized.connectTimeoutMs());
    assertEquals(30_000, normalized.readTimeoutMs());
  }

  @Test
  void normalizeTranslationSerializesTokensAndDetectionLanguagePolicy() {
    RuntimeConfigClientSettingsCodec.TranslationSettings normalized =
        normalizeTranslation(
            new IrcProperties.Client.Translation(
                true,
                IrcProperties.Client.Translation.Mode.MANUAL,
                " DeePL ",
                "",
                " secret ",
                " EN ",
                " ES ",
                false,
                false,
                List.of(" EN ", "es", ""),
                IrcProperties.Client.Translation.DisplayMode.BELOW_ORIGINAL,
                0,
                -1,
                99));

    assertEquals(true, normalized.enabled());
    assertEquals("manual", normalized.mode());
    assertEquals("deepl", normalized.backendId());
    assertEquals("https://api-free.deepl.com/v2/translate", normalized.endpoint());
    assertEquals("secret", normalized.apiKey());
    assertEquals("en", normalized.sourceLanguage());
    assertEquals("es", normalized.targetLanguage());
    assertEquals(false, normalized.translateUnknownMessages());
    assertEquals(false, normalized.detectAllLanguages());
    assertEquals(List.of("en", "es"), normalized.detectionLanguages());
    assertEquals("below_original", normalized.displayMode());
    assertEquals(10_000, normalized.requestTimeoutMs());
    assertEquals(4_000, normalized.maxRequestChars());
    assertEquals(16, normalized.maxConcurrentRequests());
  }

  @Test
  void normalizeTranslationDefaultsNullAndSuppressesDetectionLanguagesWhenDetectingAll() {
    RuntimeConfigClientSettingsCodec.TranslationSettings normalized = normalizeTranslation(null);

    assertEquals(false, normalized.enabled());
    assertEquals("auto", normalized.mode());
    assertEquals("", normalized.backendId());
    assertEquals("", normalized.endpoint());
    assertEquals("", normalized.apiKey());
    assertEquals("auto", normalized.sourceLanguage());
    assertEquals("", normalized.targetLanguage());
    assertEquals(true, normalized.translateUnknownMessages());
    assertEquals(true, normalized.detectAllLanguages());
    assertEquals(List.of(), normalized.detectionLanguages());
    assertEquals("below_original", normalized.displayMode());
    assertEquals(10_000, normalized.requestTimeoutMs());
    assertEquals(4_000, normalized.maxRequestChars());
    assertEquals(2, normalized.maxConcurrentRequests());
  }

  @Test
  void putOptionalStringRemovesBlankAndDefaultValues() {
    Map<String, Object> target = new LinkedHashMap<>();
    target.put("backend", "old");
    target.put("targetLanguage", "old");

    putOptionalString(target, "backend", " ", "");
    putOptionalString(target, "targetLanguage", "auto", "auto");
    putOptionalString(target, "apiKey", " secret ", "");

    assertFalse(target.containsKey("backend"));
    assertFalse(target.containsKey("targetLanguage"));
    assertEquals("secret", target.get("apiKey"));
  }
}

package cafe.woden.ircclient.config.runtime.client;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted IRC client transport settings under {@code irc.client}. */
public class RuntimeConfigClientSettingsStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigClientSettingsStore.class);

  private final RuntimeConfigYamlSection clientSection;

  public RuntimeConfigClientSettingsStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.clientSection = RuntimeConfigYamlSection.irc(file, documentStore, log, "client");
  }

  public synchronized void rememberTlsTrustAllCertificates(boolean trustAllCertificates) {
    clientSection.putValue(
        "TLS trust-all setting", trustAllCertificates, "tls", "trustAllCertificates");
  }

  public synchronized void rememberHeartbeat(IrcProperties.Heartbeat heartbeat) {
    IrcProperties.Heartbeat hb =
        (heartbeat != null) ? heartbeat : new IrcProperties.Heartbeat(true, 15_000, 360_000);

    clientSection.mutateMap(
        "heartbeat settings",
        hbMap -> {
          hbMap.put("enabled", hb.enabled());
          hbMap.put("checkPeriodMs", Math.max(1_000L, hb.checkPeriodMs()));
          hbMap.put("timeoutMs", Math.max(1_000L, hb.timeoutMs()));
        },
        "heartbeat");
  }

  public synchronized void rememberProxy(IrcProperties.Proxy proxy) {
    IrcProperties.Proxy p =
        (proxy != null)
            ? proxy
            : new IrcProperties.Proxy(false, "", 0, "", "", true, 20_000, 30_000);

    clientSection.mutateMap(
        "SOCKS proxy settings",
        proxyMap -> {
          proxyMap.put("enabled", p.enabled());
          proxyMap.put("host", Objects.toString(p.host(), "").trim());
          proxyMap.put("port", Math.max(0, p.port()));
          proxyMap.put("username", Objects.toString(p.username(), "").trim());
          proxyMap.put("password", Objects.toString(p.password(), ""));
          proxyMap.put("remoteDns", p.remoteDns());
          proxyMap.put("connectTimeoutMs", Math.max(0L, p.connectTimeoutMs()));
          proxyMap.put("readTimeoutMs", Math.max(0L, p.readTimeoutMs()));
        },
        "proxy");
  }

  public synchronized void rememberTranslation(IrcProperties.Client.Translation translation) {
    IrcProperties.Client.Translation safe =
        translation != null
            ? translation
            : new IrcProperties.Client.Translation(
                false,
                IrcProperties.Client.Translation.Mode.AUTO,
                "",
                "",
                "",
                "auto",
                "",
                null,
                10_000,
                4_000,
                2);

    clientSection.mutateMap(
        "translation settings",
        translationMap -> {
          translationMap.put("enabled", safe.enabled());
          translationMap.put("mode", safe.mode().name().toLowerCase());
          rememberOptionalString(translationMap, "backend", safe.backendId(), "");
          rememberOptionalString(translationMap, "endpoint", safe.endpoint(), "");
          rememberOptionalString(translationMap, "apiKey", safe.apiKey(), "");
          translationMap.put("sourceLanguage", safe.sourceLanguage());
          rememberOptionalString(translationMap, "targetLanguage", safe.targetLanguage(), "");
          translationMap.put("translateUnknownMessages", safe.translateUnknownMessages());
          translationMap.put("detectAllLanguages", safe.detectAllLanguages());
          if (safe.detectAllLanguages() || safe.detectionLanguages().isEmpty()) {
            translationMap.remove("detectionLanguages");
          } else {
            translationMap.put("detectionLanguages", safe.detectionLanguages());
          }
          translationMap.put("displayMode", safe.displayMode().name().toLowerCase());
          translationMap.put("requestTimeoutMs", Math.max(1L, safe.requestTimeoutMs()));
          translationMap.put("maxRequestChars", Math.max(1, safe.maxRequestChars()));
          translationMap.put("maxConcurrentRequests", Math.max(1, safe.maxConcurrentRequests()));
        },
        "translation");
  }

  private static void rememberOptionalString(
      java.util.Map<String, Object> target, String key, String value, String defaultValue) {
    String normalized = Objects.toString(value, "").trim();
    if (normalized.isEmpty() || normalized.equals(defaultValue)) {
      target.remove(key);
    } else {
      target.put(key, normalized);
    }
  }
}

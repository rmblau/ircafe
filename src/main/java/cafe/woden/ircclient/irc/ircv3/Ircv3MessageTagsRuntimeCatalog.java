package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParseRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParseResult;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParserProvider;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Runtime catalog for the replaceable IRCv3 message-tag parser. */
@Component
@InfrastructureLayer
public final class Ircv3MessageTagsRuntimeCatalog {

  private static final int MAX_TAGS = 128;
  private static final int MAX_TAG_KEY_LENGTH = 128;
  private static final int MAX_TAG_VALUE_LENGTH = 8192;

  private final Ircv3MessageTagParserProvider provider;

  @Autowired
  public Ircv3MessageTagsRuntimeCatalog(InstalledPluginsPort installedPlugins) {
    this(selectProvider(loadInstalledProviders(installedPlugins)));
  }

  private Ircv3MessageTagsRuntimeCatalog(Ircv3MessageTagParserProvider provider) {
    this.provider = provider;
  }

  public static Ircv3MessageTagsRuntimeCatalog applicationClasspath() {
    return fromProviders(loadApplicationProviders());
  }

  public static Ircv3MessageTagsRuntimeCatalog fromInstalledServices(
      InstalledPluginsPort installedPlugins) {
    return new Ircv3MessageTagsRuntimeCatalog(
        selectProvider(loadInstalledProviders(installedPlugins)));
  }

  public static Ircv3MessageTagsRuntimeCatalog fromProviders(
      List<? extends Ircv3MessageTagParserProvider> providers) {
    return new Ircv3MessageTagsRuntimeCatalog(selectProvider(providers));
  }

  public String providerId() {
    return provider == null
        ? ""
        : Ircv3RuntimeProviderSupport.normalizeProviderId(provider.providerId());
  }

  public Map<String, String> parse(Map<String, String> transportTags, String rawLine) {
    if (provider == null) {
      return Map.of();
    }
    Ircv3MessageTagParseResult parsed =
        provider.parse(new Ircv3MessageTagParseRequest(transportTags, rawLine));
    return sanitize(parsed == null ? Map.of() : parsed.tags());
  }

  public Map<String, String> parseRawLine(String rawLine) {
    return parse(Map.of(), rawLine);
  }

  static List<Ircv3MessageTagParserProvider> loadApplicationProviders() {
    return Ircv3RuntimeProviderSupport.loadApplicationProviders(
        Ircv3MessageTagParserProvider.class, Ircv3MessageTagsRuntimeCatalog.class);
  }

  private static List<Ircv3MessageTagParserProvider> loadInstalledProviders(
      InstalledPluginsPort installedPlugins) {
    return Ircv3RuntimeProviderSupport.loadInstalledProviders(
        Ircv3MessageTagParserProvider.class,
        Ircv3MessageTagsRuntimeCatalog.class,
        installedPlugins,
        Ircv3MessageTagsRuntimeCatalog::fromProviders,
        "Failed to load IRCv3 message-tag parser providers.");
  }

  private static Ircv3MessageTagParserProvider selectProvider(
      List<? extends Ircv3MessageTagParserProvider> providers) {
    return Ircv3RuntimeProviderSupport.selectHighestPriority(
        providers,
        Ircv3MessageTagParserProvider::providerId,
        Ircv3MessageTagParserProvider::messageTagParserPriority,
        "IRCv3 message-tag parser");
  }

  private static Map<String, String> sanitize(Map<String, String> rawTags) {
    if (rawTags == null || rawTags.isEmpty()) {
      return Map.of();
    }
    if (rawTags.size() > MAX_TAGS) {
      throw new IllegalArgumentException("IRCv3 message-tag provider returned too many tags");
    }

    LinkedHashMap<String, String> sanitized = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : rawTags.entrySet()) {
      String key = normalizeTagKey(entry.getKey());
      if (key.isEmpty()) {
        continue;
      }
      if (key.length() > MAX_TAG_KEY_LENGTH || !isSafeTagKey(key)) {
        throw new IllegalArgumentException("IRCv3 message-tag provider returned an invalid key");
      }
      String value = Objects.toString(entry.getValue(), "");
      if (value.length() > MAX_TAG_VALUE_LENGTH) {
        throw new IllegalArgumentException(
            "IRCv3 message-tag provider returned an oversized value for " + key);
      }
      sanitized.put(key, value);
    }
    return sanitized.isEmpty()
        ? Map.of()
        : Collections.unmodifiableMap(new LinkedHashMap<>(sanitized));
  }

  private static String normalizeTagKey(String rawKey) {
    String key = Objects.toString(rawKey, "").trim();
    if (key.startsWith("@")) {
      key = key.substring(1).trim();
    }
    if (key.startsWith("+")) {
      key = key.substring(1).trim();
    }
    return key.toLowerCase(Locale.ROOT);
  }

  private static boolean isSafeTagKey(String key) {
    for (int i = 0; i < key.length(); i++) {
      char c = key.charAt(i);
      if (Character.isWhitespace(c) || Character.isISOControl(c) || c == ';' || c == '=') {
        return false;
      }
    }
    return true;
  }
}

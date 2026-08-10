package cafe.woden.ircclient.plugin.spi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Attributes;

/** Manifest and compatibility contract for IRCafe plugin jars discovered through ServiceLoader. */
public final class IrcafePluginManifest {

  /** Stable plugin identifier declared by each external plugin jar. */
  public static final String PLUGIN_ID_ATTRIBUTE = "Ircafe-Plugin-Id";

  /** Plugin version declared by each external plugin jar. */
  public static final String PLUGIN_VERSION_ATTRIBUTE = "Ircafe-Plugin-Version";

  /**
   * Fallback plugin version attribute accepted when {@link #PLUGIN_VERSION_ATTRIBUTE} is absent.
   */
  public static final String FALLBACK_PLUGIN_VERSION_ATTRIBUTE =
      Attributes.Name.IMPLEMENTATION_VERSION.toString();

  /** Plugin API compatibility line declared by each external plugin jar. */
  public static final String PLUGIN_API_VERSION_ATTRIBUTE = "Ircafe-Plugin-Api-Version";

  /** Required IRCafe manifest attributes for declared external plugin jars. */
  public static final List<String> REQUIRED_PLUGIN_ATTRIBUTE_NAMES =
      List.of(PLUGIN_ID_ATTRIBUTE, PLUGIN_VERSION_ATTRIBUTE, PLUGIN_API_VERSION_ATTRIBUTE);

  /** Supported manifest attributes that may identify a plugin jar version. */
  public static final List<String> SUPPORTED_PLUGIN_VERSION_ATTRIBUTE_NAMES =
      List.of(PLUGIN_VERSION_ATTRIBUTE, FALLBACK_PLUGIN_VERSION_ATTRIBUTE);

  /** Current plugin API compatibility line accepted by this build. */
  public static final int SUPPORTED_PLUGIN_API_VERSION = 1;

  /** Java release plugin authors should target when compiling against this plugin API jar. */
  public static final int REQUIRED_JAVA_RELEASE = 25;

  /** Default leaf directory used for installed plugin jars next to the runtime config file. */
  public static final String DEFAULT_PLUGIN_DIRECTORY_NAME = "plugins";

  /**
   * Builds a compatible manifest attribute set for simple plugin-author packaging tools and tests.
   */
  public static Map<String, String> compatibleManifestAttributes(
      String pluginId, String pluginVersion) {
    LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
    attributes.put(PLUGIN_ID_ATTRIBUTE, requiredText(pluginId, "pluginId"));
    attributes.put(PLUGIN_VERSION_ATTRIBUTE, requiredText(pluginVersion, "pluginVersion"));
    attributes.put(PLUGIN_API_VERSION_ATTRIBUTE, Integer.toString(SUPPORTED_PLUGIN_API_VERSION));
    return Collections.unmodifiableMap(attributes);
  }

  /**
   * Builds a compatible manifest attribute set that uses the supported {@code
   * Implementation-Version} fallback for the plugin version.
   */
  public static Map<String, String> compatibleImplementationVersionManifestAttributes(
      String pluginId, String implementationVersion) {
    LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
    attributes.put(PLUGIN_ID_ATTRIBUTE, requiredText(pluginId, "pluginId"));
    attributes.put(
        FALLBACK_PLUGIN_VERSION_ATTRIBUTE,
        requiredText(implementationVersion, "implementationVersion"));
    attributes.put(PLUGIN_API_VERSION_ATTRIBUTE, Integer.toString(SUPPORTED_PLUGIN_API_VERSION));
    return Collections.unmodifiableMap(attributes);
  }

  private static String requiredText(String value, String name) {
    String normalized = Objects.toString(value, "").trim();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("[ircafe] " + name + " must not be blank");
    }
    return normalized;
  }

  private IrcafePluginManifest() {}
}

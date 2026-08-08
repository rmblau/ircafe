package cafe.woden.ircclient.plugin.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IrcafePluginManifestTest {

  @Test
  void buildsImplementationVersionFallbackAttributesForAuthorTools() {
    Map<String, String> attributes =
        IrcafePluginManifest.compatibleImplementationVersionManifestAttributes(
            " fallback-plugin ", " 2.3.4 ");

    assertEquals("fallback-plugin", attributes.get(IrcafePluginManifest.PLUGIN_ID_ATTRIBUTE));
    assertEquals("2.3.4", attributes.get(IrcafePluginManifest.FALLBACK_PLUGIN_VERSION_ATTRIBUTE));
    assertEquals(
        Integer.toString(IrcafePluginManifest.SUPPORTED_PLUGIN_API_VERSION),
        attributes.get(IrcafePluginManifest.PLUGIN_API_VERSION_ATTRIBUTE));
    assertEquals(
        List.of(
            IrcafePluginManifest.PLUGIN_ID_ATTRIBUTE,
            IrcafePluginManifest.FALLBACK_PLUGIN_VERSION_ATTRIBUTE,
            IrcafePluginManifest.PLUGIN_API_VERSION_ATTRIBUTE),
        List.copyOf(attributes.keySet()));
  }
}

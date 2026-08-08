package cafe.woden.ircclient.app.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.app.outbound.backend.spi.BackendEditorProfile;
import java.util.List;
import org.junit.jupiter.api.Test;

class BackendEditorProfileCatalogTest {

  @Test
  void builtInsFallbackToIrcProfileForUnknownBackendId() {
    BackendEditorProfileCatalog catalog = BackendEditorProfileCatalog.builtIns();

    assertEquals("plugin-backend", catalog.displayName("plugin-backend"));
    assertEquals(BackendUiMode.IRC, catalog.uiModeForBackendId("plugin-backend"));
    assertFalse(catalog.supportsQuasselCoreCommands("plugin-backend"));
  }

  @Test
  void builtInProfilesReusePluginFacingMetadata() {
    List<BackendEditorProfileSpec> appProfiles = BuiltInBackendEditorProfiles.all();
    List<BackendEditorProfile> pluginProfiles =
        cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendEditorProfiles.all();

    assertEquals(pluginProfiles.size(), appProfiles.size());
    for (int i = 0; i < pluginProfiles.size(); i++) {
      assertEquivalentProfile(pluginProfiles.get(i), appProfiles.get(i));
    }
  }

  @Test
  void pluginProfilesOverrideBuiltInMetadataByBackendId() {
    AvailableBackendIdsPort backendMetadata = mock(AvailableBackendIdsPort.class);
    when(backendMetadata.availableBackendEditorProfiles())
        .thenReturn(
            List.of(
                new BackendEditorProfileSpec(
                    "matrix",
                    "Plugin Matrix",
                    BackendUiMode.IRC,
                    9000,
                    9443,
                    false,
                    false,
                    false,
                    false,
                    true,
                    "",
                    "Host",
                    "Password",
                    "Nick",
                    "Login",
                    "Real name",
                    "Use TLS",
                    "Plugin override.",
                    "Plugin override auth.",
                    "",
                    "plugin.example.org",
                    "",
                    "",
                    "")));

    BackendEditorProfileCatalog catalog = BackendEditorProfileCatalog.from(backendMetadata);

    assertEquals("Plugin Matrix", catalog.displayName("matrix"));
    assertEquals(BackendUiMode.IRC, catalog.uiModeForBackendId("matrix"));
    assertTrue(catalog.supportsQuasselCoreCommands("matrix"));
  }

  @Test
  void pluginProfilesAddCustomBackendMetadata() {
    AvailableBackendIdsPort backendMetadata = mock(AvailableBackendIdsPort.class);
    when(backendMetadata.availableBackendEditorProfiles())
        .thenReturn(List.of(pluginProfile("plugin-matrix", BackendUiMode.MATRIX, false)));

    BackendEditorProfileCatalog catalog = BackendEditorProfileCatalog.from(backendMetadata);

    assertEquals("Plugin Matrix", catalog.displayName(" plugin-matrix "));
    assertEquals(BackendUiMode.MATRIX, catalog.uiModeForBackendId(" plugin-matrix "));
    assertFalse(catalog.supportsQuasselCoreCommands(" plugin-matrix "));
  }

  private static void assertEquivalentProfile(
      BackendEditorProfile pluginProfile, BackendEditorProfileSpec appProfile) {
    assertEquals(pluginProfile.backendId(), appProfile.backendId());
    assertEquals(pluginProfile.displayName(), appProfile.displayName());
    assertEquals(pluginProfile.uiMode().name(), appProfile.uiMode().name());
    assertEquals(pluginProfile.defaultPlainPort(), appProfile.defaultPlainPort());
    assertEquals(pluginProfile.defaultTlsPort(), appProfile.defaultTlsPort());
    assertEquals(pluginProfile.directAuthEnabled(), appProfile.directAuthEnabled());
    assertEquals(pluginProfile.matrixAuthSupported(), appProfile.matrixAuthSupported());
    assertEquals(pluginProfile.requiresNick(), appProfile.requiresNick());
    assertEquals(pluginProfile.usesNickAsDefaultLogin(), appProfile.usesNickAsDefaultLogin());
    assertEquals(
        pluginProfile.supportsQuasselCoreCommands(), appProfile.supportsQuasselCoreCommands());
    assertEquals(pluginProfile.defaultLoginFallback(), appProfile.defaultLoginFallback());
    assertEquals(pluginProfile.hostLabel(), appProfile.hostLabel());
    assertEquals(pluginProfile.serverPasswordLabel(), appProfile.serverPasswordLabel());
    assertEquals(pluginProfile.nickLabel(), appProfile.nickLabel());
    assertEquals(pluginProfile.loginLabel(), appProfile.loginLabel());
    assertEquals(pluginProfile.realNameLabel(), appProfile.realNameLabel());
    assertEquals(pluginProfile.tlsToggleLabel(), appProfile.tlsToggleLabel());
    assertEquals(pluginProfile.connectionHint(), appProfile.connectionHint());
    assertEquals(pluginProfile.authDisabledHint(), appProfile.authDisabledHint());
    assertEquals(pluginProfile.serverPasswordPlaceholder(), appProfile.serverPasswordPlaceholder());
    assertEquals(pluginProfile.hostPlaceholder(), appProfile.hostPlaceholder());
    assertEquals(pluginProfile.loginPlaceholder(), appProfile.loginPlaceholder());
    assertEquals(pluginProfile.nickPlaceholder(), appProfile.nickPlaceholder());
    assertEquals(pluginProfile.realNamePlaceholder(), appProfile.realNamePlaceholder());
  }

  private static BackendEditorProfileSpec pluginProfile(
      String backendId, BackendUiMode uiMode, boolean supportsQuasselCoreCommands) {
    return new BackendEditorProfileSpec(
        backendId,
        "Plugin Matrix",
        uiMode,
        8448,
        8448,
        false,
        false,
        false,
        false,
        supportsQuasselCoreCommands,
        "",
        "Homeserver",
        "Credential",
        "Nick",
        "Login",
        "Display name",
        "Use TLS",
        "Plugin backend.",
        "Plugin auth.",
        "token",
        "https://plugin.example.org",
        "@alice:plugin.example.org",
        "PluginNick",
        "Plugin User");
  }
}

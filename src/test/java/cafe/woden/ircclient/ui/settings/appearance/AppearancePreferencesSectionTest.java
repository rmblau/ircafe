package cafe.woden.ircclient.ui.settings.appearance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import cafe.woden.ircclient.ui.settings.UiSettingsTestFixtures;
import cafe.woden.ircclient.ui.settings.theme.ThemeManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AppearancePreferencesSectionTest {

  @Test
  void buildCreatesControlsPanelAndLivePreviewSeam() {
    ThemeManager themeManager = mock(ThemeManager.class);
    when(themeManager.supportedThemes())
        .thenReturn(
            new ThemeManager.ThemeOption[] {
              new ThemeManager.ThemeOption(
                  "darcula",
                  "Darcula",
                  ThemeManager.ThemeTone.DARK,
                  ThemeManager.ThemePack.FLATLAF,
                  true)
            });
    List<AutoCloseable> closeables = new ArrayList<>();

    AppearancePreferencesSection section =
        AppearancePreferencesSection.build(
            uiSettings(), closeables, mock(UiSettingsBus.class), themeManager, null, null, null);
    try {
      assertEquals("darcula", section.theme().combo.getSelectedItem());
      assertNotNull(section.fonts());
      assertNotNull(section.accent());
      assertNotNull(section.tweaks());
      assertNotNull(section.chatTheme());
      assertNotNull(section.serverTree());
      assertNotNull(section.panel());
      assertNotNull(section.preview());
      assertTrue(closeables.contains(section.preview()));
    } finally {
      section.preview().close();
    }
  }

  private static UiSettings uiSettings() {
    return UiSettingsTestFixtures.legacyBuilder()
        .autoConnectOnStart(false)
        .imageEmbedsMaxWidthPx(640)
        .imageEmbedsMaxHeightPx(480)
        .clientLineColorEnabled(false)
        .userhostDiscoveryEnabled(false)
        .build();
  }
}

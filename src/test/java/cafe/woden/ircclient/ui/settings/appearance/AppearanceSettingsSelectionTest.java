package cafe.woden.ircclient.ui.settings.appearance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettings;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettingsTestFixtures;
import cafe.woden.ircclient.ui.settings.theme.ThemeAppearanceSettingsTestFixtures;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettingsBus;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;

class AppearanceSettingsSelectionTest {

  @Test
  void readCollectsAppearanceValuesAndChangeFlags() throws Exception {
    ThemeAccentSettingsBus accentBus = mock(ThemeAccentSettingsBus.class);
    ThemeTweakSettingsBus tweakBus = mock(ThemeTweakSettingsBus.class);
    ChatThemeSettingsBus chatThemeBus = mock(ChatThemeSettingsBus.class);
    when(accentBus.get())
        .thenReturn(ThemeAppearanceSettingsTestFixtures.accentBuilder().strength(50).build());
    when(tweakBus.get())
        .thenReturn(
            ThemeAppearanceSettingsTestFixtures.tweak(ThemeTweakSettings.ThemeDensity.AUTO, 10));
    when(chatThemeBus.get()).thenReturn(defaultChatTheme());

    AppearanceSettingsSelection selection =
        AppearanceSettingsSelection.read(
            section("darcula", "Dialog", 14, true, "abc"), accentBus, tweakBus, chatThemeBus);

    assertEquals("darcula", selection.theme());
    assertEquals("Dialog", selection.chatFontFamily());
    assertEquals(14, selection.chatFontSize());
    assertEquals("#AABBCC", selection.accent().accentColor());
    assertEquals(75, selection.accent().strength());
    assertEquals(ThemeTweakSettings.ThemeDensity.COMPACT, selection.tweaks().density());
    assertEquals(ChatThemeSettings.Preset.SOFT, selection.chatTheme().preset());
    assertEquals("#112233", selection.chatTheme().timestampColor());
    assertTrue(selection.accentChanged());
    assertTrue(selection.tweaksChanged());
    assertTrue(selection.chatThemeChanged());
  }

  @Test
  void readReportsInvalidAppearanceInput() {
    AppearancePreferencesSection section = section("darcula", "Dialog", 14, true, "not-a-color");

    AppearanceControlsSupport.AppearanceSettingsException ex =
        assertThrows(
            AppearanceControlsSupport.AppearanceSettingsException.class,
            () -> AppearanceSettingsSelection.read(section, null, null, null));

    assertEquals("Invalid accent color", ex.title());
  }

  private static AppearancePreferencesSection section(
      String theme, String fontFamily, int fontSize, boolean accentEnabled, String accentHex) {
    return new AppearancePreferencesSection(
        themeControls(theme),
        fontControls(fontFamily, fontSize),
        accentControls(accentEnabled, accentHex),
        tweakControls(),
        chatThemeControls(),
        serverTreeControls(),
        new JPanel(),
        null);
  }

  private static ThemeControls themeControls(String theme) {
    JComboBox<String> combo = new JComboBox<>(new String[] {theme});
    combo.setSelectedItem(theme);
    return new ThemeControls(combo);
  }

  private static FontControls fontControls(String fontFamily, int fontSize) {
    JComboBox<String> fontFamilyCombo = new JComboBox<>(new String[] {fontFamily});
    fontFamilyCombo.setSelectedItem(fontFamily);
    return new FontControls(
        fontFamilyCombo, new JSpinner(new SpinnerNumberModel(fontSize, 8, 48, 1)));
  }

  private static AccentControls accentControls(boolean enabled, String hex) {
    JCheckBox enabledBox = new JCheckBox();
    enabledBox.setSelected(enabled);
    return new AccentControls(
        enabledBox,
        new JComboBox<>(AccentPreset.values()),
        new JTextField(hex),
        new JButton(),
        new JButton(),
        new JSlider(0, 100, 75),
        new JPanel(),
        new JPanel(),
        () -> {},
        () -> {},
        () -> {});
  }

  private static TweakControls tweakControls() {
    JComboBox<DensityOption> density =
        new JComboBox<>(new DensityOption[] {new DensityOption("compact", "Compact")});
    density.setSelectedIndex(0);
    JCheckBox uiFontOverride = new JCheckBox();
    uiFontOverride.setSelected(true);
    JComboBox<String> fontFamily = new JComboBox<>(new String[] {"Dialog"});
    fontFamily.setSelectedItem("Dialog");
    return new TweakControls(
        density,
        new JSlider(0, 20, 4),
        uiFontOverride,
        fontFamily,
        new JSpinner(new SpinnerNumberModel(14, 8, 48, 1)),
        () -> {});
  }

  private static ChatThemeControls chatThemeControls() {
    JComboBox<ChatThemeSettings.Preset> preset = new JComboBox<>(ChatThemeSettings.Preset.values());
    preset.setSelectedItem(ChatThemeSettings.Preset.SOFT);
    return new ChatThemeControls(
        preset,
        colorField("112233"),
        colorField(""),
        colorField(""),
        colorField(""),
        colorField(""),
        colorField(""),
        colorField(""),
        colorField(""),
        new JSlider(0, 100, 35));
  }

  private static AppearanceServerTreeControls serverTreeControls() {
    return new AppearanceServerTreeControls(colorField(""), colorField(""), new JCheckBox());
  }

  private static ColorField colorField(String hex) {
    return new ColorField(
        new JTextField(hex), new JButton(), new JButton(), new JPanel(), () -> {});
  }

  private static ChatThemeSettings defaultChatTheme() {
    return ChatThemeSettingsTestFixtures.defaults();
  }
}

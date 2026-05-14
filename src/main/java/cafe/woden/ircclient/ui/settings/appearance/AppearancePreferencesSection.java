package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.config.UiProperties;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettings;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeManager;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettingsBus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

public record AppearancePreferencesSection(
    ThemeControls theme,
    FontControls fonts,
    AccentControls accent,
    TweakControls tweaks,
    ChatThemeControls chatTheme,
    AppearanceServerTreeControls serverTree,
    JPanel panel,
    AppearanceLivePreviewSession preview) {

  public static AppearancePreferencesSection build(
      UiSettings current,
      List<AutoCloseable> closeables,
      UiSettingsBus settingsBus,
      ThemeManager themeManager,
      ThemeAccentSettingsBus accentSettingsBus,
      ThemeTweakSettingsBus tweakSettingsBus,
      ChatThemeSettingsBus chatThemeSettingsBus) {
    ThemeControls theme =
        AppearanceControlsSupport.buildThemeControls(current, themeLabelById(themeManager));
    FontControls fonts = AppearanceControlsSupport.buildFontControls(current, closeables);

    ThemeAccentSettings initialAccent = initialAccent(accentSettingsBus);
    AccentControls accent = AppearanceControlsSupport.buildAccentControls(initialAccent);

    ThemeTweakSettings initialTweaks = initialTweaks(tweakSettingsBus);
    TweakControls tweaks = AppearanceControlsSupport.buildTweakControls(initialTweaks, closeables);

    ChatThemeSettings initialChatTheme = initialChatTheme(chatThemeSettingsBus);
    ChatThemeControls chatTheme =
        AppearanceControlsSupport.buildChatThemeControls(initialChatTheme);

    PreferencesUiSupport.decorateComboBoxSelection(theme.combo, closeables);
    PreferencesUiSupport.decorateComboBoxSelection(chatTheme.preset, closeables);
    PreferencesUiSupport.decorateComboBoxSelection(tweaks.density, closeables);

    AppearanceLivePreviewSession preview =
        new AppearanceLivePreviewSession(
            current,
            initialAccent,
            initialTweaks,
            initialChatTheme,
            theme,
            accent,
            chatTheme,
            fonts,
            tweaks,
            settingsBus,
            themeManager,
            accentSettingsBus,
            tweakSettingsBus,
            chatThemeSettingsBus);
    closeables.add(preview);
    preview.attachListeners();

    AppearanceServerTreeControls serverTree =
        AppearanceControlsSupport.buildServerTreeControls(current);
    JPanel panel =
        AppearancePanelSupport.buildPanel(theme, accent, chatTheme, fonts, tweaks, serverTree);

    return new AppearancePreferencesSection(
        theme, fonts, accent, tweaks, chatTheme, serverTree, panel, preview);
  }

  public AppearanceControlsSupport.ServerTreeAppearanceSettings readServerTreeSettings()
      throws AppearanceControlsSupport.AppearanceSettingsException {
    return AppearanceControlsSupport.readServerTreeSettings(serverTree);
  }

  private static Map<String, String> themeLabelById(ThemeManager themeManager) {
    Map<String, String> themeLabelById = new LinkedHashMap<>();
    for (ThemeManager.ThemeOption opt : themeManager.supportedThemes()) {
      themeLabelById.put(opt.id(), opt.label());
    }
    return themeLabelById;
  }

  private static ThemeAccentSettings initialAccent(ThemeAccentSettingsBus accentSettingsBus) {
    return accentSettingsBus != null
        ? accentSettingsBus.get()
        : new ThemeAccentSettings(
            UiProperties.DEFAULT_ACCENT_COLOR, UiProperties.DEFAULT_ACCENT_STRENGTH);
  }

  private static ThemeTweakSettings initialTweaks(ThemeTweakSettingsBus tweakSettingsBus) {
    return tweakSettingsBus != null
        ? tweakSettingsBus.get()
        : new ThemeTweakSettings(ThemeTweakSettings.ThemeDensity.AUTO, 10);
  }

  private static ChatThemeSettings initialChatTheme(ChatThemeSettingsBus chatThemeSettingsBus) {
    return chatThemeSettingsBus != null
        ? chatThemeSettingsBus.get()
        : new ChatThemeSettings(
            ChatThemeSettings.Preset.DEFAULT, null, null, null, 35, null, null, null, null, null);
  }
}

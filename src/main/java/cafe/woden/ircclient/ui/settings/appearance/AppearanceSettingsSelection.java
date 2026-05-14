package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.config.UiProperties;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettings;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettingsBus;
import java.util.Objects;

public record AppearanceSettingsSelection(
    String theme,
    String chatFontFamily,
    int chatFontSize,
    ThemeAccentSettings accent,
    boolean accentChanged,
    ThemeTweakSettings tweaks,
    boolean tweaksChanged,
    ChatThemeSettings chatTheme,
    boolean chatThemeChanged) {

  public static AppearanceSettingsSelection read(
      AppearancePreferencesSection section,
      ThemeAccentSettingsBus accentSettingsBus,
      ThemeTweakSettingsBus tweakSettingsBus,
      ChatThemeSettingsBus chatThemeSettingsBus)
      throws AppearanceControlsSupport.AppearanceSettingsException {
    String theme = String.valueOf(section.theme().combo.getSelectedItem());
    String chatFontFamily = String.valueOf(section.fonts().fontFamily.getSelectedItem());
    int chatFontSize = ((Number) section.fonts().fontSize.getValue()).intValue();

    ThemeAccentSettings prevAccent = currentAccent(accentSettingsBus);
    ThemeAccentSettings nextAccent = AppearanceControlsSupport.readAccentSettings(section.accent());

    ThemeTweakSettings prevTweaks = currentTweaks(tweakSettingsBus);
    ThemeTweakSettings nextTweaks = AppearanceControlsSupport.readTweakSettings(section.tweaks());

    ChatThemeSettings prevChatTheme = currentChatTheme(chatThemeSettingsBus);
    ChatThemeSettings nextChatTheme =
        AppearanceControlsSupport.readChatThemeSettings(section.chatTheme());

    return new AppearanceSettingsSelection(
        theme,
        chatFontFamily,
        chatFontSize,
        nextAccent,
        !Objects.equals(prevAccent, nextAccent),
        nextTweaks,
        !Objects.equals(prevTweaks, nextTweaks),
        nextChatTheme,
        !Objects.equals(prevChatTheme, nextChatTheme));
  }

  private static ThemeAccentSettings currentAccent(ThemeAccentSettingsBus accentSettingsBus) {
    return accentSettingsBus != null
        ? accentSettingsBus.get()
        : new ThemeAccentSettings(
            UiProperties.DEFAULT_ACCENT_COLOR, UiProperties.DEFAULT_ACCENT_STRENGTH);
  }

  private static ThemeTweakSettings currentTweaks(ThemeTweakSettingsBus tweakSettingsBus) {
    return tweakSettingsBus != null
        ? tweakSettingsBus.get()
        : new ThemeTweakSettings(ThemeTweakSettings.ThemeDensity.AUTO, 10);
  }

  private static ChatThemeSettings currentChatTheme(ChatThemeSettingsBus chatThemeSettingsBus) {
    return chatThemeSettingsBus != null
        ? chatThemeSettingsBus.get()
        : new ChatThemeSettings(
            ChatThemeSettings.Preset.DEFAULT, null, null, null, 35, null, null, null, null, null);
  }
}

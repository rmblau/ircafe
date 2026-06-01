package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.config.api.AppearanceRuntimeConfigPort;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsColorSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettings;
import java.awt.Color;
import java.util.List;
import java.util.Map;

public final class AppearanceControlsSupport {
  private AppearanceControlsSupport() {}

  static ThemeControls buildThemeControls(UiSettings current, Map<String, String> themeLabelById) {
    return AppearanceThemeControlsFactory.build(current, themeLabelById);
  }

  static AccentControls buildAccentControls(ThemeAccentSettings current) {
    return AppearanceAccentControlsFactory.build(current);
  }

  static ChatThemeControls buildChatThemeControls(ChatThemeSettings current) {
    return AppearanceChatThemeControlsFactory.build(current);
  }

  static TweakControls buildTweakControls(
      ThemeTweakSettings current, List<AutoCloseable> closeables) {
    return AppearanceFontControlsFactory.buildTweakControls(current, closeables);
  }

  static FontControls buildFontControls(UiSettings current, List<AutoCloseable> closeables) {
    return AppearanceFontControlsFactory.buildFontControls(current, closeables);
  }

  static AppearanceServerTreeControls buildServerTreeControls(UiSettings current) {
    return AppearanceServerTreeControlsFactory.build(current);
  }

  static ColorField buildOptionalColorField(String initialHex, String pickerTitle) {
    return AppearanceColorFieldFactory.build(initialHex, pickerTitle);
  }

  static ThemeTweakSettings readTweakSettings(TweakControls controls) {
    DensityOption option =
        PreferencesUiSupport.selectedComboItem(controls.density, DensityOption.class, null);
    String densityId = option != null ? option.id : "auto";
    String uiFontFamily = PreferencesUiSupport.selectedComboText(controls.uiFontFamily);
    if (uiFontFamily.isBlank()) uiFontFamily = ThemeTweakSettings.DEFAULT_UI_FONT_FAMILY;
    return new ThemeTweakSettings(
        ThemeTweakSettings.ThemeDensity.from(densityId),
        controls.cornerRadius.getValue(),
        controls.uiFontOverrideEnabled.isSelected(),
        uiFontFamily,
        PreferencesUiSupport.spinnerInt(controls.uiFontSize));
  }

  static ThemeAccentSettings readAccentSettings(AccentControls controls)
      throws AppearanceSettingsException {
    String accentColor = null;
    if (controls.enabled.isSelected()) {
      Color parsed = SettingsColorSupport.parseHexColorLenient(controls.hex.getText());
      if (parsed == null) {
        throw new AppearanceSettingsException(
            "Invalid accent color", "Accent color must be a hex value like #RRGGBB.");
      }
      accentColor = SettingsColorSupport.toHex(parsed);
    }
    return new ThemeAccentSettings(accentColor, controls.strength.getValue());
  }

  static ChatThemeSettings readChatThemeSettings(ChatThemeControls controls)
      throws AppearanceSettingsException {
    ChatThemeSettings.Preset preset =
        PreferencesUiSupport.selectedComboItem(
            controls.preset, ChatThemeSettings.Preset.class, ChatThemeSettings.Preset.DEFAULT);
    try {
      return new ChatThemeSettings(
          preset,
          SettingsColorSupport.normalizeOptionalHexForApply(
              controls.timestamp.hex.getText(), "Chat timestamp color"),
          SettingsColorSupport.normalizeOptionalHexForApply(
              controls.system.hex.getText(), "Chat system color"),
          SettingsColorSupport.normalizeOptionalHexForApply(
              controls.mention.hex.getText(), "Mention highlight color"),
          controls.mentionStrength.getValue(),
          SettingsColorSupport.normalizeOptionalHexForApply(
              controls.message.hex.getText(), "User message color"),
          SettingsColorSupport.normalizeOptionalHexForApply(
              controls.notice.hex.getText(), "Notice message color"),
          SettingsColorSupport.normalizeOptionalHexForApply(
              controls.action.hex.getText(), "Action message color"),
          SettingsColorSupport.normalizeOptionalHexForApply(
              controls.error.hex.getText(), "Error message color"),
          SettingsColorSupport.normalizeOptionalHexForApply(
              controls.presence.hex.getText(), "Presence message color"));
    } catch (IllegalArgumentException ex) {
      throw new AppearanceSettingsException("Invalid chat message color", ex.getMessage());
    }
  }

  static ServerTreeAppearanceSettings readServerTreeSettings(AppearanceServerTreeControls controls)
      throws AppearanceSettingsException {
    return AppearanceServerTreeControlsFactory.read(controls);
  }

  public static void rememberTweakSettings(
      AppearanceRuntimeConfigPort runtimeConfig, ThemeTweakSettings settings) {
    runtimeConfig.rememberUiDensity(settings.densityId());
    runtimeConfig.rememberCornerRadius(settings.cornerRadius());
    runtimeConfig.rememberUiFontOverrideEnabled(settings.uiFontOverrideEnabled());
    runtimeConfig.rememberUiFontFamily(settings.uiFontFamily());
    runtimeConfig.rememberUiFontSize(settings.uiFontSize());
  }

  public static void rememberAccentSettings(
      AppearanceRuntimeConfigPort runtimeConfig, ThemeAccentSettings settings) {
    runtimeConfig.rememberAccentColor(settings.accentColor());
    runtimeConfig.rememberAccentStrength(settings.strength());
  }

  public static void rememberChatThemeSettings(
      AppearanceRuntimeConfigPort runtimeConfig, ChatThemeSettings settings) {
    runtimeConfig.rememberChatThemePreset(settings.preset().name());
    runtimeConfig.rememberChatTimestampColor(settings.timestampColor());
    runtimeConfig.rememberChatSystemColor(settings.systemColor());
    runtimeConfig.rememberChatMessageColor(settings.messageColor());
    runtimeConfig.rememberChatNoticeColor(settings.noticeColor());
    runtimeConfig.rememberChatActionColor(settings.actionColor());
    runtimeConfig.rememberChatErrorColor(settings.errorColor());
    runtimeConfig.rememberChatPresenceColor(settings.presenceColor());
    runtimeConfig.rememberChatMentionBgColor(settings.mentionBgColor());
    runtimeConfig.rememberChatMentionStrength(settings.mentionStrength());
  }

  public static void rememberServerTreeSettings(
      AppearanceRuntimeConfigPort runtimeConfig, ServerTreeAppearanceSettings settings) {
    AppearanceServerTreeControlsFactory.remember(runtimeConfig, settings);
  }

  public record ServerTreeAppearanceSettings(
      String unreadChannelColor,
      String highlightChannelColor,
      boolean preserveDockLayoutBetweenSessions) {}

  public static final class AppearanceSettingsException extends Exception {
    private final String title;

    AppearanceSettingsException(String title, String message) {
      super(message);
      this.title = title;
    }

    public String title() {
      return title;
    }
  }
}

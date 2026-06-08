package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.ui.localization.UiMessages;

final class AppearanceTooltips {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();
  static final String DENSITY = MESSAGES.text("preferences.appearance.tooltip.density");
  static final String CORNER_RADIUS = MESSAGES.text("preferences.appearance.tooltip.cornerRadius");
  static final String UI_FONT_OVERRIDE =
      MESSAGES.text("preferences.appearance.tooltip.uiFontOverride");

  private AppearanceTooltips() {}
}

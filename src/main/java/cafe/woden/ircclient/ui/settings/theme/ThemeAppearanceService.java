package cafe.woden.ircclient.ui.settings.theme;

import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import cafe.woden.ircclient.ui.util.UiDefaultKeys;
import cafe.woden.ircclient.ui.util.UiFontKeys;
import com.formdev.flatlaf.FlatLaf;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@InterfaceLayer
@Lazy
class ThemeAppearanceService {

  private static final String[] COMMON_TWEAK_OVERRIDE_KEYS = {
    UiDefaultKeys.COMPONENT_ARC,
    UiDefaultKeys.BUTTON_ARC,
    UiDefaultKeys.TEXT_COMPONENT_ARC,
    UiDefaultKeys.PROGRESS_BAR_ARC,
    UiDefaultKeys.SCROLL_PANE_ARC,
    UiDefaultKeys.TREE_ROW_HEIGHT,
    UiDefaultKeys.TABLE_ROW_HEIGHT,
    UiDefaultKeys.LIST_CELL_HEIGHT,
    UiDefaultKeys.BUTTON_MARGIN,
    UiDefaultKeys.TOGGLE_BUTTON_MARGIN,
    UiDefaultKeys.RADIO_BUTTON_MARGIN,
    UiDefaultKeys.CHECK_BOX_MARGIN,
    UiDefaultKeys.TEXT_COMPONENT_MARGIN,
    UiDefaultKeys.TEXT_FIELD_MARGIN,
    UiDefaultKeys.PASSWORD_FIELD_MARGIN,
    UiDefaultKeys.TEXT_AREA_MARGIN,
    UiDefaultKeys.COMBO_BOX_PADDING
  };

  private static final String[] ACCENT_OVERRIDE_KEYS = {
    UiColorKeys.ACCENT_COLOR,
    UiColorKeys.ACCENT_BASE_COLOR,
    UiColorKeys.ACCENT_BASE_2_COLOR,
    UiColorKeys.COMPONENT_FOCUS_COLOR,
    UiColorKeys.COMPONENT_LINK_COLOR,
    UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND,
    UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND,
    UiColorKeys.LIST_SELECTION_BACKGROUND,
    UiColorKeys.LIST_SELECTION_FOREGROUND,
    UiColorKeys.TABLE_SELECTION_BACKGROUND,
    UiColorKeys.TABLE_SELECTION_FOREGROUND,
    UiColorKeys.TREE_SELECTION_BACKGROUND,
    UiColorKeys.TREE_SELECTION_FOREGROUND
  };

  private static final Object NULL_SENTINEL = new Object();
  private static final String[] UI_FONT_PRIORITY_KEYS = {
    UiFontKeys.DEFAULT_FONT,
    UiFontKeys.LABEL_FONT,
    UiFontKeys.BUTTON_FONT,
    UiFontKeys.TABLE_FONT,
    UiFontKeys.TABLE_HEADER_FONT,
    UiFontKeys.TEXT_FIELD_FONT,
    UiFontKeys.TEXT_AREA_FONT,
    UiFontKeys.CHECK_BOX_FONT,
    UiFontKeys.COMBO_BOX_FONT,
    UiFontKeys.TREE_FONT,
    UiFontKeys.TABBED_PANE_FONT,
    UiFontKeys.TITLED_BORDER_FONT,
    UiFontKeys.MENU_BAR_FONT,
    UiFontKeys.MENU_FONT,
    UiFontKeys.MENU_ITEM_FONT,
    UiFontKeys.CHECK_BOX_MENU_ITEM_FONT,
    UiFontKeys.RADIO_BUTTON_MENU_ITEM_FONT,
    UiFontKeys.POPUP_MENU_FONT,
    UiFontKeys.MENU_ITEM_ACCELERATOR_FONT,
    UiFontKeys.CHECK_BOX_MENU_ITEM_ACCELERATOR_FONT,
    UiFontKeys.RADIO_BUTTON_MENU_ITEM_ACCELERATOR_FONT
  };

  private final Map<String, Object> accentBaselineValues = new HashMap<>();
  private String accentBaselineLafClassName;
  private final Map<Object, Object> uiFontBaselineValues = new HashMap<>();
  private String uiFontBaselineLafClassName;

  void applyCommonTweaks(ThemeTweakSettings tweaks) {
    ThemeTweakSettings resolved =
        tweaks != null ? tweaks : new ThemeTweakSettings(ThemeTweakSettings.ThemeDensity.AUTO, 10);

    clearCommonTweakOverrides();
    applyUiFontOverrides(resolved);

    if (!isFlatLafActive()) {
      return;
    }

    int arc = resolved.cornerRadius();
    UIManager.put(UiDefaultKeys.COMPONENT_ARC, arc);
    UIManager.put(UiDefaultKeys.BUTTON_ARC, arc);
    UIManager.put(UiDefaultKeys.TEXT_COMPONENT_ARC, arc);
    UIManager.put(UiDefaultKeys.PROGRESS_BAR_ARC, arc);
    UIManager.put(UiDefaultKeys.SCROLL_PANE_ARC, arc);

    ThemeTweakSettings.ThemeDensity density = resolved.density();
    if (density == ThemeTweakSettings.ThemeDensity.AUTO) {
      return;
    }

    int rowHeight =
        switch (density) {
          case COMPACT -> 20;
          case SPACIOUS -> 28;
          default -> 22;
        };

    UIManager.put(UiDefaultKeys.TREE_ROW_HEIGHT, rowHeight);
    UIManager.put(UiDefaultKeys.TABLE_ROW_HEIGHT, rowHeight);
    UIManager.put(UiDefaultKeys.LIST_CELL_HEIGHT, rowHeight);

    Insets buttonMargin =
        switch (density) {
          case COMPACT -> new Insets(4, 10, 4, 10);
          case SPACIOUS -> new Insets(8, 14, 8, 14);
          default -> new Insets(5, 10, 5, 10);
        };

    Insets textMargin =
        switch (density) {
          case COMPACT -> new Insets(4, 6, 4, 6);
          case SPACIOUS -> new Insets(8, 10, 8, 10);
          default -> new Insets(5, 7, 5, 7);
        };

    UIManager.put(UiDefaultKeys.BUTTON_MARGIN, buttonMargin);
    UIManager.put(UiDefaultKeys.TOGGLE_BUTTON_MARGIN, buttonMargin);
    UIManager.put(UiDefaultKeys.RADIO_BUTTON_MARGIN, buttonMargin);
    UIManager.put(UiDefaultKeys.CHECK_BOX_MARGIN, buttonMargin);

    UIManager.put(UiDefaultKeys.TEXT_COMPONENT_MARGIN, textMargin);
    UIManager.put(UiDefaultKeys.TEXT_FIELD_MARGIN, textMargin);
    UIManager.put(UiDefaultKeys.PASSWORD_FIELD_MARGIN, textMargin);
    UIManager.put(UiDefaultKeys.TEXT_AREA_MARGIN, textMargin);
    UIManager.put(UiDefaultKeys.COMBO_BOX_PADDING, textMargin);
  }

  private void applyUiFontOverrides(ThemeTweakSettings tweaks) {
    restorePreviousUiFontOverridesIfCompatible();

    if (tweaks == null || !tweaks.uiFontOverrideEnabled()) return;

    captureUiFontBaseline();

    Font defaultFont = UIManager.getFont(UiFontKeys.DEFAULT_FONT);
    if (defaultFont == null) defaultFont = UIManager.getFont(UiFontKeys.LABEL_FONT);
    if (defaultFont == null) {
      defaultFont =
          new Font(
              ThemeTweakSettings.DEFAULT_UI_FONT_FAMILY,
              Font.PLAIN,
              ThemeTweakSettings.DEFAULT_UI_FONT_SIZE);
    }

    float baseSize = Math.max(8f, defaultFont.getSize2D());
    float scale = tweaks.uiFontSize() / baseSize;

    for (Map.Entry<Object, Object> entry : uiFontBaselineValues.entrySet()) {
      Object key = entry.getKey();
      Object value = entry.getValue();
      if (!(value instanceof Font font)) continue;

      int scaledSize = Math.max(8, Math.round(font.getSize2D() * scale));
      Font replacement = new Font(tweaks.uiFontFamily(), font.getStyle(), scaledSize);
      UIManager.put(key, new FontUIResource(replacement));
    }

    UIManager.put(
        UiFontKeys.DEFAULT_FONT,
        new FontUIResource(new Font(tweaks.uiFontFamily(), Font.PLAIN, tweaks.uiFontSize())));
  }

  void applyAccentOverrides(ThemeAccentSettings accent) {
    restorePreviousAccentOverridesIfCompatible();

    if (accent == null || !accent.enabled()) return;

    Color chosen = ThemeColorUtils.parseHexColor(accent.accentColor());
    if (chosen == null) return;

    Color themeAccent = UIManager.getColor(UiColorKeys.ACCENT_COLOR);
    if (themeAccent == null) themeAccent = UIManager.getColor(UiColorKeys.COMPONENT_FOCUS_COLOR);
    if (themeAccent == null) themeAccent = new Color(0x2D, 0x6B, 0xFF);

    double strength = SettingsRangeSupport.normalizeThemePercent(accent.strength()) / 100.0;
    Color blended = ThemeColorUtils.mix(themeAccent, chosen, strength);

    Color panelBg = UIManager.getColor(UiColorKeys.PANEL_BACKGROUND);
    if (panelBg == null) panelBg = UIManager.getColor(UiColorKeys.CONTROL);

    boolean dark = ThemeColorUtils.isDark(panelBg);
    Color focus =
        dark ? ThemeColorUtils.lighten(blended, 0.20) : ThemeColorUtils.darken(blended, 0.10);
    Color link =
        dark ? ThemeColorUtils.lighten(blended, 0.28) : ThemeColorUtils.darken(blended, 0.12);

    if (!isFlatLafActive() && panelBg != null) {
      focus = ThemeColorUtils.ensureContrastAgainstBackground(focus, panelBg, 1.25);
      link = ThemeColorUtils.ensureContrastAgainstBackground(link, panelBg, 1.25);
    }

    captureAccentBaseline();

    if (isFlatLafActive()) {
      UIManager.put(UiColorKeys.ACCENT_COLOR, blended);
      UIManager.put(UiColorKeys.ACCENT_BASE_COLOR, blended);
      UIManager.put(UiColorKeys.ACCENT_BASE_2_COLOR, focus);
    }

    UIManager.put(UiColorKeys.COMPONENT_FOCUS_COLOR, focus);
    UIManager.put(UiColorKeys.COMPONENT_LINK_COLOR, link);

    Color bg = UIManager.getColor(UiColorKeys.TEXT_COMPONENT_BACKGROUND);
    if (bg == null) bg = UIManager.getColor(UiColorKeys.PANEL_BACKGROUND);
    if (bg == null) bg = UIManager.getColor(UiColorKeys.CONTROL);
    if (bg == null) bg = dark ? Color.DARK_GRAY : Color.LIGHT_GRAY;

    double selMix = dark ? 0.55 : 0.35;
    Color selectionBg = ThemeColorUtils.mix(bg, blended, selMix);
    Color selectionFg = ThemeColorUtils.bestTextColor(selectionBg);

    UIManager.put(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, selectionBg);
    UIManager.put(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, selectionFg);
    UIManager.put(UiColorKeys.LIST_SELECTION_BACKGROUND, selectionBg);
    UIManager.put(UiColorKeys.LIST_SELECTION_FOREGROUND, selectionFg);
    UIManager.put(UiColorKeys.TABLE_SELECTION_BACKGROUND, selectionBg);
    UIManager.put(UiColorKeys.TABLE_SELECTION_FOREGROUND, selectionFg);
    UIManager.put(UiColorKeys.TREE_SELECTION_BACKGROUND, selectionBg);
    UIManager.put(UiColorKeys.TREE_SELECTION_FOREGROUND, selectionFg);
  }

  private void captureAccentBaseline() {
    accentBaselineValues.clear();
    for (String key : ACCENT_OVERRIDE_KEYS) {
      Object value = UIManager.get(key);
      accentBaselineValues.put(key, value != null ? value : NULL_SENTINEL);
    }
    accentBaselineLafClassName = ThemeLookAndFeelUtils.currentLookAndFeelClassName();
  }

  private void restorePreviousAccentOverridesIfCompatible() {
    if (accentBaselineValues.isEmpty()) return;

    String currentLafClassName = ThemeLookAndFeelUtils.currentLookAndFeelClassName();
    if (!Objects.equals(currentLafClassName, accentBaselineLafClassName)) {
      accentBaselineValues.clear();
      accentBaselineLafClassName = null;
      return;
    }

    for (Map.Entry<String, Object> entry : accentBaselineValues.entrySet()) {
      UIManager.put(entry.getKey(), entry.getValue() == NULL_SENTINEL ? null : entry.getValue());
    }

    accentBaselineValues.clear();
    accentBaselineLafClassName = null;
  }

  private void captureUiFontBaseline() {
    uiFontBaselineValues.clear();

    UIDefaults defaults = UIManager.getDefaults();
    for (Map.Entry<Object, Object> entry : defaults.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof Font) {
        uiFontBaselineValues.put(entry.getKey(), value);
      }
    }

    Object defaultFont = UIManager.get(UiFontKeys.DEFAULT_FONT);
    if (!uiFontBaselineValues.containsKey(UiFontKeys.DEFAULT_FONT)) {
      uiFontBaselineValues.put(
          UiFontKeys.DEFAULT_FONT, defaultFont != null ? defaultFont : NULL_SENTINEL);
    }

    // Some LAFs expose menu fonts via LazyValue entries; resolve them explicitly.
    for (String key : UI_FONT_PRIORITY_KEYS) {
      if (key == null || key.isBlank()) continue;
      if (uiFontBaselineValues.containsKey(key)) continue;
      Font font = UIManager.getFont(key);
      if (font != null) {
        uiFontBaselineValues.put(key, font);
      }
    }

    uiFontBaselineLafClassName = ThemeLookAndFeelUtils.currentLookAndFeelClassName();
  }

  private void restorePreviousUiFontOverridesIfCompatible() {
    if (uiFontBaselineValues.isEmpty()) return;

    String currentLafClassName = ThemeLookAndFeelUtils.currentLookAndFeelClassName();
    if (!Objects.equals(currentLafClassName, uiFontBaselineLafClassName)) {
      // UIManager.put() writes developer defaults, which survive LAF switches.
      // If a font override was applied under another LAF class, clear those keys so the
      // newly-installed LAF can supply its own defaults (e.g. Tree.font in DarkLaf).
      for (Object key : uiFontBaselineValues.keySet()) {
        UIManager.put(key, null);
      }
      uiFontBaselineValues.clear();
      uiFontBaselineLafClassName = null;
      return;
    }

    for (Map.Entry<Object, Object> entry : uiFontBaselineValues.entrySet()) {
      UIManager.put(entry.getKey(), entry.getValue() == NULL_SENTINEL ? null : entry.getValue());
    }

    uiFontBaselineValues.clear();
    uiFontBaselineLafClassName = null;
  }

  private static void clearCommonTweakOverrides() {
    for (String key : COMMON_TWEAK_OVERRIDE_KEYS) {
      UIManager.put(key, null);
    }
  }

  private static boolean isFlatLafActive() {
    return UIManager.getLookAndFeel() instanceof FlatLaf;
  }
}

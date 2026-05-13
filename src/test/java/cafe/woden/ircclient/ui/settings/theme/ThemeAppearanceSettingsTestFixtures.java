package cafe.woden.ircclient.ui.settings.theme;

/** Test fixtures for theme accent and tweak settings. */
public final class ThemeAppearanceSettingsTestFixtures {
  private ThemeAppearanceSettingsTestFixtures() {}

  public static ThemeAccentSettings accentDefaults() {
    return accentBuilder().build();
  }

  public static ThemeAccentSettings accent(String accentColor, int strength) {
    return accentBuilder().accentColor(accentColor).strength(strength).build();
  }

  public static AccentBuilder accentBuilder() {
    return new AccentBuilder();
  }

  public static ThemeTweakSettings tweakDefaults() {
    return tweakBuilder().build();
  }

  public static ThemeTweakSettings tweak(
      ThemeTweakSettings.ThemeDensity density, int cornerRadius) {
    return tweakBuilder().density(density).cornerRadius(cornerRadius).build();
  }

  public static TweakBuilder tweakBuilder() {
    return new TweakBuilder();
  }

  public static final class AccentBuilder {
    private String accentColor;
    private int strength = 70;

    private AccentBuilder() {}

    public AccentBuilder accentColor(String accentColor) {
      this.accentColor = accentColor;
      return this;
    }

    public AccentBuilder strength(int strength) {
      this.strength = strength;
      return this;
    }

    public ThemeAccentSettings build() {
      return new ThemeAccentSettings(accentColor, strength);
    }
  }

  public static final class TweakBuilder {
    private ThemeTweakSettings.ThemeDensity density = ThemeTweakSettings.ThemeDensity.AUTO;
    private int cornerRadius = 10;
    private boolean uiFontOverrideEnabled;
    private String uiFontFamily = ThemeTweakSettings.DEFAULT_UI_FONT_FAMILY;
    private int uiFontSize = ThemeTweakSettings.DEFAULT_UI_FONT_SIZE;

    private TweakBuilder() {}

    public TweakBuilder density(ThemeTweakSettings.ThemeDensity density) {
      this.density = density;
      return this;
    }

    public TweakBuilder cornerRadius(int cornerRadius) {
      this.cornerRadius = cornerRadius;
      return this;
    }

    public TweakBuilder uiFontOverrideEnabled(boolean uiFontOverrideEnabled) {
      this.uiFontOverrideEnabled = uiFontOverrideEnabled;
      return this;
    }

    public TweakBuilder uiFontFamily(String uiFontFamily) {
      this.uiFontFamily = uiFontFamily;
      return this;
    }

    public TweakBuilder uiFontSize(int uiFontSize) {
      this.uiFontSize = uiFontSize;
      return this;
    }

    public ThemeTweakSettings build() {
      return new ThemeTweakSettings(
          density, cornerRadius, uiFontOverrideEnabled, uiFontFamily, uiFontSize);
    }
  }
}

package cafe.woden.ircclient.ui.settings.theme;

import cafe.woden.ircclient.ui.settings.theme.spi.ThemeContributionProvider;
import com.google.auto.service.AutoService;
import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** Built-in theme picker options supplied through the theme contribution SPI. */
@InterfaceLayer
@AutoService(ThemeContributionProvider.class)
public final class BuiltInThemeContributionProvider implements ThemeContributionProvider {

  @Override
  public List<ThemeManager.ThemeOption> themeOptions() {
    return List.of(
        new ThemeManager.ThemeOption(
            "system",
            "Native (System)",
            ThemeManager.ThemeTone.SYSTEM,
            ThemeManager.ThemePack.SYSTEM,
            true),
        new ThemeManager.ThemeOption(
            "dark", "Flat Dark", ThemeManager.ThemeTone.DARK, ThemeManager.ThemePack.FLATLAF, true),
        new ThemeManager.ThemeOption(
            "darcula",
            "Flat Darcula",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.FLATLAF,
            true),
        new ThemeManager.ThemeOption(
            "light",
            "Flat Light",
            ThemeManager.ThemeTone.LIGHT,
            ThemeManager.ThemePack.FLATLAF,
            true),
        new ThemeManager.ThemeOption(
            "crt-green",
            "CRT Green",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.RETRO,
            false),
        new ThemeManager.ThemeOption(
            "cde-blue",
            "CDE Blue",
            ThemeManager.ThemeTone.LIGHT,
            ThemeManager.ThemePack.RETRO,
            false),
        new ThemeManager.ThemeOption(
            "tokyo-night",
            "Tokyo Night",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.MODERN,
            true),
        new ThemeManager.ThemeOption(
            "catppuccin-mocha",
            "Catppuccin Mocha",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.MODERN,
            false),
        new ThemeManager.ThemeOption(
            "gruvbox-dark",
            "Gruvbox Dark",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.MODERN,
            false),
        new ThemeManager.ThemeOption(
            "github-soft-light",
            "GitHub Soft Light",
            ThemeManager.ThemeTone.LIGHT,
            ThemeManager.ThemePack.MODERN,
            true),
        new ThemeManager.ThemeOption(
            "blue-dark",
            "Flat Blue (Dark)",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.IRCAFE,
            true),
        new ThemeManager.ThemeOption(
            "violet-nebula",
            "Violet Nebula",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.IRCAFE,
            true),
        new ThemeManager.ThemeOption(
            "high-contrast-dark",
            "High Contrast Dark",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.IRCAFE,
            true),
        new ThemeManager.ThemeOption(
            "graphite-mono",
            "Graphite Mono",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.IRCAFE,
            false),
        new ThemeManager.ThemeOption(
            "forest-dark",
            "Forest Dark",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.IRCAFE,
            false),
        new ThemeManager.ThemeOption(
            "ruby-night",
            "Ruby Night",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.IRCAFE,
            false),
        new ThemeManager.ThemeOption(
            "solarized-dark",
            "Solarized Dark",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.IRCAFE,
            false),
        new ThemeManager.ThemeOption(
            "sunset-dark",
            "Sunset Dark",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.IRCAFE,
            false),
        new ThemeManager.ThemeOption(
            "terminal-amber",
            "Terminal Amber",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.IRCAFE,
            false),
        new ThemeManager.ThemeOption(
            "teal-deep",
            "Teal Deep",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.IRCAFE,
            false),
        new ThemeManager.ThemeOption(
            "orange",
            "Flat Orange (Dark)",
            ThemeManager.ThemeTone.DARK,
            ThemeManager.ThemePack.IRCAFE,
            false),
        new ThemeManager.ThemeOption(
            "nordic-light",
            "Nordic Light",
            ThemeManager.ThemeTone.LIGHT,
            ThemeManager.ThemePack.IRCAFE,
            true),
        new ThemeManager.ThemeOption(
            "blue-light",
            "Flat Blue (Light)",
            ThemeManager.ThemeTone.LIGHT,
            ThemeManager.ThemePack.IRCAFE,
            true),
        new ThemeManager.ThemeOption(
            "arctic-light",
            "Arctic Light",
            ThemeManager.ThemeTone.LIGHT,
            ThemeManager.ThemePack.IRCAFE,
            false),
        new ThemeManager.ThemeOption(
            "mint-light",
            "Mint Light",
            ThemeManager.ThemeTone.LIGHT,
            ThemeManager.ThemePack.IRCAFE,
            false),
        new ThemeManager.ThemeOption(
            "solarized-light",
            "Solarized Light",
            ThemeManager.ThemeTone.LIGHT,
            ThemeManager.ThemePack.IRCAFE,
            false));
  }
}

package cafe.woden.ircclient.ui.settings.theme.builtins;

import cafe.woden.ircclient.ui.settings.theme.spi.ThemeContributionProvider;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeOption;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemePack;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeTone;
import com.google.auto.service.AutoService;
import java.util.List;

/** Built-in theme picker options supplied through the theme contribution SPI. */
@AutoService(ThemeContributionProvider.class)
public final class BuiltInThemeContributionProvider implements ThemeContributionProvider {

  @Override
  public List<ThemeOption> themeOptions() {
    return List.of(
        new ThemeOption("system", "Native (System)", ThemeTone.SYSTEM, ThemePack.SYSTEM, true),
        new ThemeOption("dark", "Flat Dark", ThemeTone.DARK, ThemePack.FLATLAF, true),
        new ThemeOption("darcula", "Flat Darcula", ThemeTone.DARK, ThemePack.FLATLAF, true),
        new ThemeOption("light", "Flat Light", ThemeTone.LIGHT, ThemePack.FLATLAF, true),
        new ThemeOption("crt-green", "CRT Green", ThemeTone.DARK, ThemePack.RETRO, false),
        new ThemeOption("cde-blue", "CDE Blue", ThemeTone.LIGHT, ThemePack.RETRO, false),
        new ThemeOption("tokyo-night", "Tokyo Night", ThemeTone.DARK, ThemePack.MODERN, true),
        new ThemeOption(
            "catppuccin-mocha", "Catppuccin Mocha", ThemeTone.DARK, ThemePack.MODERN, false),
        new ThemeOption("gruvbox-dark", "Gruvbox Dark", ThemeTone.DARK, ThemePack.MODERN, false),
        new ThemeOption(
            "github-soft-light", "GitHub Soft Light", ThemeTone.LIGHT, ThemePack.MODERN, true),
        new ThemeOption("blue-dark", "Flat Blue (Dark)", ThemeTone.DARK, ThemePack.IRCAFE, true),
        new ThemeOption("violet-nebula", "Violet Nebula", ThemeTone.DARK, ThemePack.IRCAFE, true),
        new ThemeOption(
            "high-contrast-dark", "High Contrast Dark", ThemeTone.DARK, ThemePack.IRCAFE, true),
        new ThemeOption("graphite-mono", "Graphite Mono", ThemeTone.DARK, ThemePack.IRCAFE, false),
        new ThemeOption("forest-dark", "Forest Dark", ThemeTone.DARK, ThemePack.IRCAFE, false),
        new ThemeOption("ruby-night", "Ruby Night", ThemeTone.DARK, ThemePack.IRCAFE, false),
        new ThemeOption(
            "solarized-dark", "Solarized Dark", ThemeTone.DARK, ThemePack.IRCAFE, false),
        new ThemeOption("sunset-dark", "Sunset Dark", ThemeTone.DARK, ThemePack.IRCAFE, false),
        new ThemeOption(
            "terminal-amber", "Terminal Amber", ThemeTone.DARK, ThemePack.IRCAFE, false),
        new ThemeOption("teal-deep", "Teal Deep", ThemeTone.DARK, ThemePack.IRCAFE, false),
        new ThemeOption("orange", "Flat Orange (Dark)", ThemeTone.DARK, ThemePack.IRCAFE, false),
        new ThemeOption("nordic-light", "Nordic Light", ThemeTone.LIGHT, ThemePack.IRCAFE, true),
        new ThemeOption("blue-light", "Flat Blue (Light)", ThemeTone.LIGHT, ThemePack.IRCAFE, true),
        new ThemeOption("arctic-light", "Arctic Light", ThemeTone.LIGHT, ThemePack.IRCAFE, false),
        new ThemeOption("mint-light", "Mint Light", ThemeTone.LIGHT, ThemePack.IRCAFE, false),
        new ThemeOption(
            "solarized-light", "Solarized Light", ThemeTone.LIGHT, ThemePack.IRCAFE, false));
  }
}

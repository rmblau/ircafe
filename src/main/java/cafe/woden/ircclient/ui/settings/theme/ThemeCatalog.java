package cafe.woden.ircclient.ui.settings.theme;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeOption;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemePack;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeTone;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@InterfaceLayer
@Lazy
class ThemeCatalog {

  private record LegacySystemThemeDefinition(
      String id, String label, ThemeTone tone, String lafClassName, boolean featured) {}

  private static final String NIMBUS_LAF_CLASS = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
  private static final String METAL_LAF_CLASS = "javax.swing.plaf.metal.MetalLookAndFeel";
  private static final String MOTIF_LAF_CLASS = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
  private static final String WINDOWS_LAF_CLASS =
      "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
  private static final String GTK_LAF_CLASS = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";

  private static final LegacySystemThemeDefinition[] LEGACY_SYSTEM_THEME_DEFINITIONS =
      new LegacySystemThemeDefinition[] {
        new LegacySystemThemeDefinition(
            "nimbus", "Nimbus", ThemeTone.LIGHT, NIMBUS_LAF_CLASS, true),
        new LegacySystemThemeDefinition(
            "nimbus-dark", "Nimbus (Dark)", ThemeTone.DARK, NIMBUS_LAF_CLASS, false),
        new LegacySystemThemeDefinition(
            "nimbus-dark-amber", "Nimbus (Dark Amber)", ThemeTone.DARK, NIMBUS_LAF_CLASS, false),
        new LegacySystemThemeDefinition(
            "nimbus-dark-blue", "Nimbus (Dark Blue)", ThemeTone.DARK, NIMBUS_LAF_CLASS, false),
        new LegacySystemThemeDefinition(
            "nimbus-dark-violet", "Nimbus (Dark Violet)", ThemeTone.DARK, NIMBUS_LAF_CLASS, false),
        new LegacySystemThemeDefinition(
            "nimbus-dark-green", "Nimbus (Dark Green)", ThemeTone.DARK, NIMBUS_LAF_CLASS, false),
        new LegacySystemThemeDefinition(
            "nimbus-dark-orange", "Nimbus (Dark Orange)", ThemeTone.DARK, NIMBUS_LAF_CLASS, false),
        new LegacySystemThemeDefinition(
            "nimbus-dark-magenta",
            "Nimbus (Dark Magenta)",
            ThemeTone.DARK,
            NIMBUS_LAF_CLASS,
            false),
        new LegacySystemThemeDefinition(
            "nimbus-orange", "Nimbus (Orange)", ThemeTone.LIGHT, NIMBUS_LAF_CLASS, false),
        new LegacySystemThemeDefinition(
            "nimbus-green", "Nimbus (Green)", ThemeTone.LIGHT, NIMBUS_LAF_CLASS, false),
        new LegacySystemThemeDefinition(
            "nimbus-blue", "Nimbus (Blue)", ThemeTone.LIGHT, NIMBUS_LAF_CLASS, false),
        new LegacySystemThemeDefinition(
            "nimbus-violet", "Nimbus (Violet)", ThemeTone.LIGHT, NIMBUS_LAF_CLASS, false),
        new LegacySystemThemeDefinition(
            "nimbus-magenta", "Nimbus (Magenta)", ThemeTone.LIGHT, NIMBUS_LAF_CLASS, false),
        new LegacySystemThemeDefinition(
            "nimbus-amber", "Nimbus (Amber)", ThemeTone.LIGHT, NIMBUS_LAF_CLASS, false),
        new LegacySystemThemeDefinition(
            "metal-ocean", "Metal (Ocean)", ThemeTone.LIGHT, METAL_LAF_CLASS, true),
        new LegacySystemThemeDefinition(
            "metal-steel", "Metal (Steel)", ThemeTone.LIGHT, METAL_LAF_CLASS, false),
        new LegacySystemThemeDefinition("motif", "Motif", ThemeTone.LIGHT, MOTIF_LAF_CLASS, true),
        new LegacySystemThemeDefinition(
            "windows", "Windows Classic", ThemeTone.SYSTEM, WINDOWS_LAF_CLASS, false),
        new LegacySystemThemeDefinition("gtk", "GTK", ThemeTone.SYSTEM, GTK_LAF_CLASS, false)
      };

  private final List<ThemeOption> builtInThemeOptions;
  private final List<ThemeOption> pluginThemeOptions;
  private volatile ThemeOption[] cachedThemes;
  private volatile ThemeOption[] cachedThemesWithAllIntelliJ;

  ThemeCatalog() {
    this((InstalledPluginsPort) null);
  }

  @Autowired
  ThemeCatalog(ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(resolveInstalledPlugins(installedPluginsProvider));
  }

  ThemeCatalog(InstalledPluginsPort installedPlugins) {
    this.builtInThemeOptions = ThemeContributionProviders.builtInThemeOptions(installedPlugins);
    this.pluginThemeOptions = ThemeContributionProviders.pluginThemeOptions(installedPlugins);
  }

  ThemeOption[] supportedThemes() {
    return allThemes().clone();
  }

  ThemeOption[] featuredThemes() {
    ThemeOption[] all = allThemes();
    List<ThemeOption> featured = Arrays.stream(all).filter(ThemeOption::featured).toList();

    List<ThemeOption> out = new ArrayList<>(featured.size());
    addFeaturedById(out, featured, "darcula");
    addFeaturedById(out, featured, "darklaf");

    for (ThemeOption t : featured) {
      if (t == null || t.id() == null) continue;
      if ("darcula".equalsIgnoreCase(t.id())) continue;
      if ("darklaf".equalsIgnoreCase(t.id())) continue;
      out.add(t);
    }

    return out.toArray(ThemeOption[]::new);
  }

  ThemeOption[] themesForPicker(boolean includeAllIntelliJThemes) {
    if (!includeAllIntelliJThemes) {
      return supportedThemes();
    }

    ThemeOption[] cached = cachedThemesWithAllIntelliJ;
    if (cached != null) return cached.clone();

    List<ThemeOption> out = new ArrayList<>();
    out.addAll(builtInThemeOptions);
    out.addAll(darkLafThemes());
    out.addAll(legacySystemThemes());

    List<IntelliJThemePack.PackTheme> pack = IntelliJThemePack.listThemes();
    if (!pack.isEmpty()) {
      Set<String> seen = new HashSet<>();
      for (ThemeOption o : out) {
        if (o != null && o.id() != null) seen.add(o.id());
      }

      for (IntelliJThemePack.PackTheme t : pack) {
        if (t == null || t.id() == null || t.id().isBlank()) continue;
        if (!seen.add(t.id())) continue;

        ThemeTone tone = t.dark() ? ThemeTone.DARK : ThemeTone.LIGHT;
        out.add(new ThemeOption(t.id(), "IntelliJ: " + t.label(), tone, ThemePack.INTELLIJ, false));
      }
    }

    addPluginThemeOptions(out);

    cached = out.toArray(ThemeOption[]::new);
    cachedThemesWithAllIntelliJ = cached;
    return cached.clone();
  }

  private ThemeOption[] allThemes() {
    ThemeOption[] cached = cachedThemes;
    if (cached != null) return cached;

    List<ThemeOption> out = new ArrayList<>();
    out.addAll(builtInThemeOptions);
    out.addAll(darkLafThemes());
    out.addAll(legacySystemThemes());
    out.addAll(buildCuratedIntelliJThemes());
    addPluginThemeOptions(out);

    cached = out.toArray(ThemeOption[]::new);
    cachedThemes = cached;
    return cached;
  }

  private void addPluginThemeOptions(List<ThemeOption> out) {
    if (out == null || pluginThemeOptions.isEmpty()) return;

    Set<String> seen = new HashSet<>();
    for (ThemeOption option : out) {
      if (option != null && option.id() != null) {
        seen.add(option.id().toLowerCase(Locale.ROOT));
      }
    }

    for (ThemeOption option : pluginThemeOptions) {
      if (option == null || option.id() == null || option.id().isBlank()) continue;
      if (!seen.add(option.id().toLowerCase(Locale.ROOT))) continue;
      out.add(option);
    }
  }

  private static InstalledPluginsPort resolveInstalledPlugins(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    return installedPluginsProvider != null ? installedPluginsProvider.getIfAvailable() : null;
  }

  private static List<ThemeOption> darkLafThemes() {
    if (!DarkLafSupport.isAvailable()) return List.of();
    return List.of(
        new ThemeOption("darklaf", "DarkLaf (One Dark)", ThemeTone.DARK, ThemePack.DARKLAF, true),
        new ThemeOption(
            "darklaf-darcula", "DarkLaf (Darcula)", ThemeTone.DARK, ThemePack.DARKLAF, false),
        new ThemeOption(
            "darklaf-solarized-dark",
            "DarkLaf (Solarized Dark)",
            ThemeTone.DARK,
            ThemePack.DARKLAF,
            false),
        new ThemeOption(
            "darklaf-high-contrast-dark",
            "DarkLaf (High Contrast Dark)",
            ThemeTone.DARK,
            ThemePack.DARKLAF,
            false),
        new ThemeOption(
            "darklaf-light",
            "DarkLaf (Solarized Light)",
            ThemeTone.LIGHT,
            ThemePack.DARKLAF,
            false),
        new ThemeOption(
            "darklaf-high-contrast-light",
            "DarkLaf (High Contrast Light)",
            ThemeTone.LIGHT,
            ThemePack.DARKLAF,
            false),
        new ThemeOption(
            "darklaf-intellij", "DarkLaf (IntelliJ)", ThemeTone.LIGHT, ThemePack.DARKLAF, false));
  }

  private static List<ThemeOption> legacySystemThemes() {
    Set<String> installed = ThemeLookAndFeelUtils.installedLookAndFeelClassNames();
    if (installed.isEmpty()) return List.of();

    List<ThemeOption> out = new ArrayList<>();
    for (LegacySystemThemeDefinition def : LEGACY_SYSTEM_THEME_DEFINITIONS) {
      if (def == null || def.lafClassName() == null || def.lafClassName().isBlank()) continue;
      if (!installed.contains(def.lafClassName().toLowerCase(Locale.ROOT))) continue;
      out.add(new ThemeOption(def.id(), def.label(), def.tone(), ThemePack.SYSTEM, def.featured()));
    }

    return out;
  }

  private static List<ThemeOption> buildCuratedIntelliJThemes() {
    List<IntelliJThemePack.PackTheme> pack = IntelliJThemePack.listThemes();
    if (pack.isEmpty()) return List.of();

    String[] priority =
        new String[] {
          "tokyo night",
          "catppuccin",
          "gruvbox",
          "github dark",
          "github light",
          "one dark",
          "dracula",
          "arc dark",
          "monokai",
          "nord",
          "solarized dark",
          "solarized light",
          "gradianto",
          "github",
          "material",
          "cobalt"
        };

    final int maxThemes = 16;

    Set<String> chosenIds = new HashSet<>();
    List<ThemeOption> curated = new ArrayList<>();

    java.util.function.Consumer<IntelliJThemePack.PackTheme> add =
        t -> {
          if (t == null) return;
          if (!chosenIds.add(t.id())) return;

          ThemeTone tone = t.dark() ? ThemeTone.DARK : ThemeTone.LIGHT;
          boolean featured = curated.size() < 3;

          curated.add(
              new ThemeOption(
                  t.id(), "IntelliJ: " + t.label(), tone, ThemePack.INTELLIJ, featured));
        };

    for (String fragment : priority) {
      if (curated.size() >= maxThemes) break;
      String lowerFragment = fragment.toLowerCase(Locale.ROOT);

      for (IntelliJThemePack.PackTheme t : pack) {
        if (t == null) continue;

        String name = t.label() != null ? t.label().toLowerCase(Locale.ROOT) : "";
        String className =
            t.lafClassName() != null ? t.lafClassName().toLowerCase(Locale.ROOT) : "";
        if (name.contains(lowerFragment) || className.contains(lowerFragment.replace(" ", ""))) {
          add.accept(t);
          break;
        }
      }
    }

    if (curated.size() < maxThemes) {
      for (IntelliJThemePack.PackTheme t : pack) {
        if (curated.size() >= maxThemes) break;
        if (t != null && t.dark()) add.accept(t);
      }
      for (IntelliJThemePack.PackTheme t : pack) {
        if (curated.size() >= maxThemes) break;
        if (t != null && !t.dark()) add.accept(t);
      }
    }

    return curated;
  }

  private static void addFeaturedById(
      List<ThemeOption> out, List<ThemeOption> featured, String wantedId) {
    if (out == null || featured == null || wantedId == null || wantedId.isBlank()) return;

    for (ThemeOption t : featured) {
      if (t == null || t.id() == null) continue;
      if (t.id().equalsIgnoreCase(wantedId)) {
        out.add(t);
        return;
      }
    }
  }
}

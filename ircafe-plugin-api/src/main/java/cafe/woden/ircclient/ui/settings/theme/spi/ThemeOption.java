package cafe.woden.ircclient.ui.settings.theme.spi;

/** Plugin-facing theme picker option contribution. */
public record ThemeOption(
    String id, String label, ThemeTone tone, ThemePack pack, boolean featured) {
  public boolean isDark() {
    return tone == ThemeTone.DARK;
  }
}

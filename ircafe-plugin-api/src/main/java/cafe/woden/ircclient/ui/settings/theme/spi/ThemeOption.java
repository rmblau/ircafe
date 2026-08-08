package cafe.woden.ircclient.ui.settings.theme.spi;

/**
 * Plugin-facing theme picker option contribution.
 *
 * @param id stable non-blank id; IRCafe compares ids case-insensitively
 * @param label user-visible picker label
 * @param tone broad light/dark/system classification used for filtering
 * @param pack picker group; external contributions should normally use {@link ThemePack#PLUGIN}
 * @param featured whether the option may appear in the curated featured list
 */
public record ThemeOption(
    String id, String label, ThemeTone tone, ThemePack pack, boolean featured) {
  public boolean isDark() {
    return tone == ThemeTone.DARK;
  }
}

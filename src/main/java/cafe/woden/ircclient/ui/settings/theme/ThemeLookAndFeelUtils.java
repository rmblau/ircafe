package cafe.woden.ircclient.ui.settings.theme;

import cafe.woden.ircclient.ui.settings.SettingsValueSupport;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

final class ThemeLookAndFeelUtils {

  private static final String NIMBUS_DEBUG_PROPERTY = "ircafe.debug.nimbus";
  private static final String NIMBUS_DEBUG_ENV = "IRCAFE_DEBUG_NIMBUS";

  private ThemeLookAndFeelUtils() {}

  static String currentLookAndFeelClassName() {
    LookAndFeel laf = UIManager.getLookAndFeel();
    return laf != null ? laf.getClass().getName() : null;
  }

  static Set<String> installedLookAndFeelClassNames() {
    Set<String> out = new HashSet<>();
    try {
      UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
      if (infos == null) return out;

      for (UIManager.LookAndFeelInfo info : infos) {
        if (info == null || info.getClassName() == null || info.getClassName().isBlank()) continue;
        out.add(info.getClassName().toLowerCase(Locale.ROOT));
      }
    } catch (Exception ignored) {
    }
    return out;
  }

  static boolean isNimbusDebugEnabled() {
    return isTruthy(System.getProperty(NIMBUS_DEBUG_PROPERTY))
        || isTruthy(System.getenv(NIMBUS_DEBUG_ENV));
  }

  private static boolean isTruthy(String raw) {
    String s = SettingsValueSupport.lowerTrimmedString(raw);
    return "1".equals(s) || "true".equals(s) || "yes".equals(s) || "on".equals(s);
  }
}

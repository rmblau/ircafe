package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisted appearance/theme settings. */
@SecondaryPort
@ApplicationLayer
public interface AppearanceRuntimeConfigPort {

  void rememberAccentColor(String accentColor);

  void rememberAccentStrength(int strength);

  void rememberUiDensity(String density);

  void rememberUiFontOverrideEnabled(boolean enabled);

  void rememberUiFontFamily(String family);

  void rememberUiFontSize(int size);

  void rememberCornerRadius(int cornerRadius);

  void rememberChatThemePreset(String preset);

  void rememberChatTimestampColor(String hex);

  void rememberChatSystemColor(String hex);

  void rememberChatMessageColor(String hex);

  void rememberChatNoticeColor(String hex);

  void rememberChatActionColor(String hex);

  void rememberChatErrorColor(String hex);

  void rememberChatPresenceColor(String hex);

  void rememberChatMentionBgColor(String hex);

  void rememberChatMentionStrength(int strength);

  void rememberServerTreeUnreadChannelColor(String hex);

  void rememberServerTreeHighlightChannelColor(String hex);

  void rememberPreserveDockLayout(boolean preserveDockLayout);
}

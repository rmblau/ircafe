package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisted chat transcript palette settings. */
@SecondaryPort
@ApplicationLayer
public interface ChatAppearanceRuntimeConfigPort {

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
}

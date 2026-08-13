package cafe.woden.ircclient.config.api;

import java.util.Objects;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisted chat behavior preferences. */
@SecondaryPort
@ApplicationLayer
public interface ChatBehaviorRuntimeConfigPort extends QuitMessageRuntimeConfigPort {

  boolean readNickCompletionCycleWithTabEnabled(boolean defaultValue);

  boolean readNickCompletionAppendAddressSuffixEnabled(boolean defaultValue);

  int readServerTreeUnreadBadgeScalePercent(int defaultValue);

  void rememberPresenceFoldsEnabled(boolean enabled);

  void rememberCtcpRequestsInActiveTargetEnabled(boolean enabled);

  void rememberDefaultQuitMessage(String message);

  void rememberNickCompletionCycleWithTabEnabled(boolean enabled);

  void rememberNickCompletionAppendAddressSuffixEnabled(boolean enabled);

  void rememberTypingIndicatorsEnabled(boolean enabled);

  void rememberTypingIndicatorsReceiveEnabled(boolean enabled);

  void rememberTypingTreeIndicatorStyle(String style);

  void rememberTypingIndicatorsTreeEnabled(boolean enabled);

  void rememberTypingIndicatorsUsersListEnabled(boolean enabled);

  void rememberMatrixUserListNameDisplayMode(String mode);

  void rememberTypingIndicatorsTranscriptEnabled(boolean enabled);

  void rememberTypingIndicatorsSendSignalEnabled(boolean enabled);

  void rememberServerTreeUnreadBadgeScalePercent(int scalePercent);

  void rememberServerTreeNotificationBadgesEnabled(boolean enabled);

  default String normalizeDefaultQuitMessage(String raw) {
    String message = Objects.toString(raw, "").replace('\r', ' ').replace('\n', ' ').trim();
    return message.isEmpty() ? DEFAULT_QUIT_MESSAGE : message;
  }
}

package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for CTCP auto-reply policy. */
@SecondaryPort
@ApplicationLayer
public interface CtcpReplyRuntimeConfigPort {

  boolean readCtcpAutoRepliesEnabled(boolean defaultValue);

  boolean readCtcpAutoReplyVersionEnabled(boolean defaultValue);

  boolean readCtcpAutoReplyPingEnabled(boolean defaultValue);

  boolean readCtcpAutoReplyTimeEnabled(boolean defaultValue);

  void rememberCtcpAutoRepliesEnabled(boolean enabled);

  void rememberCtcpAutoReplyVersionEnabled(boolean enabled);

  void rememberCtcpAutoReplyPingEnabled(boolean enabled);

  void rememberCtcpAutoReplyTimeEnabled(boolean enabled);
}

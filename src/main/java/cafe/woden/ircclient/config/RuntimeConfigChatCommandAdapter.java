package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.api.ChatCommandRuntimeConfigPort;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/**
 * Secondary adapter for outbound chat command runtime settings backed by {@link
 * RuntimeConfigStore}.
 */
@Component
@SecondaryAdapter
@ApplicationLayer
public final class RuntimeConfigChatCommandAdapter implements ChatCommandRuntimeConfigPort {
  private final RuntimeConfigStore runtimeConfig;

  public RuntimeConfigChatCommandAdapter(RuntimeConfigStore runtimeConfig) {
    this.runtimeConfig = runtimeConfig;
  }

  @Override
  public void rememberJoinedChannel(String serverId, String channel) {
    runtimeConfig.rememberJoinedChannel(serverId, channel);
  }

  @Override
  public void rememberNick(String serverId, String nick) {
    runtimeConfig.rememberNick(serverId, nick);
  }

  @Override
  public void rememberInviteAutoJoinEnabled(boolean enabled) {
    runtimeConfig.rememberInviteAutoJoinEnabled(enabled);
  }

  @Override
  public String readDefaultQuitMessage() {
    return runtimeConfig.readDefaultQuitMessage();
  }
}

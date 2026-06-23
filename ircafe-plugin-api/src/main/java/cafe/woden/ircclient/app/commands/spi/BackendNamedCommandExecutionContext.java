package cafe.woden.ircclient.app.commands.spi;

import java.util.Objects;

/** Portable execution context exposed to backend-scoped named command plugins. */
public interface BackendNamedCommandExecutionContext {

  SlashCommandTargetView activeTarget();

  SlashCommandTargetView safeStatusTarget();

  default SlashCommandTargetView statusTarget(String serverId) {
    String sid = Objects.toString(serverId, "").trim();
    return sid.isEmpty() ? safeStatusTarget() : new SlashCommandTargetView(sid, "status");
  }

  default SlashCommandTargetView activeTargetOrSafeStatusTarget() {
    SlashCommandTargetView active = activeTarget();
    return active != null ? active : safeStatusTarget();
  }

  boolean isConnected(String serverId);

  void appendStatus(SlashCommandTargetView target, String prefix, String message);

  void appendError(SlashCommandTargetView target, String prefix, String message);

  void ensureTargetExists(SlashCommandTargetView target);

  void selectTarget(SlashCommandTargetView target);

  void sendRaw(String serverId, String line);
}

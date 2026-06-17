package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.mutation.MessageMutationOutboundCommands;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Router for backend-specific message mutation outbound command handlers. */
@Component
@ApplicationLayer
@RequiredArgsConstructor
public final class MessageMutationOutboundCommandsRouter {
  @NonNull private final BackendExtensionCatalog backendExtensionCatalog;

  public MessageMutationOutboundCommands commandsFor(String backendId) {
    return backendExtensionCatalog.messageMutationCommandsFor(backendId);
  }
}

package cafe.woden.ircclient.app.outbound.spi;

import cafe.woden.ircclient.app.commands.FilterCommand;

/** App-owned handler contract for local /filter commands. */
public interface LocalFilterCommandHandler {

  void handle(FilterCommand command);
}

package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Set;

/** Built-in runtime provider for stable, draft, client-only, and backend message-ID tags. */
@AutoService(Ircv3InboundTagSignalProvider.class)
public final class Ircv3MessageIdRuntimeProvider implements Ircv3InboundTagSignalProvider {

  @Override
  public String providerId() {
    return "message-id";
  }

  @Override
  public Set<Ircv3InboundTagOperation> inboundTagOperations() {
    return Set.of(Ircv3InboundTagOperation.MESSAGE_ID);
  }

  @Override
  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation != Ircv3InboundTagOperation.MESSAGE_ID || request == null) {
      return List.of();
    }
    String messageId = Ircv3MessageIdTagPolicy.firstMessageId(request.tags());
    return messageId.isEmpty()
        ? List.of()
        : List.of(Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.MESSAGE_ID, messageId));
  }
}

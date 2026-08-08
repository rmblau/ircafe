package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Set;

/** Built-in metadata provider for experimental IRCv3 message editing. */
@AutoService({
  Ircv3ExtensionProvider.class,
  Ircv3MessageMutationProvider.class,
  Ircv3InboundTagSignalProvider.class
})
public final class Ircv3MessageEditExtensionProvider
    implements Ircv3ExtensionProvider,
        Ircv3MessageMutationProvider,
        Ircv3InboundTagSignalProvider {

  private static final String FEATURE = "message-edit";
  private static final String DRAFT_FEATURE = "draft/message-edit";

  @Override
  public String providerId() {
    return FEATURE;
  }

  @Override
  public int sortOrder() {
    return 290;
  }

  @Override
  public Set<Ircv3MessageMutationOperation> operations() {
    return Set.of(Ircv3MessageMutationOperation.EDIT);
  }

  @Override
  public String build(
      Ircv3MessageMutationOperation operation, Ircv3MessageMutationRequest request) {
    if (operation != Ircv3MessageMutationOperation.EDIT || request == null) {
      return "";
    }
    return Ircv3MessageEditCommandBuilder.buildRawLine(
        request.target(), request.messageId(), request.payload());
  }

  @Override
  public Set<Ircv3InboundTagOperation> inboundTagOperations() {
    return Set.of(Ircv3InboundTagOperation.MESSAGE_EDIT);
  }

  @Override
  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation != Ircv3InboundTagOperation.MESSAGE_EDIT || request == null) {
      return List.of();
    }
    return Ircv3MessageEditTagSignal.fromTags(request.tags())
        .map(
            signal ->
                List.of(
                    Ircv3InboundTagSignal.of(
                        Ircv3InboundTagSignalType.MESSAGE_EDIT, signal.targetMessageId())))
        .orElseGet(List::of);
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        new Ircv3ExtensionContribution(
            FEATURE,
            Ircv3ExtensionKind.EXPERIMENTAL,
            Ircv3SpecStatus.EXPERIMENTAL,
            List.of(DRAFT_FEATURE),
            "",
            FEATURE,
            new Ircv3UiMetadata(
                "Message edits (experimental)",
                Ircv3UiGroup.CONVERSATION,
                280,
                "Experimental message editing support; not part of the published IRCv3 surface.")));
  }
}

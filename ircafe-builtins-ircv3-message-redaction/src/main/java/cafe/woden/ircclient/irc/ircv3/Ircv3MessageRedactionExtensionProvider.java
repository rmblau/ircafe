package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
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

/** Built-in metadata provider for the IRCv3 message-redaction draft capability. */
@AutoService({
  Ircv3ExtensionProvider.class,
  Ircv3MessageMutationProvider.class,
  Ircv3InboundTagSignalProvider.class,
  Ircv3InboundCommandSignalProvider.class
})
public final class Ircv3MessageRedactionExtensionProvider
    implements Ircv3ExtensionProvider,
        Ircv3MessageMutationProvider,
        Ircv3InboundTagSignalProvider,
        Ircv3InboundCommandSignalProvider {

  private static final String CAPABILITY = "message-redaction";
  private static final String DRAFT_CAPABILITY = "draft/message-redaction";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 220;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        new Ircv3ExtensionContribution(
            CAPABILITY,
            Ircv3ExtensionKind.CAPABILITY,
            Ircv3SpecStatus.DRAFT,
            List.of(DRAFT_CAPABILITY),
            DRAFT_CAPABILITY,
            CAPABILITY,
            new Ircv3UiMetadata(
                "Message redaction (draft)",
                Ircv3UiGroup.CONVERSATION,
                300,
                "Allows delete/redaction updates for messages.")));
  }

  @Override
  public Set<Ircv3MessageMutationOperation> operations() {
    return Set.of(Ircv3MessageMutationOperation.REDACT);
  }

  @Override
  public String build(
      Ircv3MessageMutationOperation operation, Ircv3MessageMutationRequest request) {
    if (operation != Ircv3MessageMutationOperation.REDACT || request == null) {
      return "";
    }
    return Ircv3MessageRedactionCommandBuilder.buildRawLine(
        request.target(), request.messageId(), request.payload());
  }

  @Override
  public Set<Ircv3InboundTagOperation> inboundTagOperations() {
    return Set.of(Ircv3InboundTagOperation.MESSAGE_REDACTION);
  }

  @Override
  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation != Ircv3InboundTagOperation.MESSAGE_REDACTION
        || request == null
        || !request.isMessageLikeCommand()) {
      return List.of();
    }
    return Ircv3MessageRedactionTagSignal.fromTags(request.tags())
        .map(
            signal ->
                List.of(
                    Ircv3InboundTagSignal.of(
                        Ircv3InboundTagSignalType.MESSAGE_REDACTION, signal.messageId())))
        .orElseGet(List::of);
  }

  @Override
  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
    return Set.of(Ircv3InboundCommandOperation.MESSAGE_REDACTION);
  }

  @Override
  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (operation != Ircv3InboundCommandOperation.MESSAGE_REDACTION || request == null) {
      return List.of();
    }
    return Ircv3MessageRedactionCommandSignal.parse(request.command(), request.parameters())
        .map(
            signal ->
                List.<Ircv3InboundCommandSignal>of(
                    new Ircv3InboundCommandSignal.MessageRedactionObserved(
                        signal.target(), signal.messageId())))
        .orElseGet(List::of);
  }

  @Override
  public List<Ircv3FeatureContribution> visibleFeatures() {
    return List.of(
        new Ircv3FeatureContribution(
            400,
            "Message redaction",
            List.of(),
            List.of(CAPABILITY, DRAFT_CAPABILITY)));
  }
}

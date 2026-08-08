package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
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

/** Built-in metadata provider for the IRCv3 reply message-tag feature. */
@AutoService({
  Ircv3ExtensionProvider.class,
  Ircv3MessageMutationProvider.class,
  Ircv3InboundTagSignalProvider.class
})
public final class Ircv3ReplyExtensionProvider
    implements Ircv3ExtensionProvider,
        Ircv3MessageMutationProvider,
        Ircv3InboundTagSignalProvider {

  private static final String FEATURE = "reply";
  private static final String LEGACY_FEATURE = "draft/reply";
  private static final String MESSAGE_TAGS = "message-tags";

  @Override
  public String providerId() {
    return FEATURE;
  }

  @Override
  public int sortOrder() {
    return 250;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        new Ircv3ExtensionContribution(
            FEATURE,
            Ircv3ExtensionKind.TAG_FEATURE,
            Ircv3SpecStatus.STABLE,
            List.of(LEGACY_FEATURE),
            "",
            FEATURE,
            new Ircv3UiMetadata(
                "Replies",
                Ircv3UiGroup.CONVERSATION,
                250,
                "Reply threading is carried by message tags on top of message-tags transport.")));
  }

  @Override
  public Set<Ircv3MessageMutationOperation> operations() {
    return Set.of(Ircv3MessageMutationOperation.REPLY);
  }

  @Override
  public String build(
      Ircv3MessageMutationOperation operation, Ircv3MessageMutationRequest request) {
    if (operation != Ircv3MessageMutationOperation.REPLY || request == null) {
      return "";
    }
    return Ircv3ReplyCommandBuilder.buildRawLine(
        request.target(), request.messageId(), request.payload());
  }

  @Override
  public Set<Ircv3InboundTagOperation> inboundTagOperations() {
    return Set.of(Ircv3InboundTagOperation.REPLY);
  }

  @Override
  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation != Ircv3InboundTagOperation.REPLY
        || request == null
        || !request.isMessageLikeCommand()) {
      return List.of();
    }
    return Ircv3ReplyTagSignal.fromTags(request.tags())
        .map(
            signal ->
                List.of(
                    Ircv3InboundTagSignal.of(
                        Ircv3InboundTagSignalType.REPLY, signal.replyToMessageId())))
        .orElseGet(List::of);
  }

  @Override
  public List<Ircv3FeatureContribution> visibleFeatures() {
    return List.of(
        new Ircv3FeatureContribution(100, "Replies", List.of(MESSAGE_TAGS), List.of()));
  }
}

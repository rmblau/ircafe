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

/** Built-in metadata provider for IRCv3 reaction message-tag features. */
@AutoService({
  Ircv3ExtensionProvider.class,
  Ircv3MessageMutationProvider.class,
  Ircv3InboundTagSignalProvider.class
})
public final class Ircv3ReactionsExtensionProvider
    implements Ircv3ExtensionProvider,
        Ircv3MessageMutationProvider,
        Ircv3InboundTagSignalProvider {

  private static final String REACT = "react";
  private static final String DRAFT_REACT = "draft/react";
  private static final String UNREACT = "unreact";
  private static final String DRAFT_UNREACT = "draft/unreact";
  private static final String MESSAGE_TAGS = "message-tags";

  @Override
  public String providerId() {
    return "reactions";
  }

  @Override
  public int sortOrder() {
    return 260;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        tagFeature(
            REACT,
            DRAFT_REACT,
            "Reactions",
            260,
            "Reactions are carried by message tags on top of message-tags transport."),
        tagFeature(
            UNREACT,
            DRAFT_UNREACT,
            "Reaction removal",
            265,
            "Reaction removals are carried by message tags on top of message-tags transport."));
  }

  @Override
  public Set<Ircv3MessageMutationOperation> operations() {
    return Set.of(
        Ircv3MessageMutationOperation.REACT, Ircv3MessageMutationOperation.UNREACT);
  }

  @Override
  public String build(
      Ircv3MessageMutationOperation operation, Ircv3MessageMutationRequest request) {
    if (operation == null || request == null) {
      return "";
    }
    return switch (operation) {
      case REACT ->
          Ircv3ReactionCommandBuilder.buildReactRawLine(
              request.target(), request.messageId(), request.payload());
      case UNREACT ->
          Ircv3ReactionCommandBuilder.buildUnreactRawLine(
              request.target(), request.messageId(), request.payload());
      default -> "";
    };
  }

  @Override
  public Set<Ircv3InboundTagOperation> inboundTagOperations() {
    return Set.of(Ircv3InboundTagOperation.REACTIONS);
  }

  @Override
  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation != Ircv3InboundTagOperation.REACTIONS
        || request == null
        || !request.isMessageLikeCommand()) {
      return List.of();
    }
    return Ircv3ReactionTagSignal.fromTags(request.tags()).stream()
        .map(
            signal ->
                new Ircv3InboundTagSignal(
                    signal.operation() == Ircv3ReactionTagSignal.Operation.REACT
                        ? Ircv3InboundTagSignalType.REACT
                        : Ircv3InboundTagSignalType.UNREACT,
                    signal.reaction(),
                    signal.messageId()))
        .toList();
  }

  @Override
  public List<Ircv3FeatureContribution> visibleFeatures() {
    return List.of(
        new Ircv3FeatureContribution(200, "Reactions", List.of(MESSAGE_TAGS), List.of()),
        new Ircv3FeatureContribution(
            300, "Reaction removal", List.of(MESSAGE_TAGS), List.of()));
  }

  private static Ircv3ExtensionContribution tagFeature(
      String id, String alias, String label, int sortOrder, String impactSummary) {
    return new Ircv3ExtensionContribution(
        id,
        Ircv3ExtensionKind.TAG_FEATURE,
        Ircv3SpecStatus.DRAFT,
        List.of(alias),
        "",
        id,
        new Ircv3UiMetadata(
            label, Ircv3UiGroup.CONVERSATION, sortOrder, impactSummary));
  }
}

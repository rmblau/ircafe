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
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Set;

/** Built-in metadata and runtime provider for the IRCv3 typing message-tag feature. */
@AutoService({
  Ircv3ExtensionProvider.class,
  Ircv3OutboundCommandProvider.class,
  Ircv3InboundTagSignalProvider.class,
  Ircv3InboundCommandSignalProvider.class
})
public final class Ircv3TypingExtensionProvider
    implements Ircv3ExtensionProvider,
        Ircv3OutboundCommandProvider,
        Ircv3InboundTagSignalProvider,
        Ircv3InboundCommandSignalProvider {

  private static final String FEATURE = "typing";
  private static final String DRAFT_FEATURE = "draft/typing";
  private static final String MESSAGE_TAGS = "message-tags";

  @Override
  public String providerId() {
    return FEATURE;
  }

  @Override
  public int sortOrder() {
    return 270;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        new Ircv3ExtensionContribution(
            FEATURE,
            Ircv3ExtensionKind.TAG_FEATURE,
            Ircv3SpecStatus.STABLE,
            List.of(DRAFT_FEATURE),
            "",
            FEATURE,
            new Ircv3UiMetadata(
                "Typing",
                Ircv3UiGroup.CONVERSATION,
                230,
                "Typing indicators are sent as client-only tags and depend on CLIENTTAGDENY policy.")));
  }

  @Override
  public Set<Ircv3OutboundCommandOperation> operations() {
    return Set.of(Ircv3OutboundCommandOperation.TYPING);
  }

  @Override
  public List<String> build(
      Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
    if (operation != Ircv3OutboundCommandOperation.TYPING || request == null) {
      return List.of();
    }
    String line = Ircv3TypingCommandBuilder.buildRawLine(request.target(), request.primaryValue());
    return line.isEmpty() ? List.of() : List.of(line);
  }

  @Override
  public Set<Ircv3InboundTagOperation> inboundTagOperations() {
    return Set.of(Ircv3InboundTagOperation.TYPING);
  }

  @Override
  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation != Ircv3InboundTagOperation.TYPING
        || request == null
        || !request.isMessageLikeCommand()) {
      return List.of();
    }
    return Ircv3TypingTagSignal.fromTags(request.tags())
        .map(signal -> Ircv3TypingCommandBuilder.normalizeState(signal.state()))
        .filter(state -> !state.isEmpty())
        .map(state -> List.of(Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.TYPING, state)))
        .orElseGet(List::of);
  }

  @Override
  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
    return Set.of(Ircv3InboundCommandOperation.ISUPPORT_CLIENT_TAG_POLICY);
  }

  @Override
  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (operation != Ircv3InboundCommandOperation.ISUPPORT_CLIENT_TAG_POLICY || request == null) {
      return List.of();
    }
    Ircv3TypingClientTagPolicy.Observation policy =
        Ircv3TypingClientTagPolicy.parseRpl005(request.rawLine());
    if (policy == null) {
      return List.of();
    }
    return List.of(
        new Ircv3InboundCommandSignal.ClientTagPolicyObserved(
            FEATURE, policy.allowed(), policy.rawDenyValue()));
  }

  @Override
  public List<Ircv3FeatureContribution> visibleFeatures() {
    return List.of(new Ircv3FeatureContribution(600, "Typing", List.of(MESSAGE_TAGS), List.of()));
  }
}

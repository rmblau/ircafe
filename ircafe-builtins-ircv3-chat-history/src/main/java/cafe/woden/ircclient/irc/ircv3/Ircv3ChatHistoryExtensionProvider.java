package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import com.google.auto.service.AutoService;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/** Built-in metadata and runtime provider for the IRCv3 chathistory draft capability. */
@AutoService({Ircv3ExtensionProvider.class, Ircv3OutboundCommandProvider.class})
public final class Ircv3ChatHistoryExtensionProvider
    implements Ircv3ExtensionProvider, Ircv3OutboundCommandProvider {

  private static final String CAPABILITY = "chathistory";
  private static final String DRAFT_CAPABILITY = "draft/chathistory";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 240;
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
                "Chat history (draft)",
                Ircv3UiGroup.HISTORY,
                430,
                "Enables server-side history retrieval and backfill features.")));
  }

  @Override
  public Set<Ircv3OutboundCommandOperation> operations() {
    return Set.of(
        Ircv3OutboundCommandOperation.CHAT_HISTORY_BEFORE,
        Ircv3OutboundCommandOperation.CHAT_HISTORY_LATEST,
        Ircv3OutboundCommandOperation.CHAT_HISTORY_BETWEEN,
        Ircv3OutboundCommandOperation.CHAT_HISTORY_AROUND);
  }

  @Override
  public List<String> build(
      Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
    if (operation == null || request == null) {
      return List.of();
    }
    String line =
        switch (operation) {
          case CHAT_HISTORY_BEFORE ->
              Ircv3ChatHistoryCommandBuilder.buildBefore(
                  request.target(), beforeSelector(request), request.limit());
          case CHAT_HISTORY_LATEST ->
              Ircv3ChatHistoryCommandBuilder.buildLatest(
                  request.target(), latestSelector(request), request.limit());
          case CHAT_HISTORY_BETWEEN ->
              Ircv3ChatHistoryCommandBuilder.buildBetween(
                  request.target(),
                  request.primaryValue(),
                  request.secondaryValue(),
                  request.limit());
          case CHAT_HISTORY_AROUND ->
              Ircv3ChatHistoryCommandBuilder.buildAround(
                  request.target(), request.primaryValue(), request.limit());
          default -> "";
        };
    return line.isEmpty() ? List.of() : List.of(line);
  }

  private static String beforeSelector(Ircv3OutboundCommandRequest request) {
    String selector = request.primaryValue();
    if (!selector.isBlank()) {
      return selector;
    }
    Instant fallback = request.timestamp() == null ? Instant.now() : request.timestamp();
    return Ircv3ChatHistoryCommandBuilder.timestampSelector(fallback);
  }

  private static String latestSelector(Ircv3OutboundCommandRequest request) {
    return request.primaryValue().isBlank() ? "*" : request.primaryValue();
  }

  @Override
  public List<Ircv3FeatureContribution> visibleFeatures() {
    return List.of(
        new Ircv3FeatureContribution(
            500, "History", List.of(), List.of(CAPABILITY, DRAFT_CAPABILITY, "znc.in/playback")));
  }
}

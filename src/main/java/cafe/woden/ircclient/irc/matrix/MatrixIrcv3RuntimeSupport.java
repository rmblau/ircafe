package cafe.woden.ircclient.irc.matrix;

import cafe.woden.ircclient.irc.ircv3.Ircv3InboundTagSignalRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageMutationRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageTagsRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3RuntimeCatalogs;
import cafe.woden.ircclient.irc.ircv3.Ircv3TypingRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Runtime-provider bridge for IRCv3 compatibility commands exposed by the Matrix adapter. */
@Component
@InfrastructureLayer
public final class MatrixIrcv3RuntimeSupport {

  private final Ircv3MessageMutationRuntimeSupport messageMutationRuntimeSupport;
  private final Ircv3MessageTagsRuntimeCatalog messageTagsCatalog;
  private final Ircv3TypingRuntimeSupport typingRuntimeSupport;

  @Autowired
  public MatrixIrcv3RuntimeSupport(Ircv3RuntimeCatalogs catalogs) {
    Ircv3RuntimeCatalogs runtimeCatalogs = Objects.requireNonNull(catalogs, "catalogs");
    Ircv3InboundTagSignalRuntimeCatalog inboundTagCatalog = runtimeCatalogs.inboundTags();
    this.messageMutationRuntimeSupport =
        new Ircv3MessageMutationRuntimeSupport(
            runtimeCatalogs.messageMutations(),
            inboundTagCatalog,
            runtimeCatalogs.inboundCommands());
    this.messageTagsCatalog = runtimeCatalogs.messageTags();
    this.typingRuntimeSupport =
        new Ircv3TypingRuntimeSupport(
            runtimeCatalogs.outboundCommands(),
            inboundTagCatalog,
            runtimeCatalogs.inboundCommands());
  }

  public Map<String, String> messageTags(String rawLine) {
    return messageTagsCatalog.parseRawLine(rawLine);
  }

  public String replyTarget(
      String command,
      String rawTarget,
      List<String> parameters,
      Map<String, String> tags,
      String rawLine) {
    return messageMutationRuntimeSupport
        .replyFromTags(request(command, rawTarget, parameters, tags, rawLine))
        .map(Ircv3MessageMutationRuntimeSupport.ReplyObservation::messageId)
        .orElse("");
  }

  public String messageEditTarget(
      String command,
      String rawTarget,
      List<String> parameters,
      Map<String, String> tags,
      String rawLine) {
    return messageMutationRuntimeSupport
        .messageEditFromTags(request(command, rawTarget, parameters, tags, rawLine))
        .map(Ircv3MessageMutationRuntimeSupport.MessageEditObservation::messageId)
        .orElse("");
  }

  public String typingState(
      String command,
      String rawTarget,
      List<String> parameters,
      Map<String, String> tags,
      String rawLine) {
    return typingRuntimeSupport
        .fromTags(request(command, rawTarget, parameters, tags, rawLine))
        .map(Ircv3TypingRuntimeSupport.TagObservation::state)
        .orElse("");
  }

  public ReactionPlan reaction(
      String command,
      String rawTarget,
      List<String> parameters,
      Map<String, String> tags,
      String rawLine) {
    Ircv3MessageMutationRuntimeSupport.ReactionSelection selection =
        messageMutationRuntimeSupport.reactionSelectionFromTags(
            request(command, rawTarget, parameters, tags, rawLine));
    if (selection.type() == Ircv3MessageMutationRuntimeSupport.ReactionSelectionType.AMBIGUOUS) {
      return ReactionPlan.ambiguous();
    }
    if (selection.type() != Ircv3MessageMutationRuntimeSupport.ReactionSelectionType.OBSERVED) {
      return ReactionPlan.none();
    }
    Ircv3MessageMutationRuntimeSupport.ReactionObservation observed = selection.observation();
    return new ReactionPlan(
        observed.operation() == Ircv3MessageMutationRuntimeSupport.ReactionOperation.REACT
            ? ReactionType.REACT
            : ReactionType.UNREACT,
        observed.messageId(),
        observed.reaction());
  }

  private static Ircv3InboundTagRequest request(
      String command,
      String rawTarget,
      List<String> parameters,
      Map<String, String> tags,
      String rawLine) {
    return new Ircv3InboundTagRequest(command, "", rawTarget, parameters, tags, rawLine);
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }

  public enum ReactionType {
    NONE,
    REACT,
    UNREACT,
    AMBIGUOUS
  }

  public record ReactionPlan(ReactionType type, String messageId, String reaction) {

    public ReactionPlan {
      Objects.requireNonNull(type, "type");
      messageId = normalize(messageId);
      reaction = normalize(reaction);
      if ((type == ReactionType.REACT || type == ReactionType.UNREACT) && reaction.isEmpty()) {
        throw new IllegalArgumentException("reaction must not be blank");
      }
      if ((type == ReactionType.NONE || type == ReactionType.AMBIGUOUS)
          && (!messageId.isEmpty() || !reaction.isEmpty())) {
        throw new IllegalArgumentException("empty reaction plans must not carry values");
      }
    }

    public static ReactionPlan none() {
      return new ReactionPlan(ReactionType.NONE, "", "");
    }

    public static ReactionPlan ambiguous() {
      return new ReactionPlan(ReactionType.AMBIGUOUS, "", "");
    }
  }
}

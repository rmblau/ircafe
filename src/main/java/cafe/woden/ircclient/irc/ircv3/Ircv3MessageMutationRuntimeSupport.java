package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Validates runtime-provider message mutations before events or transport writes. */
@Component
@InfrastructureLayer
public final class Ircv3MessageMutationRuntimeSupport {

  private static final int MAX_RAW_LINE_LENGTH = 4096;
  private static final int MAX_TARGET_LENGTH = 512;
  private static final int MAX_MESSAGE_ID_LENGTH = 2048;
  private static final int MAX_REACTION_LENGTH = 512;
  private static final int MAX_PAYLOAD_LENGTH = 4096;

  private final Ircv3MessageMutationRuntimeCatalog outboundCatalog;
  private final Ircv3InboundTagSignalRuntimeCatalog inboundTagCatalog;
  private final Ircv3InboundCommandSignalRuntimeCatalog inboundCommandCatalog;

  @Autowired
  public Ircv3MessageMutationRuntimeSupport(
      Ircv3MessageMutationRuntimeCatalog outboundCatalog,
      Ircv3InboundTagSignalRuntimeCatalog inboundTagCatalog,
      Ircv3InboundCommandSignalRuntimeCatalog inboundCommandCatalog) {
    this.outboundCatalog = Objects.requireNonNull(outboundCatalog, "outboundCatalog");
    this.inboundTagCatalog = Objects.requireNonNull(inboundTagCatalog, "inboundTagCatalog");
    this.inboundCommandCatalog =
        Objects.requireNonNull(inboundCommandCatalog, "inboundCommandCatalog");
  }

  /** Explicit outbound-only composition for backend adapters that do not parse inbound signals. */
  public static Ircv3MessageMutationRuntimeSupport outboundOnly(
      Ircv3MessageMutationRuntimeCatalog outboundCatalog) {
    return new Ircv3MessageMutationRuntimeSupport(
        outboundCatalog,
        Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of()),
        Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of()));
  }

  /** Explicit inbound-only composition for transport adapters that do not render mutations. */
  public static Ircv3MessageMutationRuntimeSupport inboundOnly(
      Ircv3InboundTagSignalRuntimeCatalog inboundTagCatalog,
      Ircv3InboundCommandSignalRuntimeCatalog inboundCommandCatalog) {
    return new Ircv3MessageMutationRuntimeSupport(
        Ircv3MessageMutationRuntimeCatalog.fromProviders(List.of()),
        inboundTagCatalog,
        inboundCommandCatalog);
  }

  public boolean outboundAvailable(Ircv3MessageMutationOperation operation) {
    return outboundCatalog.supports(operation);
  }

  public Optional<OutboundPlan> renderReply(String target, String messageId, String message) {
    return render(Ircv3MessageMutationOperation.REPLY, target, messageId, message);
  }

  public Optional<OutboundPlan> renderReaction(
      String target, String messageId, String reaction, boolean remove) {
    return render(
        remove ? Ircv3MessageMutationOperation.UNREACT : Ircv3MessageMutationOperation.REACT,
        target,
        messageId,
        reaction);
  }

  public Optional<OutboundPlan> renderEdit(String target, String messageId, String editedText) {
    return render(Ircv3MessageMutationOperation.EDIT, target, messageId, editedText);
  }

  public Optional<OutboundPlan> renderRedaction(String target, String messageId, String reason) {
    return render(Ircv3MessageMutationOperation.REDACT, target, messageId, reason);
  }

  public Optional<ReplyObservation> replyFromTags(Ircv3InboundTagRequest request) {
    if (request == null) return Optional.empty();
    String accepted = "";
    for (Ircv3InboundTagSignal signal :
        inboundTagCatalog.parse(Ircv3InboundTagOperation.REPLY, request)) {
      if (signal.type() != Ircv3InboundTagSignalType.REPLY) continue;
      String messageId = normalizeTagValue(signal.primaryValue(), MAX_MESSAGE_ID_LENGTH);
      if (messageId.isEmpty() || !accepted.isEmpty()) return Optional.empty();
      accepted = messageId;
    }
    return accepted.isEmpty() ? Optional.empty() : Optional.of(new ReplyObservation(accepted));
  }

  public ReactionSelection reactionSelectionFromTags(Ircv3InboundTagRequest request) {
    if (request == null) return ReactionSelection.none();
    ReactionObservation accepted = null;
    for (Ircv3InboundTagSignal signal :
        inboundTagCatalog.parse(Ircv3InboundTagOperation.REACTIONS, request)) {
      ReactionOperation operation =
          switch (signal.type()) {
            case REACT -> ReactionOperation.REACT;
            case UNREACT -> ReactionOperation.UNREACT;
            default -> null;
          };
      if (operation == null) continue;
      String reaction = normalizeTagValue(signal.primaryValue(), MAX_REACTION_LENGTH);
      String messageId = normalizeOptionalTagValue(signal.secondaryValue(), MAX_MESSAGE_ID_LENGTH);
      if (reaction.isEmpty() || messageId == null || accepted != null) {
        return ReactionSelection.ambiguous();
      }
      accepted = new ReactionObservation(operation, reaction, messageId);
    }
    return accepted == null ? ReactionSelection.none() : ReactionSelection.observed(accepted);
  }

  public Optional<ReactionObservation> reactionFromTags(Ircv3InboundTagRequest request) {
    ReactionSelection selection = reactionSelectionFromTags(request);
    return selection.type() == ReactionSelectionType.OBSERVED
        ? Optional.of(selection.observation())
        : Optional.empty();
  }

  public Optional<MessageEditObservation> messageEditFromTags(Ircv3InboundTagRequest request) {
    if (request == null) return Optional.empty();
    String accepted = "";
    for (Ircv3InboundTagSignal signal :
        inboundTagCatalog.parse(Ircv3InboundTagOperation.MESSAGE_EDIT, request)) {
      if (signal.type() != Ircv3InboundTagSignalType.MESSAGE_EDIT) continue;
      String messageId = normalizeTagValue(signal.primaryValue(), MAX_MESSAGE_ID_LENGTH);
      if (messageId.isEmpty() || !accepted.isEmpty()) return Optional.empty();
      accepted = messageId;
    }
    return accepted.isEmpty()
        ? Optional.empty()
        : Optional.of(new MessageEditObservation(accepted));
  }

  public Optional<RedactionObservation> redactionFromTags(Ircv3InboundTagRequest request) {
    if (request == null) return Optional.empty();
    String accepted = "";
    for (Ircv3InboundTagSignal signal :
        inboundTagCatalog.parse(Ircv3InboundTagOperation.MESSAGE_REDACTION, request)) {
      if (signal.type() != Ircv3InboundTagSignalType.MESSAGE_REDACTION) continue;
      String messageId = normalizeTagValue(signal.primaryValue(), MAX_MESSAGE_ID_LENGTH);
      if (messageId.isEmpty() || !accepted.isEmpty()) return Optional.empty();
      accepted = messageId;
    }
    return accepted.isEmpty()
        ? Optional.empty()
        : Optional.of(new RedactionObservation(accepted));
  }

  public Optional<CommandRedactionObservation> redactionFromCommand(
      Ircv3InboundCommandRequest request) {
    if (request == null) return Optional.empty();
    CommandRedactionObservation accepted = null;
    for (Ircv3InboundCommandSignal signal :
        inboundCommandCatalog.parse(Ircv3InboundCommandOperation.MESSAGE_REDACTION, request)) {
      if (!(signal instanceof Ircv3InboundCommandSignal.MessageRedactionObserved observed)) {
        continue;
      }
      String target = normalizeTarget(observed.target());
      String messageId = normalizeToken(observed.messageId(), MAX_MESSAGE_ID_LENGTH);
      if (target.isEmpty() || messageId.isEmpty() || accepted != null) return Optional.empty();
      accepted = new CommandRedactionObservation(target, messageId);
    }
    return Optional.ofNullable(accepted);
  }

  public List<Ircv3InboundTagSignal> conversationSignals(Ircv3InboundTagRequest request) {
    if (request == null) return List.of();
    ArrayList<Ircv3InboundTagSignal> signals = new ArrayList<>(3);
    replyFromTags(request)
        .ifPresent(
            observed ->
                signals.add(
                    Ircv3InboundTagSignal.of(
                        Ircv3InboundTagSignalType.REPLY, observed.messageId())));
    reactionFromTags(request)
        .ifPresent(
            observed ->
                signals.add(
                    new Ircv3InboundTagSignal(
                        observed.operation() == ReactionOperation.REACT
                            ? Ircv3InboundTagSignalType.REACT
                            : Ircv3InboundTagSignalType.UNREACT,
                        observed.reaction(),
                        observed.messageId())));
    redactionFromTags(request)
        .ifPresent(
            observed ->
                signals.add(
                    Ircv3InboundTagSignal.of(
                        Ircv3InboundTagSignalType.MESSAGE_REDACTION,
                        observed.messageId())));
    return List.copyOf(signals);
  }

  public boolean hasNonReplyMutationTag(Ircv3InboundTagRequest request) {
    return messageEditFromTags(request).isPresent()
        || reactionFromTags(request).isPresent()
        || redactionFromTags(request).isPresent();
  }

  private Optional<OutboundPlan> render(
      Ircv3MessageMutationOperation operation,
      String target,
      String messageId,
      String payload) {
    Objects.requireNonNull(operation, "operation");
    String requestedTarget = requireTarget(target, IllegalArgumentException::new);
    String requestedMessageId =
        operation == Ircv3MessageMutationOperation.REDACT
            ? requireToken(messageId, "redaction message id", IllegalArgumentException::new)
            : requireTagValue(messageId, "message id", MAX_MESSAGE_ID_LENGTH, IllegalArgumentException::new);
    String requestedPayload = normalizePayload(payload);
    if (operation != Ircv3MessageMutationOperation.REDACT && requestedPayload.isEmpty()) {
      return Optional.empty();
    }
    String rawLine =
        outboundCatalog.build(
            operation,
            new Ircv3MessageMutationRequest(
                requestedTarget, requestedMessageId, requestedPayload));
    if (rawLine.isBlank()) return Optional.empty();
    return Optional.of(
        parseOutbound(
            operation,
            requestedTarget,
            requestedMessageId,
            requestedPayload,
            rawLine));
  }

  private static OutboundPlan parseOutbound(
      Ircv3MessageMutationOperation operation,
      String requestedTarget,
      String requestedMessageId,
      String requestedPayload,
      String rawLine) {
    String line = normalizeRawLine(rawLine);
    return switch (operation) {
      case REPLY ->
          parseTaggedPrivmsg(
              operation,
              requestedTarget,
              requestedMessageId,
              requestedPayload,
              line,
              List.of("reply", "draft/reply"));
      case EDIT ->
          parseTaggedPrivmsg(
              operation,
              requestedTarget,
              requestedMessageId,
              requestedPayload,
              line,
              List.of("draft/edit"));
      case REACT, UNREACT ->
          parseReaction(operation, requestedTarget, requestedMessageId, requestedPayload, line);
      case REDACT ->
          parseRedaction(requestedTarget, requestedMessageId, requestedPayload, line);
    };
  }

  private static OutboundPlan parseTaggedPrivmsg(
      Ircv3MessageMutationOperation operation,
      String requestedTarget,
      String requestedMessageId,
      String requestedPayload,
      String line,
      List<String> acceptedTagKeys) {
    int tagsEnd = line.indexOf(' ');
    if (!line.startsWith("@") || tagsEnd < 2) {
      throw invalid(operation, "missing mutation tag");
    }
    Map<String, String> tags = parseTags(line.substring(1, tagsEnd), operation);
    if (tags.size() != 1) throw invalid(operation, "unexpected mutation tags");
    String renderedMessageId = firstTag(tags, acceptedTagKeys);
    if (!requestedMessageId.equals(renderedMessageId)) {
      throw invalid(operation, "changed the requested message id");
    }

    String command = line.substring(tagsEnd + 1);
    int firstSpace = command.indexOf(' ');
    int payloadMarker = command.indexOf(" :", firstSpace + 1);
    if (firstSpace <= 0
        || payloadMarker < 0
        || !"PRIVMSG".equalsIgnoreCase(command.substring(0, firstSpace))) {
      throw invalid(operation, "returned an invalid PRIVMSG command");
    }
    String renderedTarget = requireTarget(command.substring(firstSpace + 1, payloadMarker),
        message -> invalid(operation, message));
    if (!requestedTarget.equals(renderedTarget)) {
      throw invalid(operation, "changed the requested target");
    }
    String renderedPayload = command.substring(payloadMarker + 2);
    if (!requestedPayload.equals(renderedPayload)) {
      throw invalid(operation, "changed the requested payload");
    }
    return new OutboundPlan(operation, line, renderedTarget, renderedMessageId, renderedPayload);
  }

  private static OutboundPlan parseReaction(
      Ircv3MessageMutationOperation operation,
      String requestedTarget,
      String requestedMessageId,
      String requestedReaction,
      String line) {
    int tagsEnd = line.indexOf(' ');
    if (!line.startsWith("@") || tagsEnd < 2) {
      throw invalid(operation, "missing reaction tags");
    }
    Map<String, String> tags = parseTags(line.substring(1, tagsEnd), operation);
    if (tags.size() != 2) throw invalid(operation, "unexpected reaction tags");
    String reactionKey =
        operation == Ircv3MessageMutationOperation.REACT ? "draft/react" : "draft/unreact";
    String renderedReaction = tags.getOrDefault(reactionKey, "");
    String renderedMessageId = firstTag(tags, List.of("reply", "draft/reply"));
    if (!requestedReaction.equals(renderedReaction)) {
      throw invalid(operation, "changed the requested reaction");
    }
    if (!requestedMessageId.equals(renderedMessageId)) {
      throw invalid(operation, "changed the requested message id");
    }

    String command = line.substring(tagsEnd + 1).trim();
    int firstSpace = command.indexOf(' ');
    if (firstSpace <= 0
        || command.indexOf(' ', firstSpace + 1) >= 0
        || !"TAGMSG".equalsIgnoreCase(command.substring(0, firstSpace))) {
      throw invalid(operation, "returned an invalid TAGMSG command");
    }
    String renderedTarget = requireTarget(command.substring(firstSpace + 1),
        message -> invalid(operation, message));
    if (!requestedTarget.equals(renderedTarget)) {
      throw invalid(operation, "changed the requested target");
    }
    return new OutboundPlan(
        operation, line, renderedTarget, renderedMessageId, renderedReaction);
  }

  private static OutboundPlan parseRedaction(
      String requestedTarget, String requestedMessageId, String requestedReason, String line) {
    int firstSpace = line.indexOf(' ');
    int secondSpace = firstSpace < 0 ? -1 : line.indexOf(' ', firstSpace + 1);
    if (firstSpace <= 0
        || secondSpace <= firstSpace + 1
        || !"REDACT".equalsIgnoreCase(line.substring(0, firstSpace))) {
      throw invalid(Ircv3MessageMutationOperation.REDACT, "returned an invalid REDACT command");
    }
    String renderedTarget = requireTarget(line.substring(firstSpace + 1, secondSpace),
        message -> invalid(Ircv3MessageMutationOperation.REDACT, message));
    String remainder = line.substring(secondSpace + 1);
    int reasonMarker = remainder.indexOf(" :");
    String renderedMessageId = reasonMarker < 0 ? remainder : remainder.substring(0, reasonMarker);
    String renderedReason = reasonMarker < 0 ? "" : remainder.substring(reasonMarker + 2);
    renderedMessageId = normalizeToken(renderedMessageId, MAX_MESSAGE_ID_LENGTH);
    if (renderedMessageId.isEmpty()) {
      throw invalid(Ircv3MessageMutationOperation.REDACT, "returned an unsafe message id");
    }
    if (!requestedTarget.equals(renderedTarget)) {
      throw invalid(Ircv3MessageMutationOperation.REDACT, "changed the requested target");
    }
    if (!requestedMessageId.equals(renderedMessageId)) {
      throw invalid(Ircv3MessageMutationOperation.REDACT, "changed the requested message id");
    }
    if (!requestedReason.equals(renderedReason)) {
      throw invalid(Ircv3MessageMutationOperation.REDACT, "changed the requested reason");
    }
    return new OutboundPlan(
        Ircv3MessageMutationOperation.REDACT,
        line,
        renderedTarget,
        renderedMessageId,
        renderedReason);
  }

  private static Map<String, String> parseTags(
      String rawTags, Ircv3MessageMutationOperation operation) {
    LinkedHashMap<String, String> tags = new LinkedHashMap<>();
    for (String entry : Objects.toString(rawTags, "").split(";", -1)) {
      int equals = entry.indexOf('=');
      if (equals <= 0 || equals == entry.length() - 1) {
        throw invalid(operation, "returned a malformed mutation tag");
      }
      String key = entry.substring(0, equals).trim();
      if (key.startsWith("+")) key = key.substring(1);
      key = key.toLowerCase(Locale.ROOT);
      if (key.isEmpty() || tags.containsKey(key)) {
        throw invalid(operation, "returned duplicate or blank mutation tags");
      }
      tags.put(key, decodeTagValue(entry.substring(equals + 1), operation));
    }
    return Map.copyOf(tags);
  }

  private static String firstTag(Map<String, String> tags, List<String> keys) {
    String accepted = "";
    for (String key : keys) {
      String value = tags.getOrDefault(key, "");
      if (value.isEmpty()) continue;
      if (!accepted.isEmpty()) return "";
      accepted = value;
    }
    return accepted;
  }

  private static String decodeTagValue(
      String raw, Ircv3MessageMutationOperation operation) {
    StringBuilder decoded = new StringBuilder(raw.length());
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      if (c != '\\') {
        decoded.append(c);
        continue;
      }
      if (++i >= raw.length()) throw invalid(operation, "returned a dangling tag escape");
      switch (raw.charAt(i)) {
        case ':' -> decoded.append(';');
        case 's' -> decoded.append(' ');
        case '\\' -> decoded.append('\\');
        case 'r' -> decoded.append('\r');
        case 'n' -> decoded.append('\n');
        default -> throw invalid(operation, "returned an unknown tag escape");
      }
    }
    return decoded.toString();
  }

  private static String normalizeRawLine(String rawLine) {
    String line = Objects.toString(rawLine, "").trim();
    if (line.isEmpty() || line.length() > MAX_RAW_LINE_LENGTH || containsControl(line)) {
      throw new IllegalStateException("Message-mutation runtime provider returned an unsafe raw line");
    }
    return line;
  }

  private static String normalizeTarget(String raw) {
    String target = Objects.toString(raw, "").trim();
    if (target.isEmpty()
        || target.length() > MAX_TARGET_LENGTH
        || containsControl(target)
        || target.chars().anyMatch(Character::isWhitespace)) {
      return "";
    }
    return target;
  }

  private static String normalizeToken(String raw, int maxLength) {
    String token = Objects.toString(raw, "").trim();
    if (token.isEmpty()
        || token.length() > maxLength
        || containsControl(token)
        || token.chars().anyMatch(Character::isWhitespace)) {
      return "";
    }
    return token;
  }

  private static String normalizeTagValue(String raw, int maxLength) {
    String value = Objects.toString(raw, "").trim();
    if (value.isEmpty() || value.length() > maxLength || containsControl(value)) return "";
    return value;
  }

  private static String normalizeOptionalTagValue(String raw, int maxLength) {
    String value = Objects.toString(raw, "").trim();
    if (value.isEmpty()) return "";
    return value.length() > maxLength || containsControl(value) ? null : value;
  }

  private static String normalizePayload(String raw) {
    String payload = Objects.toString(raw, "").trim();
    if (payload.length() > MAX_PAYLOAD_LENGTH || containsControl(payload)) {
      throw new IllegalArgumentException("message-mutation payload contains controls or is too long");
    }
    return payload;
  }

  private static String requireTarget(
      String raw, Function<String, ? extends RuntimeException> errorFactory) {
    String target = normalizeTarget(raw);
    if (target.isEmpty()) throw errorFactory.apply("message-mutation target is blank or unsafe");
    return target;
  }

  private static String requireToken(
      String raw,
      String label,
      Function<String, ? extends RuntimeException> errorFactory) {
    String token = normalizeToken(raw, MAX_MESSAGE_ID_LENGTH);
    if (token.isEmpty()) throw errorFactory.apply(label + " is blank or unsafe");
    return token;
  }

  private static String requireTagValue(
      String raw,
      String label,
      int maxLength,
      Function<String, ? extends RuntimeException> errorFactory) {
    String value = normalizeTagValue(raw, maxLength);
    if (value.isEmpty()) throw errorFactory.apply(label + " is blank or unsafe");
    return value;
  }

  private static IllegalStateException invalid(
      Ircv3MessageMutationOperation operation, String detail) {
    return new IllegalStateException(
        "Message-mutation runtime provider for " + operation + " " + detail);
  }

  private static boolean containsControl(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (Character.isISOControl(value.charAt(i))) return true;
    }
    return false;
  }

  public enum ReactionOperation {
    REACT,
    UNREACT
  }

  public enum ReactionSelectionType {
    NONE,
    OBSERVED,
    AMBIGUOUS
  }

  public record ReactionSelection(
      ReactionSelectionType type, ReactionObservation observation) {
    public ReactionSelection {
      type = Objects.requireNonNull(type, "type");
      if (type == ReactionSelectionType.OBSERVED && observation == null) {
        throw new IllegalArgumentException("observed reaction selection requires an observation");
      }
      if (type != ReactionSelectionType.OBSERVED && observation != null) {
        throw new IllegalArgumentException("empty reaction selection must not carry an observation");
      }
    }

    public static ReactionSelection none() {
      return new ReactionSelection(ReactionSelectionType.NONE, null);
    }

    public static ReactionSelection observed(ReactionObservation observation) {
      return new ReactionSelection(
          ReactionSelectionType.OBSERVED,
          Objects.requireNonNull(observation, "observation"));
    }

    public static ReactionSelection ambiguous() {
      return new ReactionSelection(ReactionSelectionType.AMBIGUOUS, null);
    }
  }

  public record OutboundPlan(
      Ircv3MessageMutationOperation operation,
      String rawLine,
      String target,
      String messageId,
      String payload) {
    public OutboundPlan {
      operation = Objects.requireNonNull(operation, "operation");
      rawLine = Objects.requireNonNull(rawLine, "rawLine");
      target = Objects.requireNonNull(target, "target");
      messageId = Objects.requireNonNull(messageId, "messageId");
      payload = Objects.requireNonNull(payload, "payload");
    }
  }

  public record ReplyObservation(String messageId) {
    public ReplyObservation {
      messageId = Objects.requireNonNull(messageId, "messageId");
    }
  }

  public record ReactionObservation(
      ReactionOperation operation, String reaction, String messageId) {
    public ReactionObservation {
      operation = Objects.requireNonNull(operation, "operation");
      reaction = Objects.requireNonNull(reaction, "reaction");
      messageId = Objects.requireNonNull(messageId, "messageId");
    }
  }

  public record MessageEditObservation(String messageId) {
    public MessageEditObservation {
      messageId = Objects.requireNonNull(messageId, "messageId");
    }
  }

  public record RedactionObservation(String messageId) {
    public RedactionObservation {
      messageId = Objects.requireNonNull(messageId, "messageId");
    }
  }

  public record CommandRedactionObservation(String target, String messageId) {
    public CommandRedactionObservation {
      target = Objects.requireNonNull(target, "target");
      messageId = Objects.requireNonNull(messageId, "messageId");
    }
  }
}

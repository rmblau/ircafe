package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Validates runtime-provider typing commands and observations before application use. */
@Component
@InfrastructureLayer
public final class Ircv3TypingRuntimeSupport {

  private static final int MAX_RAW_LINE_LENGTH = 4096;
  private static final int MAX_TARGET_LENGTH = 512;
  private static final int MAX_STATE_LENGTH = 32;
  private static final int MAX_CLIENT_TAG_POLICY_LENGTH = 2048;

  private final Ircv3OutboundCommandRuntimeCatalog outboundCatalog;
  private final Ircv3InboundTagSignalRuntimeCatalog inboundTagCatalog;
  private final Ircv3InboundCommandSignalRuntimeCatalog inboundCommandCatalog;

  @Autowired
  public Ircv3TypingRuntimeSupport(
      Ircv3OutboundCommandRuntimeCatalog outboundCatalog,
      Ircv3InboundTagSignalRuntimeCatalog inboundTagCatalog,
      Ircv3InboundCommandSignalRuntimeCatalog inboundCommandCatalog) {
    this.outboundCatalog = Objects.requireNonNull(outboundCatalog, "outboundCatalog");
    this.inboundTagCatalog = Objects.requireNonNull(inboundTagCatalog, "inboundTagCatalog");
    this.inboundCommandCatalog =
        Objects.requireNonNull(inboundCommandCatalog, "inboundCommandCatalog");
  }

  /** Explicit outbound-only composition for command adapters that do not parse inbound signals. */
  public static Ircv3TypingRuntimeSupport outboundOnly(
      Ircv3OutboundCommandRuntimeCatalog outboundCatalog) {
    return new Ircv3TypingRuntimeSupport(
        outboundCatalog,
        Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of()),
        Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of()));
  }

  public boolean outboundAvailable() {
    return outboundCatalog.supports(Ircv3OutboundCommandOperation.TYPING);
  }

  public Optional<OutboundPlan> render(String target, String state) {
    String requestedTarget = requireTarget(target, IllegalArgumentException::new);
    String requestedState = normalizeRequestedState(state);
    if (requestedState.isEmpty()) {
      return Optional.empty();
    }
    String rawLine =
        outboundCatalog.buildSingle(
            Ircv3OutboundCommandOperation.TYPING,
            Ircv3OutboundCommandRequest.typing(requestedTarget, requestedState));
    if (rawLine.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(parseOutbound(requestedTarget, rawLine));
  }

  public Optional<TagObservation> fromTags(Ircv3InboundTagRequest request) {
    if (request == null) {
      return Optional.empty();
    }
    String accepted = "";
    for (Ircv3InboundTagSignal signal :
        inboundTagCatalog.parse(Ircv3InboundTagOperation.TYPING, request)) {
      if (signal.type() != Ircv3InboundTagSignalType.TYPING) {
        continue;
      }
      String state = canonicalState(signal.primaryValue());
      if (state.isEmpty() || !accepted.isEmpty()) {
        return Optional.empty();
      }
      accepted = state;
    }
    return accepted.isEmpty() ? Optional.empty() : Optional.of(new TagObservation(accepted));
  }

  public Optional<ClientTagPolicy> clientTagPolicy(String rawLine) {
    String raw = Objects.toString(rawLine, "");
    if (raw.isBlank() || raw.length() > MAX_RAW_LINE_LENGTH || containsControl(raw)) {
      return Optional.empty();
    }
    ClientTagPolicy accepted = null;
    for (Ircv3InboundCommandSignal signal :
        inboundCommandCatalog.parse(
            Ircv3InboundCommandOperation.ISUPPORT_CLIENT_TAG_POLICY,
            new Ircv3InboundCommandRequest("", "005", raw, List.of(), Map.of()))) {
      if (!(signal instanceof Ircv3InboundCommandSignal.ClientTagPolicyObserved policy)
          || !"typing".equalsIgnoreCase(policy.tagName())) {
        continue;
      }
      String rawDenyValue = normalizeClientTagPolicy(policy.rawDenyValue());
      if (rawDenyValue == null || accepted != null) {
        return Optional.empty();
      }
      accepted = new ClientTagPolicy(policy.allowed(), rawDenyValue);
    }
    return Optional.ofNullable(accepted);
  }

  private static OutboundPlan parseOutbound(String requestedTarget, String rawLine) {
    String line = Objects.toString(rawLine, "").trim();
    if (line.isEmpty() || line.length() > MAX_RAW_LINE_LENGTH || containsControl(line)) {
      throw new IllegalStateException("Typing runtime provider returned an unsafe raw line");
    }
    String[] tokens = line.split("\\s+");
    if (tokens.length != 3 || !"TAGMSG".equalsIgnoreCase(tokens[1])) {
      throw new IllegalStateException("Typing runtime provider returned an invalid command");
    }
    String state = parseTypingTag(tokens[0]);
    String renderedTarget = requireTarget(tokens[2], IllegalStateException::new);
    if (!requestedTarget.equals(renderedTarget)) {
      throw new IllegalStateException("Typing runtime provider changed the requested target");
    }
    return new OutboundPlan(line, renderedTarget, state);
  }

  private static String parseTypingTag(String raw) {
    String tag = Objects.toString(raw, "").trim();
    String prefix = "@+typing=";
    if (!tag.regionMatches(true, 0, prefix, 0, prefix.length())
        || tag.indexOf(';') >= 0) {
      throw new IllegalStateException("Typing runtime provider returned an invalid typing tag");
    }
    String state = canonicalState(tag.substring(prefix.length()));
    if (state.isEmpty()) {
      throw new IllegalStateException("Typing runtime provider returned an invalid typing state");
    }
    return state;
  }

  private static String normalizeRequestedState(String raw) {
    String state = Objects.toString(raw, "").trim();
    if (state.isEmpty()) {
      return "";
    }
    if (state.length() > MAX_STATE_LENGTH
        || containsControl(state)
        || state.chars().anyMatch(Character::isWhitespace)) {
      throw new IllegalArgumentException("typing state contains whitespace or controls");
    }
    return state;
  }

  private static String canonicalState(String raw) {
    String state = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    if (state.length() > MAX_STATE_LENGTH
        || containsControl(state)
        || state.chars().anyMatch(Character::isWhitespace)) {
      return "";
    }
    return switch (state) {
      case "active", "paused", "done" -> state;
      default -> "";
    };
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

  private static String requireTarget(
      String raw, java.util.function.Function<String, ? extends RuntimeException> errorFactory) {
    String target = normalizeTarget(raw);
    if (target.isEmpty()) {
      throw errorFactory.apply("typing target is blank or unsafe");
    }
    return target;
  }

  private static String normalizeClientTagPolicy(String raw) {
    String policy = Objects.toString(raw, "").trim();
    if (policy.length() > MAX_CLIENT_TAG_POLICY_LENGTH || containsControl(policy)) {
      return null;
    }
    return policy;
  }

  private static boolean containsControl(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (Character.isISOControl(value.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  public record OutboundPlan(String rawLine, String target, String state) {
    public OutboundPlan {
      rawLine = Objects.requireNonNull(rawLine, "rawLine");
      target = Objects.requireNonNull(target, "target");
      state = Objects.requireNonNull(state, "state");
    }
  }

  public record TagObservation(String state) {
    public TagObservation {
      state = Objects.requireNonNull(state, "state");
    }
  }

  public record ClientTagPolicy(boolean allowed, String rawDenyValue) {
    public ClientTagPolicy {
      rawDenyValue = Objects.requireNonNull(rawDenyValue, "rawDenyValue");
    }
  }
}

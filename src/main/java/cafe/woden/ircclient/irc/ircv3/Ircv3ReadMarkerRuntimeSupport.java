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
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Validates runtime-provider read-marker commands and observations before transport use. */
@Component
@InfrastructureLayer
public final class Ircv3ReadMarkerRuntimeSupport {

  private static final int MAX_RAW_LINE_LENGTH = 4096;
  private static final int MAX_TARGET_LENGTH = 512;
  private static final int MAX_MARKER_LENGTH = 2048;

  private final Ircv3OutboundCommandRuntimeCatalog outboundCatalog;
  private final Ircv3InboundTagSignalRuntimeCatalog inboundTagCatalog;
  private final Ircv3InboundCommandSignalRuntimeCatalog inboundCommandCatalog;

  @Autowired
  public Ircv3ReadMarkerRuntimeSupport(
      Ircv3OutboundCommandRuntimeCatalog outboundCatalog,
      Ircv3InboundTagSignalRuntimeCatalog inboundTagCatalog,
      Ircv3InboundCommandSignalRuntimeCatalog inboundCommandCatalog) {
    this.outboundCatalog = Objects.requireNonNull(outboundCatalog, "outboundCatalog");
    this.inboundTagCatalog = Objects.requireNonNull(inboundTagCatalog, "inboundTagCatalog");
    this.inboundCommandCatalog =
        Objects.requireNonNull(inboundCommandCatalog, "inboundCommandCatalog");
  }

  /** Explicit outbound-only composition for command adapters that do not parse inbound signals. */
  public static Ircv3ReadMarkerRuntimeSupport outboundOnly(
      Ircv3OutboundCommandRuntimeCatalog outboundCatalog) {
    return new Ircv3ReadMarkerRuntimeSupport(
        outboundCatalog,
        Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of()),
        Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of()));
  }

  public boolean outboundAvailable() {
    return outboundCatalog.supports(Ircv3OutboundCommandOperation.READ_MARKER);
  }

  public OutboundPlan render(String target, Instant markerAt) {
    String requestedTarget = requireTarget(target, IllegalArgumentException::new);
    String rawLine =
        outboundCatalog.buildSingle(
            Ircv3OutboundCommandOperation.READ_MARKER,
            Ircv3OutboundCommandRequest.readMarker(requestedTarget, markerAt));
    if (rawLine.isEmpty()) {
      throw new IllegalStateException("Read-marker runtime provider did not render a command");
    }
    return parseOutbound(requestedTarget, rawLine);
  }

  public Optional<TagObservation> fromTags(Ircv3InboundTagRequest request) {
    if (request == null) {
      return Optional.empty();
    }
    String accepted = "";
    for (Ircv3InboundTagSignal signal :
        inboundTagCatalog.parse(Ircv3InboundTagOperation.READ_MARKER, request)) {
      if (signal.type() != Ircv3InboundTagSignalType.READ_MARKER) {
        continue;
      }
      String marker = normalizeMarker(signal.primaryValue());
      if (marker.isEmpty() || !accepted.isEmpty()) {
        return Optional.empty();
      }
      accepted = marker;
    }
    return accepted.isEmpty() ? Optional.empty() : Optional.of(new TagObservation(accepted));
  }

  public Optional<CommandObservation> fromCommand(Ircv3InboundCommandRequest request) {
    if (request == null) {
      return Optional.empty();
    }
    CommandObservation accepted = null;
    for (Ircv3InboundCommandSignal signal :
        inboundCommandCatalog.parse(Ircv3InboundCommandOperation.READ_MARKER, request)) {
      if (!(signal instanceof Ircv3InboundCommandSignal.ReadMarkerObserved observed)) {
        continue;
      }
      String target = normalizeTarget(observed.target());
      String marker = normalizeMarker(observed.marker());
      if (target.isEmpty() || marker.isEmpty() || accepted != null) {
        return Optional.empty();
      }
      accepted = new CommandObservation(target, marker);
    }
    return Optional.ofNullable(accepted);
  }

  private static OutboundPlan parseOutbound(String requestedTarget, String rawLine) {
    String line = Objects.toString(rawLine, "").trim();
    if (line.isEmpty() || line.length() > MAX_RAW_LINE_LENGTH || containsControl(line)) {
      throw new IllegalStateException("Read-marker runtime provider returned an unsafe raw line");
    }
    String[] tokens = line.split("\\s+");
    if (tokens.length != 3 || !"MARKREAD".equalsIgnoreCase(tokens[0])) {
      throw new IllegalStateException("Read-marker runtime provider returned an invalid command");
    }
    String renderedTarget = requireTarget(tokens[1], IllegalStateException::new);
    if (!requestedTarget.equals(renderedTarget)) {
      throw new IllegalStateException("Read-marker runtime provider changed the requested target");
    }
    String marker = normalizeMarker(tokens[2]);
    if (!marker.regionMatches(true, 0, "timestamp=", 0, "timestamp=".length())) {
      throw new IllegalStateException(
          "Read-marker runtime provider returned a non-timestamp marker");
    }
    String timestamp = marker.substring("timestamp=".length());
    try {
      Instant.parse(timestamp);
    } catch (RuntimeException error) {
      throw new IllegalStateException(
          "Read-marker runtime provider returned an invalid timestamp", error);
    }
    return new OutboundPlan(line, renderedTarget, "timestamp=" + timestamp);
  }

  private static String normalizeTarget(String raw) {
    String value = Objects.toString(raw, "").trim();
    if (value.isEmpty()
        || value.length() > MAX_TARGET_LENGTH
        || containsControl(value)
        || value.chars().anyMatch(Character::isWhitespace)) {
      return "";
    }
    return value;
  }

  private static String normalizeMarker(String raw) {
    String value = Objects.toString(raw, "").trim();
    if (value.isEmpty()
        || value.length() > MAX_MARKER_LENGTH
        || containsControl(value)
        || value.chars().anyMatch(Character::isWhitespace)) {
      return "";
    }
    return value;
  }

  private static String requireTarget(
      String raw, java.util.function.Function<String, ? extends RuntimeException> errorFactory) {
    String target = normalizeTarget(raw);
    if (target.isEmpty()) {
      throw errorFactory.apply("read-marker target is blank or unsafe");
    }
    return target;
  }

  private static boolean containsControl(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (Character.isISOControl(value.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  public record OutboundPlan(String rawLine, String target, String marker) {
    public OutboundPlan {
      rawLine = Objects.requireNonNull(rawLine, "rawLine");
      target = Objects.requireNonNull(target, "target");
      marker = Objects.requireNonNull(marker, "marker");
    }
  }

  public record TagObservation(String marker) {
    public TagObservation {
      marker = Objects.requireNonNull(marker, "marker");
    }
  }

  public record CommandObservation(String target, String marker) {
    public CommandObservation {
      target = Objects.requireNonNull(target, "target");
      marker = Objects.requireNonNull(marker, "marker");
    }
  }
}

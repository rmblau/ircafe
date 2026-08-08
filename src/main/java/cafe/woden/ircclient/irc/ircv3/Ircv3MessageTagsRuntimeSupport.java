package cafe.woden.ircclient.irc.ircv3;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Installed-provider-aware application adapter for IRCv3 message-tag parsing. */
@Component
@InfrastructureLayer
public final class Ircv3MessageTagsRuntimeSupport {

  private final Ircv3MessageTagsRuntimeCatalog runtimeCatalog;
  private final Ircv3MessageIdRuntimeSupport messageIdRuntimeSupport;

  @Autowired
  public Ircv3MessageTagsRuntimeSupport(
      Ircv3MessageTagsRuntimeCatalog runtimeCatalog,
      Ircv3MessageIdRuntimeSupport messageIdRuntimeSupport) {
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog");
    this.messageIdRuntimeSupport =
        Objects.requireNonNull(messageIdRuntimeSupport, "messageIdRuntimeSupport");
  }

  public Map<String, String> fromTransport(Map<String, String> transportTags, String rawLine) {
    return runtimeCatalog.parse(transportTags, rawLine);
  }

  public Map<String, String> fromRawLine(String rawLine) {
    return runtimeCatalog.parseRawLine(rawLine);
  }

  public String messageId(Map<String, String> tags) {
    return messageIdRuntimeSupport.resolve(tags);
  }

  public String messageId(Map<String, String> tags, String fallbackMessageId) {
    return messageIdRuntimeSupport.resolve(tags, fallbackMessageId);
  }

  public Map<String, String> fromEvent(Object event) {
    if (event == null) {
      return Map.of();
    }
    Map<String, String> transportTags = eventTags(event);
    String rawLine = eventRawLine(event);
    return fromTransport(transportTags, rawLine);
  }

  private static Map<String, String> eventTags(Object event) {
    Object reflected = reflectCall(event, "getTags");
    if (!(reflected instanceof Map<?, ?> raw) || raw.isEmpty()) {
      return Map.of();
    }
    LinkedHashMap<String, String> copied = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : raw.entrySet()) {
      if (entry.getKey() != null) {
        copied.put(String.valueOf(entry.getKey()), Objects.toString(entry.getValue(), ""));
      }
    }
    return copied;
  }

  private static String eventRawLine(Object event) {
    for (String method : new String[] {"getRawLine", "getLine", "getRaw"}) {
      Object reflected = reflectCall(event, method);
      if (reflected != null) {
        return String.valueOf(reflected);
      }
    }
    return "";
  }

  private static Object reflectCall(Object target, String methodName) {
    try {
      Method method = target.getClass().getMethod(methodName);
      return method.invoke(target);
    } catch (ReflectiveOperationException | RuntimeException ignored) {
      return null;
    }
  }
}

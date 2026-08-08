package cafe.woden.ircclient.ui.chat.embed;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

/** Feature-safe streaming HTTP response wrapper for image downloads. */
public record ImageFetchHttpResponse(
    int statusCode, Map<String, List<String>> headers, InputStream body) {

  public ImageFetchHttpResponse {
    if (headers == null || headers.isEmpty()) {
      headers = Map.of();
    } else {
      LinkedHashMap<String, List<String>> sanitized = new LinkedHashMap<>();
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) {
          continue;
        }
        sanitized.put(entry.getKey(), List.copyOf(entry.getValue()));
      }
      headers = Map.copyOf(sanitized);
    }
    body = body == null ? InputStream.nullInputStream() : body;
  }

  public Optional<String> firstHeader(String name) {
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }
    String target = name.toLowerCase(Locale.ROOT);
    for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
      String key = entry.getKey();
      if (key == null || !key.toLowerCase(Locale.ROOT).equals(target)) {
        continue;
      }
      List<String> values = entry.getValue();
      if (values == null || values.isEmpty()) {
        return Optional.empty();
      }
      return Optional.ofNullable(values.getFirst());
    }
    return Optional.empty();
  }

  public OptionalLong firstHeaderAsLong(String name) {
    Optional<String> value = firstHeader(name);
    if (value.isEmpty()) {
      return OptionalLong.empty();
    }
    try {
      return OptionalLong.of(Long.parseLong(value.get().trim()));
    } catch (NumberFormatException ignored) {
      return OptionalLong.empty();
    }
  }
}

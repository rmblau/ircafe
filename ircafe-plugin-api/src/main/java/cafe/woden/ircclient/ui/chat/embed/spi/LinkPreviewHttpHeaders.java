package cafe.woden.ircclient.ui.chat.embed.spi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

public record LinkPreviewHttpHeaders(Map<String, List<String>> raw) {

  public LinkPreviewHttpHeaders {
    if (raw == null || raw.isEmpty()) {
      raw = Map.of();
    } else {
      Map<String, List<String>> copy = new LinkedHashMap<>();
      for (Map.Entry<String, List<String>> entry : raw.entrySet()) {
        if (entry.getKey() == null) {
          continue;
        }
        List<String> values =
            entry.getValue() == null
                ? List.of()
                : entry.getValue().stream().filter(Objects::nonNull).toList();
        copy.put(entry.getKey(), values);
      }
      raw = Map.copyOf(copy);
    }
  }

  public Optional<String> firstValue(String name) {
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }
    String target = name.toLowerCase(Locale.ROOT);
    for (Map.Entry<String, List<String>> e : raw.entrySet()) {
      String key = e.getKey();
      if (key == null || !key.toLowerCase(Locale.ROOT).equals(target)) {
        continue;
      }
      List<String> values = e.getValue();
      if (values == null || values.isEmpty()) {
        return Optional.empty();
      }
      return Optional.ofNullable(values.getFirst());
    }
    return Optional.empty();
  }

  public OptionalLong firstValueAsLong(String name) {
    Optional<String> value = firstValue(name);
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

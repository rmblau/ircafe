package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/** Plugin-facing IRCv3 capability or tag metadata contribution. */
public record Ircv3ExtensionContribution(
    String id,
    Ircv3ExtensionKind kind,
    Ircv3SpecStatus specStatus,
    List<String> aliases,
    String requestToken,
    String preferenceKey,
    Ircv3UiMetadata uiMetadata) {

  public Ircv3ExtensionContribution {
    id = normalize(id);
    kind = Objects.requireNonNull(kind, "kind");
    specStatus = Objects.requireNonNull(specStatus, "specStatus");
    aliases = copyNormalized(aliases);
    requestToken = normalize(requestToken);
    preferenceKey = normalize(preferenceKey == null || preferenceKey.isBlank() ? id : preferenceKey);
    uiMetadata = Objects.requireNonNull(uiMetadata, "uiMetadata");
  }

  public boolean requestable() {
    return kind == Ircv3ExtensionKind.CAPABILITY && !requestToken.isEmpty();
  }

  public List<String> allNames() {
    LinkedHashSet<String> names = new LinkedHashSet<>();
    if (!id.isEmpty()) {
      names.add(id);
    }
    if (!preferenceKey.isEmpty()) {
      names.add(preferenceKey);
    }
    if (!requestToken.isEmpty()) {
      names.add(requestToken);
    }
    names.addAll(aliases);
    return List.copyOf(names);
  }

  private static List<String> copyNormalized(List<String> values) {
    if (values == null || values.isEmpty()) {
      return List.of();
    }
    ArrayList<String> normalized = new ArrayList<>(values.size());
    for (String value : values) {
      String key = normalize(value);
      if (!key.isEmpty()) {
        normalized.add(key);
      }
    }
    return List.copyOf(normalized);
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim().toLowerCase(java.util.Locale.ROOT);
  }
}

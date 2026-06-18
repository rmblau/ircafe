package cafe.woden.ircclient.ui.chat.embed.spi;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/** Publisher-specific selectors used by the generic news/article preview resolver. */
public record NewsPublisherProfile(
    String key,
    String displayName,
    String[] hostSuffixes,
    String[] paragraphSelectors,
    String[] bylineSelectors,
    String[] imageSelectors,
    String[] authorMetaKeys,
    String[] dateMetaKeys) {

  public NewsPublisherProfile {
    key = normalizeKey(key);
    displayName = normalizeDisplayName(displayName, key);
    hostSuffixes = normalizeArray(hostSuffixes, true);
    paragraphSelectors = normalizeArray(paragraphSelectors, false);
    bylineSelectors = normalizeArray(bylineSelectors, false);
    imageSelectors = normalizeArray(imageSelectors, false);
    authorMetaKeys = normalizeArray(authorMetaKeys, false);
    dateMetaKeys = normalizeArray(dateMetaKeys, false);
  }

  public NewsPublisherProfile(
      String key,
      String displayName,
      String[] paragraphSelectors,
      String[] bylineSelectors,
      String[] imageSelectors,
      String[] authorMetaKeys,
      String[] dateMetaKeys) {
    this(
        key,
        displayName,
        new String[0],
        paragraphSelectors,
        bylineSelectors,
        imageSelectors,
        authorMetaKeys,
        dateMetaKeys);
  }

  @Override
  public String[] hostSuffixes() {
    return hostSuffixes.clone();
  }

  @Override
  public String[] paragraphSelectors() {
    return paragraphSelectors.clone();
  }

  @Override
  public String[] bylineSelectors() {
    return bylineSelectors.clone();
  }

  @Override
  public String[] imageSelectors() {
    return imageSelectors.clone();
  }

  @Override
  public String[] authorMetaKeys() {
    return authorMetaKeys.clone();
  }

  @Override
  public String[] dateMetaKeys() {
    return dateMetaKeys.clone();
  }

  private static String normalizeKey(String key) {
    String normalized = Objects.requireNonNull(key, "key").trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("key must not be blank");
    }
    return normalized;
  }

  private static String normalizeDisplayName(String displayName, String fallbackKey) {
    String normalized = Objects.toString(displayName, "").trim();
    return normalized.isEmpty() ? fallbackKey : normalized;
  }

  private static String[] normalizeArray(String[] values, boolean hostSuffixes) {
    if (values == null || values.length == 0) {
      return new String[0];
    }
    return Arrays.stream(values)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .map(value -> hostSuffixes ? normalizeHostSuffix(value) : value)
        .distinct()
        .toArray(String[]::new);
  }

  private static String normalizeHostSuffix(String value) {
    String suffix = value.toLowerCase(Locale.ROOT);
    return suffix.startsWith("www.") ? suffix.substring(4) : suffix;
  }
}

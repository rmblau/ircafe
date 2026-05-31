package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.mutateMap;

import cafe.woden.ircclient.model.FilterPlaceholderRanges;
import cafe.woden.ircclient.model.FilterRule;
import cafe.woden.ircclient.model.FilterScopeOverride;
import cafe.woden.ircclient.model.RegexSpec;
import cafe.woden.ircclient.model.TagSpec;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns WeeChat-style filter settings under {@code ircafe.ui.filters}. */
class RuntimeConfigFilterStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigFilterStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigFilterStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberEnabledByDefault(boolean enabled) {
    rememberScalarSetting("enabledByDefault", enabled);
  }

  synchronized void rememberPlaceholdersEnabledByDefault(boolean enabled) {
    rememberScalarSetting("placeholdersEnabledByDefault", enabled);
  }

  synchronized void rememberPlaceholdersCollapsedByDefault(boolean collapsed) {
    rememberScalarSetting("placeholdersCollapsedByDefault", collapsed);
  }

  synchronized void rememberPlaceholderMaxPreviewLines(int maxLines) {
    rememberScalarSetting(
        "placeholderMaxPreviewLines", FilterPlaceholderRanges.normalizeMaxPreviewLines(maxLines));
  }

  synchronized void rememberPlaceholderMaxLinesPerRun(int maxLines) {
    rememberScalarSetting(
        "placeholderMaxLinesPerRun", FilterPlaceholderRanges.normalizeMaxLinesPerRun(maxLines));
  }

  synchronized void rememberPlaceholderTooltipMaxTags(int maxTags) {
    rememberScalarSetting(
        "placeholderTooltipMaxTags", FilterPlaceholderRanges.normalizeTooltipMaxTags(maxTags));
  }

  synchronized void rememberHistoryPlaceholderMaxRunsPerBatch(int maxRuns) {
    rememberScalarSetting(
        "historyPlaceholderMaxRunsPerBatch",
        FilterPlaceholderRanges.normalizeHistoryMaxRunsPerBatch(maxRuns));
  }

  synchronized void rememberHistoryPlaceholdersEnabledByDefault(boolean enabled) {
    rememberScalarSetting("historyPlaceholdersEnabledByDefault", enabled);
  }

  synchronized void rememberRules(List<FilterRule> rules) {
    mutateFilterSettings("filter rules", filters -> filters.put("rules", serializeRules(rules)));
  }

  synchronized void rememberOverrides(List<FilterScopeOverride> overrides) {
    mutateFilterSettings(
        "filter overrides", filters -> filters.put("overrides", serializeOverrides(overrides)));
  }

  private void rememberScalarSetting(String key, Object value) {
    mutateFilterSettings("filters " + key + " setting", filters -> filters.put(key, value));
  }

  private void mutateFilterSettings(
      String description, Consumer<Map<String, Object>> mutation) {
    mutateMap(file, documentStore, log, description, mutation, "ircafe", "ui", "filters");
  }

  private static List<Map<String, Object>> serializeRules(List<FilterRule> rules) {
    List<Map<String, Object>> out = new ArrayList<>();
    if (rules == null) return out;

    for (FilterRule r : rules) {
      if (r == null) continue;
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("name", Objects.toString(r.name(), "").trim());
      m.put("enabled", r.enabled());
      m.put("scope", Objects.toString(r.scopePattern(), "*").trim());
      m.put("action", r.action() != null ? r.action().name() : "HIDE");
      m.put("dir", r.direction() != null ? r.direction().name() : "ANY");

      if (r.kinds() != null && !r.kinds().isEmpty()) {
        m.put("kinds", r.kinds().stream().filter(Objects::nonNull).map(Enum::name).toList());
      }
      if (r.fromNickGlobs() != null && !r.fromNickGlobs().isEmpty()) {
        m.put(
            "from",
            r.fromNickGlobs().stream()
                .filter(Objects::nonNull)
                .map(s -> Objects.toString(s, "").trim())
                .filter(s -> !s.isEmpty())
                .toList());
      }

      TagSpec tags = r.tags();
      if (tags != null && !tags.isEmpty()) {
        String expr = Objects.toString(tags.expr(), "").trim();
        if (!expr.isEmpty()) {
          m.put("tags", expr);
        }
      }

      RegexSpec re = r.textRegex();
      if (re != null && !re.isEmpty()) {
        Map<String, Object> tm = new LinkedHashMap<>();
        tm.put("pattern", re.pattern());
        if (re.flags() != null && !re.flags().isEmpty()) {
          String flags =
              re.flags().stream()
                  .map(Enum::name)
                  .map(String::toLowerCase)
                  .sorted()
                  .reduce("", (a, b) -> a + b);
          tm.put("flags", flags);
        }
        m.put("text", tm);
      }

      out.add(m);
    }
    return out;
  }

  private static List<Map<String, Object>> serializeOverrides(
      List<FilterScopeOverride> overrides) {
    List<Map<String, Object>> out = new ArrayList<>();
    if (overrides == null) return out;

    for (FilterScopeOverride o : overrides) {
      if (o == null) continue;
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("scope", Objects.toString(o.scopePattern(), "*").trim());
      if (o.filtersEnabled() != null) m.put("filtersEnabled", o.filtersEnabled());
      if (o.placeholdersEnabled() != null) m.put("placeholdersEnabled", o.placeholdersEnabled());
      if (o.placeholdersCollapsed() != null) m.put("placeholdersCollapsed", o.placeholdersCollapsed());
      out.add(m);
    }
    return out;
  }

}

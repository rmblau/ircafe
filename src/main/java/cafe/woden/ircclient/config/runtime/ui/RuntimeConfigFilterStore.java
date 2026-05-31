package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns WeeChat-style filter settings under {@code ircafe.ui.filters}. */
public final class RuntimeConfigFilterStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigFilterStore.class);

  private final RuntimeConfigYamlSection filtersSection;

  public RuntimeConfigFilterStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.filtersSection =
        RuntimeConfigYamlSection.ircafeUi(file, documentStore, log, "filters");
  }

  public synchronized void rememberEnabledByDefault(boolean enabled) {
    rememberScalarSetting("enabledByDefault", enabled);
  }

  public synchronized void rememberPlaceholdersEnabledByDefault(boolean enabled) {
    rememberScalarSetting("placeholdersEnabledByDefault", enabled);
  }

  public synchronized void rememberPlaceholdersCollapsedByDefault(boolean collapsed) {
    rememberScalarSetting("placeholdersCollapsedByDefault", collapsed);
  }

  public synchronized void rememberPlaceholderMaxPreviewLines(int maxLines) {
    rememberScalarSetting(
        "placeholderMaxPreviewLines", FilterPlaceholderRanges.normalizeMaxPreviewLines(maxLines));
  }

  public synchronized void rememberPlaceholderMaxLinesPerRun(int maxLines) {
    rememberScalarSetting(
        "placeholderMaxLinesPerRun", FilterPlaceholderRanges.normalizeMaxLinesPerRun(maxLines));
  }

  public synchronized void rememberPlaceholderTooltipMaxTags(int maxTags) {
    rememberScalarSetting(
        "placeholderTooltipMaxTags", FilterPlaceholderRanges.normalizeTooltipMaxTags(maxTags));
  }

  public synchronized void rememberHistoryPlaceholderMaxRunsPerBatch(int maxRuns) {
    rememberScalarSetting(
        "historyPlaceholderMaxRunsPerBatch",
        FilterPlaceholderRanges.normalizeHistoryMaxRunsPerBatch(maxRuns));
  }

  public synchronized void rememberHistoryPlaceholdersEnabledByDefault(boolean enabled) {
    rememberScalarSetting("historyPlaceholdersEnabledByDefault", enabled);
  }

  public synchronized void rememberRules(List<FilterRule> rules) {
    filtersSection.mutateMap(
        "filter rules", filters -> filters.put("rules", serializeRules(rules)));
  }

  public synchronized void rememberOverrides(List<FilterScopeOverride> overrides) {
    filtersSection.mutateMap(
        "filter overrides", filters -> filters.put("overrides", serializeOverrides(overrides)));
  }

  private void rememberScalarSetting(String key, Object value) {
    filtersSection.mutateMap("filters " + key + " setting", filters -> filters.put(key, value));
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

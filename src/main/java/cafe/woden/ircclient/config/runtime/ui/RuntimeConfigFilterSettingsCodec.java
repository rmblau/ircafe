package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.model.FilterPlaceholderRanges;
import cafe.woden.ircclient.model.FilterRule;
import cafe.woden.ircclient.model.FilterScopeOverride;
import cafe.woden.ircclient.model.RegexSpec;
import cafe.woden.ircclient.model.TagSpec;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Pure helpers for persisted WeeChat-style filter settings. */
final class RuntimeConfigFilterSettingsCodec {

  enum ScalarSetting {
    ENABLED_BY_DEFAULT("enabledByDefault"),
    PLACEHOLDERS_ENABLED_BY_DEFAULT("placeholdersEnabledByDefault"),
    PLACEHOLDERS_COLLAPSED_BY_DEFAULT("placeholdersCollapsedByDefault"),
    PLACEHOLDER_MAX_PREVIEW_LINES("placeholderMaxPreviewLines"),
    PLACEHOLDER_MAX_LINES_PER_RUN("placeholderMaxLinesPerRun"),
    PLACEHOLDER_TOOLTIP_MAX_TAGS("placeholderTooltipMaxTags"),
    HISTORY_PLACEHOLDER_MAX_RUNS_PER_BATCH("historyPlaceholderMaxRunsPerBatch"),
    HISTORY_PLACEHOLDERS_ENABLED_BY_DEFAULT("historyPlaceholdersEnabledByDefault");

    private final String key;

    ScalarSetting(String key) {
      this.key = key;
    }

    String key() {
      return key;
    }

    String description() {
      return "filters " + key + " setting";
    }
  }

  private RuntimeConfigFilterSettingsCodec() {}

  static int normalizePlaceholderMaxPreviewLines(int maxLines) {
    return FilterPlaceholderRanges.normalizeMaxPreviewLines(maxLines);
  }

  static int normalizePlaceholderMaxLinesPerRun(int maxLines) {
    return FilterPlaceholderRanges.normalizeMaxLinesPerRun(maxLines);
  }

  static int normalizePlaceholderTooltipMaxTags(int maxTags) {
    return FilterPlaceholderRanges.normalizeTooltipMaxTags(maxTags);
  }

  static int normalizeHistoryPlaceholderMaxRunsPerBatch(int maxRuns) {
    return FilterPlaceholderRanges.normalizeHistoryMaxRunsPerBatch(maxRuns);
  }

  static List<Map<String, Object>> serializeRules(List<FilterRule> rules) {
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

  static List<Map<String, Object>> serializeOverrides(List<FilterScopeOverride> overrides) {
    List<Map<String, Object>> out = new ArrayList<>();
    if (overrides == null) return out;

    for (FilterScopeOverride o : overrides) {
      if (o == null) continue;
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("scope", Objects.toString(o.scopePattern(), "*").trim());
      if (o.filtersEnabled() != null) m.put("filtersEnabled", o.filtersEnabled());
      if (o.placeholdersEnabled() != null) m.put("placeholdersEnabled", o.placeholdersEnabled());
      if (o.placeholdersCollapsed() != null)
        m.put("placeholdersCollapsed", o.placeholdersCollapsed());
      out.add(m);
    }
    return out;
  }
}

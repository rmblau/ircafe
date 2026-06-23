package cafe.woden.ircclient.ui.filter;

import cafe.woden.ircclient.config.api.FilterSettingsConfigPort;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Publishes the current filter settings snapshot. */
@Component
@InterfaceLayer
@Lazy
public class FilterSettingsBus {

  public static final String PROP_FILTER_SETTINGS = "filterSettings";

  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private volatile FilterSettings current;

  public FilterSettingsBus(FilterSettingsConfigPort runtimeConfig) {
    this.current = fromSnapshot(runtimeConfig);
  }

  private static FilterSettings fromSnapshot(FilterSettingsConfigPort runtimeConfig) {
    if (runtimeConfig == null) {
      return FilterSettings.defaults();
    }
    FilterSettingsConfigPort.FilterSettingsSnapshot snapshot = runtimeConfig.readFilterSettings();
    if (snapshot == null) {
      return FilterSettings.defaults();
    }
    return new FilterSettings(
        snapshot.filtersEnabledByDefault(),
        snapshot.placeholdersEnabledByDefault(),
        snapshot.placeholdersCollapsedByDefault(),
        snapshot.placeholderMaxPreviewLines(),
        snapshot.placeholderMaxLinesPerRun(),
        snapshot.placeholderTooltipMaxTags(),
        snapshot.historyPlaceholderMaxRunsPerBatch(),
        snapshot.historyPlaceholdersEnabledByDefault(),
        snapshot.rules(),
        snapshot.overrides());
  }

  public FilterSettings get() {
    return current;
  }

  public void set(FilterSettings next) {
    FilterSettings prev = this.current;
    this.current = next;
    pcs.firePropertyChange(PROP_FILTER_SETTINGS, prev, next);
  }

  public void refresh() {
    FilterSettings cur = this.current;
    pcs.firePropertyChange(PROP_FILTER_SETTINGS, cur, cur);
  }

  public void addListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  public void removeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }
}

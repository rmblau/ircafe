package cafe.woden.ircclient.ui.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.config.api.FilterSettingsConfigPort;
import cafe.woden.ircclient.config.api.FilterSettingsConfigPort.FilterSettingsSnapshot;
import java.util.List;
import org.junit.jupiter.api.Test;

class FilterSettingsBusTest {

  @Test
  void usesFilterSettingsDefaultsWhenConfigIsAbsent() {
    FilterSettingsBus bus = new FilterSettingsBus(null);

    assertEquals(FilterSettings.defaults(), bus.get());
  }

  @Test
  void initializesFromFilterSettingsPortSnapshot() {
    FilterSettingsConfigPort runtimeConfig = mock(FilterSettingsConfigPort.class);
    when(runtimeConfig.readFilterSettings())
        .thenReturn(
            new FilterSettingsSnapshot(
                false, true, false, 7, 300, 12, 80, false, List.of(), List.of()));

    FilterSettingsBus bus = new FilterSettingsBus(runtimeConfig);

    assertEquals(
        new FilterSettings(false, true, false, 7, 300, 12, 80, false, List.of(), List.of()),
        bus.get());
  }
}

package cafe.woden.ircclient.ui.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FilterSettingsBusTest {

  @Test
  void usesFilterSettingsDefaultsWhenConfigIsAbsent() {
    FilterSettingsBus bus = new FilterSettingsBus(null);

    assertEquals(FilterSettings.defaults(), bus.get());
  }
}

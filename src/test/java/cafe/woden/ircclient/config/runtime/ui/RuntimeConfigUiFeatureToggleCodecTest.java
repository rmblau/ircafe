package cafe.woden.ircclient.config.runtime.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUiFeatureToggleCodec.Setting;
import org.junit.jupiter.api.Test;

class RuntimeConfigUiFeatureToggleCodecTest {

  @Test
  void settingMetadataKeepsPersistedPathsStable() {
    assertEquals("invites", Setting.INVITE_AUTO_JOIN.section());
    assertEquals("autoJoinOnInvite", Setting.INVITE_AUTO_JOIN.key());
    assertEquals("invites.autoJoinOnInvite", Setting.INVITE_AUTO_JOIN.description());

    assertEquals("updateNotifier", Setting.UPDATE_NOTIFIER.section());
    assertEquals("enabled", Setting.UPDATE_NOTIFIER.key());
    assertEquals("ui.updateNotifier.enabled", Setting.UPDATE_NOTIFIER.description());

    assertEquals("lagIndicator", Setting.LAG_INDICATOR.section());
    assertEquals("enabled", Setting.LAG_INDICATOR.key());
    assertEquals("ui.lagIndicator.enabled", Setting.LAG_INDICATOR.description());
  }

  @Test
  void readBooleanAcceptsYamlCompatibleBooleanRepresentations() {
    assertTrue(RuntimeConfigUiFeatureToggleCodec.readBoolean(true, false));
    assertFalse(RuntimeConfigUiFeatureToggleCodec.readBoolean(" false ", true));
    assertTrue(RuntimeConfigUiFeatureToggleCodec.readBoolean(1, false));
    assertFalse(RuntimeConfigUiFeatureToggleCodec.readBoolean(0, true));
  }

  @Test
  void readBooleanUsesCallerFallbackForMalformedValues() {
    assertTrue(RuntimeConfigUiFeatureToggleCodec.readBoolean("enabled", true));
    assertFalse(RuntimeConfigUiFeatureToggleCodec.readBoolean(2, false));
    assertTrue(RuntimeConfigUiFeatureToggleCodec.readBoolean(null, true));
  }
}

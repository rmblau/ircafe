package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigCtcpAutoReplySettingsCodec.Setting.ENABLED;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigCtcpAutoReplySettingsCodec.Setting.PING;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigCtcpAutoReplySettingsCodec.Setting.TIME;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigCtcpAutoReplySettingsCodec.Setting.VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class RuntimeConfigCtcpAutoReplySettingsCodecTest {

  @Test
  void settingDefinitionsExposeStableYamlKeysAndDescriptions() {
    assertEquals("enabled", ENABLED.key());
    assertEquals("version", VERSION.key());
    assertEquals("ping", PING.key());
    assertEquals("time", TIME.key());
    assertEquals("ui.ctcpReplies.enabled", ENABLED.description());
  }

  @Test
  void readBooleanUsesRuntimeConfigBooleanCoercion() {
    assertEquals(Optional.of(true), RuntimeConfigCtcpAutoReplySettingsCodec.readBoolean("true"));
    assertEquals(Optional.of(false), RuntimeConfigCtcpAutoReplySettingsCodec.readBoolean(0));
    assertTrue(RuntimeConfigCtcpAutoReplySettingsCodec.readBoolean("maybe").isEmpty());
  }
}

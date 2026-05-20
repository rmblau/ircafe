package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

class RuntimeConfigStoreChatBehaviorSettingsTest {

  @TempDir Path tempDir;

  @Test
  void persistsChatBehaviorSettingsUnderUiSection() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigStore store = RuntimeConfigStoreTestFixtures.store(cfg);

    store.rememberPresenceFoldsEnabled(false);
    store.rememberDefaultQuitMessage("Bye\nnow");
    store.rememberCtcpRequestsInActiveTargetEnabled(false);
    store.rememberTypingIndicatorsEnabled(false);
    store.rememberTypingIndicatorsReceiveEnabled(false);
    store.rememberTypingTreeIndicatorStyle("KBD");
    store.rememberTypingIndicatorsTreeEnabled(false);
    store.rememberTypingIndicatorsUsersListEnabled(false);
    store.rememberMatrixUserListNameDisplayMode("full");
    store.rememberTypingIndicatorsTranscriptEnabled(false);
    store.rememberTypingIndicatorsSendSignalEnabled(false);

    Map<String, Object> ui = section(section(loadYaml(cfg), "ircafe"), "ui");
    assertEquals(false, ui.get("presenceFoldsEnabled"));
    assertEquals("Bye now", ui.get("defaultQuitMessage"));
    assertEquals(false, ui.get("ctcpRequestsInActiveTargetEnabled"));
    assertEquals(false, ui.get("typingIndicatorsEnabled"));
    assertEquals(false, ui.get("typingIndicatorsReceiveEnabled"));
    assertEquals("keyboard", ui.get("typingTreeIndicatorStyle"));
    assertEquals(false, ui.get("typingIndicatorsTreeEnabled"));
    assertEquals(false, ui.get("typingIndicatorsUsersListEnabled"));
    assertEquals("verbose", ui.get("matrixUserListNameDisplayMode"));
    assertEquals(false, ui.get("typingIndicatorsTranscriptEnabled"));
    assertEquals(false, ui.get("typingIndicatorsSendSignalEnabled"));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> loadYaml(Path cfg) throws Exception {
    return (Map<String, Object>) new Yaml().load(Files.readString(cfg));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> section(Map<String, Object> parent, String key) {
    return (Map<String, Object>) parent.get(key);
  }
}

package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.ChatAppearanceRuntimeConfigPort;
import cafe.woden.ircclient.config.api.DockLayoutRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ServerTreeAppearanceRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ThemeAppearanceRuntimeConfigPort;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class RuntimeConfigAppearancePortBoundaryTest {

  @Test
  void appearanceAdapterImplementsOnlyAppearanceContracts() {
    assertTrue(
        ThemeAppearanceRuntimeConfigPort.class.isAssignableFrom(
            RuntimeConfigAppearanceAdapter.class));
    assertTrue(
        ChatAppearanceRuntimeConfigPort.class.isAssignableFrom(
            RuntimeConfigAppearanceAdapter.class));
    assertTrue(
        ServerTreeAppearanceRuntimeConfigPort.class.isAssignableFrom(
            RuntimeConfigAppearanceAdapter.class));
    assertFalse(
        DockLayoutRuntimeConfigPort.class.isAssignableFrom(RuntimeConfigAppearanceAdapter.class));
  }

  @Test
  void dockLayoutPreferenceUsesTheShellAdapter() {
    assertTrue(
        DockLayoutRuntimeConfigPort.class.isAssignableFrom(RuntimeConfigServerTreeAdapter.class));
    assertTrue(
        methodNames(DockLayoutRuntimeConfigPort.class).contains("rememberPreserveDockLayout"));
  }

  @Test
  void focusedContractsDoNotReaggregateOtherAppearanceSettings() {
    assertEquals(
        Set.of(
            "rememberAccentColor",
            "rememberAccentStrength",
            "rememberUiDensity",
            "rememberUiFontOverrideEnabled",
            "rememberUiFontFamily",
            "rememberUiFontSize",
            "rememberCornerRadius"),
        methodNames(ThemeAppearanceRuntimeConfigPort.class));
    assertEquals(
        Set.of(
            "rememberChatThemePreset",
            "rememberChatTimestampColor",
            "rememberChatSystemColor",
            "rememberChatMessageColor",
            "rememberChatNoticeColor",
            "rememberChatActionColor",
            "rememberChatErrorColor",
            "rememberChatPresenceColor",
            "rememberChatMentionBgColor",
            "rememberChatMentionStrength"),
        methodNames(ChatAppearanceRuntimeConfigPort.class));
    assertEquals(
        Set.of("rememberServerTreeUnreadChannelColor", "rememberServerTreeHighlightChannelColor"),
        methodNames(ServerTreeAppearanceRuntimeConfigPort.class));
  }

  private static Set<String> methodNames(Class<?> type) {
    return Arrays.stream(type.getDeclaredMethods())
        .map(Method::getName)
        .collect(Collectors.toUnmodifiableSet());
  }
}

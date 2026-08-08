package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.ChatBehaviorRuntimeConfigPort;
import cafe.woden.ircclient.config.api.InviteAutoJoinConfigPort;
import cafe.woden.ircclient.config.api.IrcSessionRuntimeConfigPort;
import cafe.woden.ircclient.config.api.PreferredNickRuntimeConfigPort;
import cafe.woden.ircclient.config.api.QuitMessageRuntimeConfigPort;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class RuntimeConfigOutboundCommandPortBoundaryTest {

  @Test
  void outboundCommandPersistenceRemainsSplitByConcern() {
    assertEquals(Set.of("rememberNick"), methodNames(PreferredNickRuntimeConfigPort.class));
    assertEquals(Set.of("readDefaultQuitMessage"), methodNames(QuitMessageRuntimeConfigPort.class));
    assertEquals(
        Set.of("readInviteAutoJoinEnabled", "rememberInviteAutoJoinEnabled"),
        methodNames(InviteAutoJoinConfigPort.class));
    assertTrue(
        QuitMessageRuntimeConfigPort.class.isAssignableFrom(ChatBehaviorRuntimeConfigPort.class));
    assertTrue(
        QuitMessageRuntimeConfigPort.class.isAssignableFrom(
            RuntimeConfigChatBehaviorAdapter.class));
    assertTrue(
        PreferredNickRuntimeConfigPort.class.isAssignableFrom(
            RuntimeConfigPreferredNickAdapter.class));
    assertFalse(
        PreferredNickRuntimeConfigPort.class.isAssignableFrom(IrcSessionRuntimeConfigPort.class));
  }

  private static Set<String> methodNames(Class<?> type) {
    return Arrays.stream(type.getDeclaredMethods())
        .map(Method::getName)
        .collect(Collectors.toSet());
  }
}

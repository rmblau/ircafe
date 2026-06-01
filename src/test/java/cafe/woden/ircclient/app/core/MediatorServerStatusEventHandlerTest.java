package cafe.woden.ircclient.app.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.app.api.UiPort;
import cafe.woden.ircclient.irc.IrcEvent;
import cafe.woden.ircclient.irc.port.IrcMediatorInteractionPort;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.model.TargetRef;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MediatorServerStatusEventHandlerTest {

  @Test
  void serverUserModeReplyUpdatesUiModeSnapshot() {
    UiPort ui = mock(UiPort.class);
    MediatorServerStatusEventHandler handler = newHandler(null, ui);

    handler.handleServerResponseLineEvent(
        callbacks(),
        "libera",
        new TargetRef("libera", "status"),
        new IrcEvent.ServerResponseLine(Instant.EPOCH, 221, "+g", ":irc.example 221 agarose :+g"));

    verify(ui).setServerUserModes("libera", "+g");
  }

  @Test
  void userModeCommandUpdatesUiOnlyForCurrentNick() {
    IrcMediatorInteractionPort irc = mock(IrcMediatorInteractionPort.class);
    UiPort ui = mock(UiPort.class);
    when(irc.currentNick("libera")).thenReturn(Optional.of("agarose"));
    MediatorServerStatusEventHandler handler = newHandler(irc, ui);

    handler.handleServerResponseLineEvent(
        callbacks(),
        "libera",
        new TargetRef("libera", "status"),
        new IrcEvent.ServerResponseLine(Instant.EPOCH, 0, "+i", ":agarose MODE agarose :+i"));

    verify(ui).setServerUserModes("libera", "+i");
  }

  @Test
  void userModeCommandIgnoresOtherNickModes() {
    IrcMediatorInteractionPort irc = mock(IrcMediatorInteractionPort.class);
    UiPort ui = mock(UiPort.class);
    when(irc.currentNick("libera")).thenReturn(Optional.of("agarose"));
    MediatorServerStatusEventHandler handler = newHandler(irc, ui);

    handler.handleServerResponseLineEvent(
        callbacks(),
        "libera",
        new TargetRef("libera", "status"),
        new IrcEvent.ServerResponseLine(Instant.EPOCH, 0, "+i", ":alice MODE alice :+i"));

    verifyNoInteractionsBeyondStatusRendering(ui);
  }

  @Test
  void namesReplyContainingKlineNickDoesNotNotifyServerRestriction() {
    UiPort ui = mock(UiPort.class);
    MediatorServerStatusEventHandler.Callbacks callbacks = callbacks();
    MediatorServerStatusEventHandler handler = newHandler(null, ui);

    handler.handleServerResponseLineEvent(
        callbacks,
        "libera",
        new TargetRef("libera", "status"),
        new IrcEvent.ServerResponseLine(
            Instant.EPOCH,
            353,
            "hostghost @kline Teto",
            ":irc.example 353 agarose = #ircafe :hostghost @kline Teto"));

    verify(callbacks, never()).notifyIrcEvent(any(), any(), any(), any(), any(), any());
  }

  @Test
  void explicitServerRestrictionNumericNotifiesServerRestriction() {
    UiPort ui = mock(UiPort.class);
    MediatorServerStatusEventHandler.Callbacks callbacks = callbacks();
    MediatorServerStatusEventHandler handler = newHandler(null, ui);

    handler.handleServerResponseLineEvent(
        callbacks,
        "libera",
        new TargetRef("libera", "status"),
        new IrcEvent.ServerResponseLine(
            Instant.EPOCH,
            465,
            "You are banned from this server",
            ":irc.example 465 agarose :You are banned from this server"));

    verify(callbacks)
        .notifyIrcEvent(
            eq(IrcEventNotificationRule.EventType.YOU_KLINED),
            eq("libera"),
            isNull(),
            isNull(),
            eq("Server restriction"),
            eq("[465] You are banned from this server"));
  }

  private static MediatorServerStatusEventHandler newHandler(
      IrcMediatorInteractionPort irc, UiPort ui) {
    return new MediatorServerStatusEventHandler(irc, ui, null, null, null, null, null, null, null);
  }

  private static MediatorServerStatusEventHandler.Callbacks callbacks() {
    return mock(MediatorServerStatusEventHandler.Callbacks.class);
  }

  private static void verifyNoInteractionsBeyondStatusRendering(UiPort ui) {
    verify(ui).ensureTargetExists(new TargetRef("libera", "status"));
    verify(ui)
        .appendStatusAt(
            new TargetRef("libera", "status"), Instant.EPOCH, "(server)", "[0] +i", "", Map.of());
    verifyNoMoreInteractions(ui);
  }
}

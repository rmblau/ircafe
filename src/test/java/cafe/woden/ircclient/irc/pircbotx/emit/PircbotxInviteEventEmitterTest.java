package cafe.woden.ircclient.irc.pircbotx.emit;

import static cafe.woden.ircclient.irc.pircbotx.PircbotxRuntimeTestFixtures.inviteEvents;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.irc.*;
import cafe.woden.ircclient.irc.backend.*;
import cafe.woden.ircclient.irc.ircv3.*;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import cafe.woden.ircclient.irc.playback.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.UserHostmask;
import org.pircbotx.hooks.events.InviteEvent;

class PircbotxInviteEventEmitterTest {

  @Test
  void onInviteEmitsInviteUsingEventFieldsWhenNoRawLineIsAvailable() {
    List<ServerIrcEvent> events = new ArrayList<>();
    PircbotxRosterEmitter rosterEmitter = mock(PircbotxRosterEmitter.class);
    PircbotxInviteEventEmitter emitter = newEmitter(events, rosterEmitter);
    InviteEvent event = mock(InviteEvent.class);
    User user = mock(User.class);
    when(user.getNick()).thenReturn("alice");
    when(event.getChannel()).thenReturn("#ircafe");
    when(event.getUser()).thenReturn(user);

    emitter.onInvite(event);

    verify(rosterEmitter).maybeEmitHostmaskObserved("#ircafe", user);
    assertEquals(1, events.size());
    IrcEvent.InvitedToChannel invite =
        assertInstanceOf(IrcEvent.InvitedToChannel.class, events.getFirst().event());
    assertEquals("#ircafe", invite.channel());
    assertEquals("alice", invite.from());
    assertEquals("", invite.invitee());
    assertEquals("", invite.reason());
  }

  @Test
  void onInvitePrefersParsedRawLineDetailsWhenAvailable() {
    List<ServerIrcEvent> events = new ArrayList<>();
    PircbotxRosterEmitter rosterEmitter = mock(PircbotxRosterEmitter.class);
    PircbotxInviteEventEmitter emitter = newEmitter(events, rosterEmitter);
    User user = mock(User.class);
    when(user.getNick()).thenReturn("alice");
    InviteEvent event =
        new RawInviteEvent(
            mock(PircBotX.class),
            mock(UserHostmask.class),
            user,
            "#fallback",
            ":alice!ident@host INVITE me #ircafe :join us");

    emitter.onInvite(event);

    verify(rosterEmitter).maybeEmitHostmaskObserved("#fallback", user);
    assertEquals(1, events.size());
    IrcEvent.InvitedToChannel invite =
        assertInstanceOf(IrcEvent.InvitedToChannel.class, events.getFirst().event());
    assertEquals("#ircafe", invite.channel());
    assertEquals("alice", invite.from());
    assertEquals("me", invite.invitee());
    assertEquals("join us", invite.reason());
  }

  @Test
  void onInviteUsesReplacementInviteNotifyRuntimeProvider() {
    List<ServerIrcEvent> events = new ArrayList<>();
    PircbotxRosterEmitter rosterEmitter = mock(PircbotxRosterEmitter.class);
    Ircv3InboundCommandSignalRuntimeCatalog catalog =
        Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(
            List.of(
                new Ircv3InboundCommandSignalProvider() {
                  @Override
                  public String providerId() {
                    return "replacement-invite-notify";
                  }

                  @Override
                  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
                    return Set.of(Ircv3InboundCommandOperation.INVITE_NOTIFY);
                  }

                  @Override
                  public List<Ircv3InboundCommandSignal> parse(
                      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
                    return List.of(
                        new Ircv3InboundCommandSignal.InviteObserved(
                            "plugin-source", "plugin-invitee", "#plugin", "plugin reason"));
                  }
                }));
    PircbotxInviteEventEmitter emitter =
        new PircbotxInviteEventEmitter("libera", rosterEmitter, catalog, events::add);
    User user = mock(User.class);
    when(user.getNick()).thenReturn("alice");
    InviteEvent event =
        new RawInviteEvent(
            mock(PircBotX.class),
            mock(UserHostmask.class),
            user,
            "#fallback",
            ":alice!ident@host INVITE me #ircafe :join us");

    emitter.onInvite(event);

    IrcEvent.InvitedToChannel invite =
        assertInstanceOf(IrcEvent.InvitedToChannel.class, events.getFirst().event());
    assertEquals("#plugin", invite.channel());
    assertEquals("plugin-source", invite.from());
    assertEquals("plugin-invitee", invite.invitee());
    assertEquals("plugin reason", invite.reason());
  }

  private static PircbotxInviteEventEmitter newEmitter(
      List<ServerIrcEvent> events, PircbotxRosterEmitter rosterEmitter) {
    return inviteEvents("libera", rosterEmitter, events::add);
  }

  private static final class RawInviteEvent extends InviteEvent {
    private final String rawLine;

    private RawInviteEvent(
        PircBotX bot, UserHostmask userHostmask, User user, String channel, String rawLine) {
      super(bot, userHostmask, user, channel);
      this.rawLine = rawLine;
    }

    @SuppressWarnings("unused")
    public String getRawLine() {
      return rawLine;
    }
  }
}

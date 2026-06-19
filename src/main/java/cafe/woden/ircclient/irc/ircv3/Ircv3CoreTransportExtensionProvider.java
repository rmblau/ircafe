package cafe.woden.ircclient.irc.ircv3;

import static cafe.woden.ircclient.util.Ircv3CapabilityNames.ACCOUNT_NOTIFY;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.AWAY_NOTIFY;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.CAP_NOTIFY;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.CHGHOST;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.ECHO_MESSAGE;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.EXTENDED_JOIN;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.EXTENDED_MONITOR;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.INVITE_NOTIFY;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.LABELED_RESPONSE;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.MESSAGE_TAGS;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.MONITOR;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.MULTI_PREFIX;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.SERVER_TIME;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.SETNAME;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.STANDARD_REPLIES;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import com.google.auto.service.AutoService;
import java.util.List;

/** Built-in provider for the core IRCv3 transport and metadata capabilities. */
@AutoService(Ircv3ExtensionProvider.class)
public final class Ircv3CoreTransportExtensionProvider implements Ircv3ExtensionProvider {

  @Override
  public String providerId() {
    return "core-transport";
  }

  @Override
  public int sortOrder() {
    return 100;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        Ircv3ExtensionProviderSupport.capability(
            MULTI_PREFIX,
            Ircv3SpecStatus.STABLE,
            MULTI_PREFIX,
            Ircv3UiGroup.CORE,
            130,
            "Preserves all nick privilege prefixes (not just the highest) in user data."),
        Ircv3ExtensionProviderSupport.capability(
            CAP_NOTIFY,
            Ircv3SpecStatus.STABLE,
            "CAP updates",
            Ircv3UiGroup.CORE,
            140,
            "Allows capability change notifications after initial connection."),
        Ircv3ExtensionProviderSupport.capability(
            INVITE_NOTIFY,
            Ircv3SpecStatus.STABLE,
            "Invite notifications",
            Ircv3UiGroup.CORE,
            145,
            "Receives invite events for channels you share without extra queries."),
        Ircv3ExtensionProviderSupport.capability(
            AWAY_NOTIFY,
            Ircv3SpecStatus.STABLE,
            "Away status updates",
            Ircv3UiGroup.CORE,
            90,
            "Tracks away/back state transitions for users."),
        Ircv3ExtensionProviderSupport.capability(
            ACCOUNT_NOTIFY,
            Ircv3SpecStatus.STABLE,
            "Account status updates",
            Ircv3UiGroup.CORE,
            80,
            "Tracks account login/logout changes for users."),
        Ircv3ExtensionProviderSupport.capability(
            MONITOR,
            Ircv3SpecStatus.STABLE,
            "MONITOR",
            Ircv3UiGroup.CORE,
            155,
            "Lets IRCafe track online/offline state for monitored nicknames."),
        Ircv3ExtensionProviderSupport.capability(
            EXTENDED_MONITOR,
            Ircv3SpecStatus.STABLE,
            "Extended MONITOR",
            Ircv3UiGroup.CORE,
            160,
            "Extends MONITOR presence notifications to additional events."),
        Ircv3ExtensionProviderSupport.capability(
            EXTENDED_JOIN,
            Ircv3SpecStatus.STABLE,
            "Extended join data",
            Ircv3UiGroup.CORE,
            100,
            "Adds account/realname metadata to join events when available."),
        Ircv3ExtensionProviderSupport.capability(
            SETNAME,
            Ircv3SpecStatus.STABLE,
            "Setname updates",
            Ircv3UiGroup.CORE,
            120,
            "Receives user real-name changes without extra lookups."),
        Ircv3ExtensionProviderSupport.capability(
            CHGHOST,
            Ircv3SpecStatus.STABLE,
            "Hostmask changes",
            Ircv3UiGroup.CORE,
            110,
            "Keeps hostmask/userhost identity changes in sync."),
        Ircv3ExtensionProviderSupport.capability(
            MESSAGE_TAGS,
            Ircv3SpecStatus.STABLE,
            "Message tags",
            Ircv3UiGroup.CORE,
            10,
            "Foundation for many IRCv3 features: carries structured metadata on messages."),
        Ircv3ExtensionProviderSupport.capability(
            SERVER_TIME,
            Ircv3SpecStatus.STABLE,
            "Server timestamps",
            Ircv3UiGroup.CORE,
            30,
            "Uses server-provided timestamps to improve ordering and replay accuracy."),
        Ircv3ExtensionProviderSupport.capability(
            STANDARD_REPLIES,
            Ircv3SpecStatus.STABLE,
            "Standard replies",
            Ircv3UiGroup.CORE,
            60,
            "Provides structured success/error replies from the server."),
        Ircv3ExtensionProviderSupport.capability(
            ECHO_MESSAGE,
            Ircv3SpecStatus.STABLE,
            "Echo own messages",
            Ircv3UiGroup.CORE,
            40,
            "Server echoes your outbound messages, improving multi-client/bouncer consistency."),
        Ircv3ExtensionProviderSupport.capability(
            LABELED_RESPONSE,
            Ircv3SpecStatus.STABLE,
            "Labeled responses",
            Ircv3UiGroup.CORE,
            50,
            "Correlates command responses with requests more reliably."));
  }
}

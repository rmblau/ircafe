package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import com.google.auto.service.AutoService;
import java.util.List;

/** Built-in provider for remaining non-transport IRCv3 metadata and tag features. */
@AutoService(Ircv3ExtensionProvider.class)
public final class Ircv3CoreMiscExtensionProvider implements Ircv3ExtensionProvider {

  @Override
  public String providerId() {
    return "core-misc";
  }

  @Override
  public int sortOrder() {
    return 300;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        Ircv3ExtensionProviderSupport.capability(
            Ircv3CapabilityNames.ZNC_PLAYBACK,
            Ircv3SpecStatus.STABLE,
            "ZNC playback",
            Ircv3UiGroup.HISTORY,
            440,
            "Requests playback support from ZNC bouncers when available."),
        Ircv3ExtensionProviderSupport.capability(
            Ircv3CapabilityNames.ACCOUNT_TAG,
            Ircv3SpecStatus.STABLE,
            "Account tags",
            Ircv3UiGroup.CORE,
            70,
            "Attaches account metadata to messages for richer identity info."),
        Ircv3ExtensionProviderSupport.capability(
            Ircv3CapabilityNames.USERHOST_IN_NAMES,
            Ircv3SpecStatus.STABLE,
            "USERHOST in NAMES",
            Ircv3UiGroup.CORE,
            150,
            "May provide richer host/user identity details during names lists."),
        Ircv3ExtensionProviderSupport.nonRequestableCapability(
            Ircv3CapabilityNames.STS,
            Ircv3SpecStatus.STABLE,
            "Strict transport security",
            Ircv3UiGroup.CORE,
            20,
            "Learns strict transport policy and upgrades future connects for this host to TLS."),
        Ircv3ExtensionProviderSupport.tagFeature(
            Ircv3CapabilityNames.REPLY,
            Ircv3SpecStatus.STABLE,
            "Replies",
            Ircv3UiGroup.CONVERSATION,
            250,
            "Reply threading is carried by message tags on top of message-tags transport.",
            Ircv3CapabilityNames.DRAFT_REPLY),
        Ircv3ExtensionProviderSupport.tagFeature(
            Ircv3CapabilityNames.REACT,
            Ircv3SpecStatus.DRAFT,
            "Reactions",
            Ircv3UiGroup.CONVERSATION,
            260,
            "Reactions are carried by message tags on top of message-tags transport.",
            Ircv3CapabilityNames.DRAFT_REACT),
        Ircv3ExtensionProviderSupport.tagFeature(
            Ircv3CapabilityNames.UNREACT,
            Ircv3SpecStatus.DRAFT,
            "Reaction removal",
            Ircv3UiGroup.CONVERSATION,
            265,
            "Reaction removals are carried by message tags on top of message-tags transport.",
            Ircv3CapabilityNames.DRAFT_UNREACT),
        Ircv3ExtensionProviderSupport.tagFeature(
            Ircv3CapabilityNames.TYPING,
            Ircv3SpecStatus.STABLE,
            "Typing",
            Ircv3UiGroup.CONVERSATION,
            230,
            "Typing indicators are sent as client-only tags and depend on CLIENTTAGDENY policy.",
            Ircv3CapabilityNames.DRAFT_TYPING),
        Ircv3ExtensionProviderSupport.tagFeature(
            Ircv3CapabilityNames.CHANNEL_CONTEXT,
            Ircv3SpecStatus.DRAFT,
            "Channel context",
            Ircv3UiGroup.CONVERSATION,
            245,
            "Channel-context is a client tag layered on top of message-tags transport.",
            Ircv3CapabilityNames.DRAFT_CHANNEL_CONTEXT),
        Ircv3ExtensionProviderSupport.experimental(
            Ircv3CapabilityNames.MESSAGE_EDIT,
            "Message edits (experimental)",
            Ircv3UiGroup.CONVERSATION,
            280,
            "Experimental message editing support; not part of the published IRCv3 surface.",
            Ircv3CapabilityNames.DRAFT_MESSAGE_EDIT));
  }

  @Override
  public List<Ircv3FeatureContribution> visibleFeatures() {
    return List.of(
        Ircv3ExtensionProviderSupport.feature(100, "Replies", List.of(Ircv3CapabilityNames.MESSAGE_TAGS), List.of()),
        Ircv3ExtensionProviderSupport.feature(200, "Reactions", List.of(Ircv3CapabilityNames.MESSAGE_TAGS), List.of()),
        Ircv3ExtensionProviderSupport.feature(
            300, "Reaction removal", List.of(Ircv3CapabilityNames.MESSAGE_TAGS), List.of()),
        Ircv3ExtensionProviderSupport.feature(600, "Typing", List.of(Ircv3CapabilityNames.MESSAGE_TAGS), List.of()));
  }
}

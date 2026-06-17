package cafe.woden.ircclient.irc.ircv3;

import static cafe.woden.ircclient.util.Ircv3CapabilityNames.ACCOUNT_TAG;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.CHANNEL_CONTEXT;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.DRAFT_CHANNEL_CONTEXT;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.DRAFT_MESSAGE_EDIT;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.DRAFT_REACT;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.DRAFT_REPLY;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.DRAFT_TYPING;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.DRAFT_UNREACT;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.MESSAGE_EDIT;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.MESSAGE_TAGS;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.REACT;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.REPLY;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.STS;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.TYPING;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.UNREACT;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.USERHOST_IN_NAMES;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.ZNC_PLAYBACK;

import java.util.List;

/** Built-in provider for remaining non-transport IRCv3 metadata and tag features. */
public final class Ircv3CoreMiscExtensionProvider
    implements cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider {

  @Override
  public String providerId() {
    return "core-misc";
  }

  @Override
  public int sortOrder() {
    return 300;
  }

  @Override
  public List<Ircv3ExtensionRegistry.ExtensionDefinition> extensions() {
    return List.of(
        Ircv3ExtensionProviderSupport.capability(
            ZNC_PLAYBACK,
            Ircv3ExtensionRegistry.SpecStatus.STABLE,
            "ZNC playback",
            Ircv3ExtensionRegistry.UiGroup.HISTORY,
            440,
            "Requests playback support from ZNC bouncers when available."),
        Ircv3ExtensionProviderSupport.capability(
            ACCOUNT_TAG,
            Ircv3ExtensionRegistry.SpecStatus.STABLE,
            "Account tags",
            Ircv3ExtensionRegistry.UiGroup.CORE,
            70,
            "Attaches account metadata to messages for richer identity info."),
        Ircv3ExtensionProviderSupport.capability(
            USERHOST_IN_NAMES,
            Ircv3ExtensionRegistry.SpecStatus.STABLE,
            "USERHOST in NAMES",
            Ircv3ExtensionRegistry.UiGroup.CORE,
            150,
            "May provide richer host/user identity details during names lists."),
        Ircv3ExtensionProviderSupport.nonRequestableCapability(
            STS,
            Ircv3ExtensionRegistry.SpecStatus.STABLE,
            "Strict transport security",
            Ircv3ExtensionRegistry.UiGroup.CORE,
            20,
            "Learns strict transport policy and upgrades future connects for this host to TLS."),
        Ircv3ExtensionProviderSupport.tagFeature(
            REPLY,
            Ircv3ExtensionRegistry.SpecStatus.STABLE,
            "Replies",
            Ircv3ExtensionRegistry.UiGroup.CONVERSATION,
            250,
            "Reply threading is carried by message tags on top of message-tags transport.",
            DRAFT_REPLY),
        Ircv3ExtensionProviderSupport.tagFeature(
            REACT,
            Ircv3ExtensionRegistry.SpecStatus.DRAFT,
            "Reactions",
            Ircv3ExtensionRegistry.UiGroup.CONVERSATION,
            260,
            "Reactions are carried by message tags on top of message-tags transport.",
            DRAFT_REACT),
        Ircv3ExtensionProviderSupport.tagFeature(
            UNREACT,
            Ircv3ExtensionRegistry.SpecStatus.DRAFT,
            "Reaction removal",
            Ircv3ExtensionRegistry.UiGroup.CONVERSATION,
            265,
            "Reaction removals are carried by message tags on top of message-tags transport.",
            DRAFT_UNREACT),
        Ircv3ExtensionProviderSupport.tagFeature(
            TYPING,
            Ircv3ExtensionRegistry.SpecStatus.STABLE,
            "Typing",
            Ircv3ExtensionRegistry.UiGroup.CONVERSATION,
            230,
            "Typing indicators are sent as client-only tags and depend on CLIENTTAGDENY policy.",
            DRAFT_TYPING),
        Ircv3ExtensionProviderSupport.tagFeature(
            CHANNEL_CONTEXT,
            Ircv3ExtensionRegistry.SpecStatus.DRAFT,
            "Channel context",
            Ircv3ExtensionRegistry.UiGroup.CONVERSATION,
            245,
            "Channel-context is a client tag layered on top of message-tags transport.",
            DRAFT_CHANNEL_CONTEXT),
        Ircv3ExtensionProviderSupport.experimental(
            MESSAGE_EDIT,
            "Message edits (experimental)",
            Ircv3ExtensionRegistry.UiGroup.CONVERSATION,
            280,
            "Experimental message editing support; not part of the published IRCv3 surface.",
            DRAFT_MESSAGE_EDIT));
  }

  @Override
  public List<Ircv3ExtensionRegistry.FeatureDefinition> visibleFeatures() {
    return List.of(
        Ircv3ExtensionProviderSupport.feature(100, "Replies", List.of(MESSAGE_TAGS), List.of()),
        Ircv3ExtensionProviderSupport.feature(200, "Reactions", List.of(MESSAGE_TAGS), List.of()),
        Ircv3ExtensionProviderSupport.feature(
            300, "Reaction removal", List.of(MESSAGE_TAGS), List.of()),
        Ircv3ExtensionProviderSupport.feature(600, "Typing", List.of(MESSAGE_TAGS), List.of()));
  }
}

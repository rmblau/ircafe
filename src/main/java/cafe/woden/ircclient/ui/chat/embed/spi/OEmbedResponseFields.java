package cafe.woden.ircclient.ui.chat.embed.spi;

import org.jmolecules.architecture.layered.InterfaceLayer;

/** Normalized subset of an oEmbed JSON response exposed to provider plugins. */
@InterfaceLayer
public record OEmbedResponseFields(
    String title, String authorName, String providerName, String thumbnailUrl, String html) {}

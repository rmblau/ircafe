package cafe.woden.ircclient.ui.chat.embed.spi;

/** Normalized subset of an oEmbed JSON response exposed to provider plugins. */
public record OEmbedResponseFields(
    String title, String authorName, String providerName, String thumbnailUrl, String html) {}

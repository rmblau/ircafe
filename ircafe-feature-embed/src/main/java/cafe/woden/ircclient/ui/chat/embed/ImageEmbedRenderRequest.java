package cafe.woden.ircclient.ui.chat.embed;

/**
 * Feature-safe image embed render request assembled before the root Swing component is created.
 */
public record ImageEmbedRenderRequest(
    String serverId, String url, boolean collapsedByDefault, long sequence, boolean gifUrlHint) {}

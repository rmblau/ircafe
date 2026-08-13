package cafe.woden.ircclient.ui.chat.embed;

/**
 * Feature-safe link-preview render request assembled before the root Swing component is created.
 */
public record LinkPreviewRenderRequest(
    String serverId,
    String url,
    boolean collapsedByDefault,
    int imageEmbedsMaxWidthPx,
    int imageEmbedsMaxHeightPx) {}

package cafe.woden.ircclient.ui.chat.embed.spi;

import org.jmolecules.architecture.layered.InterfaceLayer;

@InterfaceLayer
public record LinkPreview(
    String url,
    String title,
    String description,
    String siteName,
    String imageUrl,
    /** Number of media attachments represented by {@code imageUrl}. */
    int mediaCount) {}

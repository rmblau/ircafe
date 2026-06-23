package cafe.woden.ircclient.ui.chat.embed.spi;

public record LinkPreviewHttpResponse<T>(int statusCode, LinkPreviewHttpHeaders headers, T body) {}

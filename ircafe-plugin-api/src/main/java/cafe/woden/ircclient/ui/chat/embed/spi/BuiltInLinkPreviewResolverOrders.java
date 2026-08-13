package cafe.woden.ircclient.ui.chat.embed.spi;

public final class BuiltInLinkPreviewResolverOrders {
  public static final int WIKIPEDIA = 100;
  public static final int YOUTUBE = 200;
  public static final int SLASHDOT = 300;
  public static final int IMDB = 400;
  public static final int ROTTEN_TOMATOES = 500;
  public static final int X = 600;
  public static final int INSTAGRAM = 700;
  public static final int IMGUR = 800;
  public static final int GITHUB = 900;
  public static final int REDDIT = 1000;
  public static final int MASTODON_STATUS_API = 1100;
  public static final int OEMBED = 1200;
  public static final int NEWS = 1300;
  public static final int OPEN_GRAPH = 1400;
  public static final int PLUGIN_DEFAULT = 10_000;

  private BuiltInLinkPreviewResolverOrders() {}
}

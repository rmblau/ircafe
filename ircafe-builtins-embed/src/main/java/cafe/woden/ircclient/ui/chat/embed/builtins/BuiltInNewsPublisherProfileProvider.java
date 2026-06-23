package cafe.woden.ircclient.ui.chat.embed.builtins;

import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfile;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider;
import com.google.auto.service.AutoService;
import java.util.List;

/** Built-in publisher profiles used by the generic news/article preview resolver. */
@AutoService(NewsPublisherProfileProvider.class)
public final class BuiltInNewsPublisherProfileProvider implements NewsPublisherProfileProvider {
  private static final String[] GENERIC_IMAGE_SELECTORS = {
    "meta[property='og:image']",
    "meta[property='og:image:secure_url']",
    "meta[name='twitter:image']",
    "meta[name='twitter:image:src']",
    "article img[src]",
    "main img[src]"
  };

  private static final List<NewsPublisherProfile> PROFILES =
      List.of(
          new NewsPublisherProfile(
              "abc",
              "ABC News",
              new String[] {"abcnews.com"},
              new String[] {"article p", "main article p", "section article p"},
              new String[] {"[data-testid='byline']", "[class*='Byline']", "[class*='byline']"},
              GENERIC_IMAGE_SELECTORS,
              new String[] {"author", "article:author", "parsely-author"},
              new String[] {"article:published_time", "parsely-pub-date", "date"}),
          new NewsPublisherProfile(
              "reuters",
              "Reuters",
              new String[] {"reuters.com"},
              new String[] {
                "div[data-testid='Body'] p",
                "article[data-testid='Body'] p",
                "article[data-testid='ArticleBody'] p",
                "article p",
                "main article p"
              },
              new String[] {
                "[data-testid='AuthorName']",
                "a[data-testid='AuthorName']",
                "[class*='author-name']",
                "[class*='Byline']"
              },
              GENERIC_IMAGE_SELECTORS,
              new String[] {"author", "article:author", "parsely-author"},
              new String[] {"article:published_time", "parsely-pub-date", "date"}),
          new NewsPublisherProfile(
              "ap",
              "AP News",
              new String[] {"apnews.com"},
              new String[] {
                "div.RichTextStoryBody p",
                "article p",
                "main article p",
                "div[data-key='article'] p"
              },
              new String[] {"[class*='byline']", "[class*='Author']", "[data-key='byline']"},
              GENERIC_IMAGE_SELECTORS,
              new String[] {"author", "article:author", "parsely-author"},
              new String[] {"article:published_time", "parsely-pub-date", "date"}),
          new NewsPublisherProfile(
              "nyt",
              "New York Times",
              new String[] {"nytimes.com"},
              new String[] {"section[name='articleBody'] p", "article section p", "article p"},
              new String[] {"[data-testid='byline']", "span[itemprop='name']", "[class*='byline']"},
              GENERIC_IMAGE_SELECTORS,
              new String[] {"byl", "author", "article:author", "parsely-author"},
              new String[] {"article:published_time", "ptime", "parsely-pub-date", "date"}),
          new NewsPublisherProfile(
              "bbc",
              "BBC",
              new String[] {"bbc.com", "bbc.co.uk"},
              new String[] {"article [data-component='text-block'] p", "article p", "main p"},
              new String[] {
                "[data-component='byline-block'] a",
                "[data-component='byline-block'] span",
                "[class*='byline']"
              },
              GENERIC_IMAGE_SELECTORS,
              new String[] {"byl", "author", "article:author"},
              new String[] {"article:published_time", "article:modified_time", "date"}),
          new NewsPublisherProfile(
              "cnn",
              "CNN",
              new String[] {"cnn.com"},
              new String[] {
                "div.article__content p",
                "div.article__main p",
                "article p",
                "[data-component-name='paragraph']"
              },
              new String[] {
                "[class*='byline']", "[data-editable='byline']", "[class*='metadata__byline']"
              },
              GENERIC_IMAGE_SELECTORS,
              new String[] {"author", "article:author", "parsely-author"},
              new String[] {
                "article:published_time", "og:updated_time", "parsely-pub-date", "date"
              }),
          new NewsPublisherProfile(
              "wapo",
              "Washington Post",
              new String[] {"washingtonpost.com"},
              new String[] {
                "div[data-qa='article-body'] p",
                "article div[data-qa='article-body'] p",
                "article p"
              },
              new String[] {"[data-qa='author-name']", "[data-qa='byline']", "[class*='byline']"},
              GENERIC_IMAGE_SELECTORS,
              new String[] {"author", "article:author", "parsely-author"},
              new String[] {"article:published_time", "parsely-pub-date", "date"}),
          new NewsPublisherProfile(
              "guardian",
              "The Guardian",
              new String[] {"theguardian.com", "guardian.co.uk"},
              new String[] {
                "div[data-gu-name='body'] p",
                "article div[data-gu-name='body'] p",
                "article div[class*='article-body'] p",
                "article p"
              },
              new String[] {"a[rel='author']", "[class*='byline']"},
              GENERIC_IMAGE_SELECTORS,
              new String[] {"author", "article:author", "parsely-author"},
              new String[] {"article:published_time", "parsely-pub-date", "date"}),
          new NewsPublisherProfile(
              "npr",
              "NPR",
              new String[] {"npr.org"},
              new String[] {
                "div.storytext p",
                "article div.storytext p",
                "article [id*='storytext'] p",
                "article p",
                "main p"
              },
              new String[] {"[class*='byline']", "[itemprop='author']", "a[rel='author']"},
              GENERIC_IMAGE_SELECTORS,
              new String[] {"author", "article:author", "dc.creator", "parsely-author"},
              new String[] {"article:published_time", "parsely-pub-date", "date", "dc.date"}),
          new NewsPublisherProfile(
              "wsj",
              "Wall Street Journal",
              new String[] {"wsj.com"},
              new String[] {
                "div[data-module='ArticleBody'] p",
                "article [data-module='ArticleBody'] p",
                "article [class*='article-content'] p",
                "article p",
                "[itemprop='articleBody'] p"
              },
              new String[] {"[class*='author-name']", "a[rel='author']", "[class*='byline']"},
              GENERIC_IMAGE_SELECTORS,
              new String[] {"author", "article:author", "parsely-author"},
              new String[] {
                "article:published_time", "article:modified_time", "parsely-pub-date", "date"
              }));

  public static List<NewsPublisherProfile> profiles() {
    return PROFILES;
  }

  @Override
  public List<NewsPublisherProfile> publisherProfiles() {
    return PROFILES;
  }
}

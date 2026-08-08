package cafe.woden.ircclient.ui.chat.embed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

class LinkPreviewFeatureWiringTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner().withUserConfiguration(FeatureRuntimeConfiguration.class);

  @Test
  void wiresFeatureRuntimeServices() {
    runner.run(
        context -> {
          assertThat(context).hasSingleBean(LinkPreviewProviderCatalog.class);
          assertThat(context).hasSingleBean(LinkPreviewFetchPreflightService.class);
          assertThat(context).hasSingleBean(LinkPreviewFetchPlanningService.class);
          assertThat(context).hasSingleBean(LinkPreviewResolutionService.class);
          assertThat(context).hasSingleBean(LinkPreviewUrlExtractionService.class);
          assertThat(context).hasSingleBean(LinkPreviewHttpHeaderCatalog.class);
          assertThat(context).hasSingleBean(LinkPreviewHttpAdapterHeaders.class);
          assertThat(context).hasSingleBean(ImageFetchPlanningService.class);
          assertThat(context).hasSingleBean(ImageFetchHttpHeaders.class);
          assertThat(context).hasSingleBean(ImageFetchDownloadPolicy.class);
          assertThat(context).hasSingleBean(ImageFetchResponseReader.class);
          assertThat(context).hasSingleBean(ImageFetchResponsePolicy.class);
          assertThat(context).hasSingleBean(EmbedLoadPolicyDecisionService.class);
          assertThat(context).hasSingleBean(EmbedLoadPolicyTagFactsParser.class);
          assertThat(context).hasSingleBean(EmbedRenderRequestService.class);
          assertThat(context).hasSingleBean(WikipediaLinkPreviewResolver.class);
          assertThat(context).hasSingleBean(YouTubeLinkPreviewResolver.class);
          assertThat(context).hasSingleBean(OpenGraphLinkPreviewResolver.class);
          assertThat(context).hasSingleBean(XLinkPreviewResolver.class);
          assertThat(context).hasSingleBean(ImgurLinkPreviewResolver.class);
          assertThat(context).hasSingleBean(GitHubLinkPreviewResolver.class);
          assertThat(context).hasSingleBean(InstagramLinkPreviewResolver.class);
          assertThat(context).hasSingleBean(NewsLinkPreviewResolver.class);
          assertThat(context).hasSingleBean(SlashdotLinkPreviewResolver.class);
          assertThat(context).hasSingleBean(ImdbLinkPreviewResolver.class);
          assertThat(context).hasSingleBean(RottenTomatoesLinkPreviewResolver.class);
          assertThat(context).hasSingleBean(RedditLinkPreviewResolver.class);
          assertThat(context).hasSingleBean(MastodonStatusApiPreviewResolver.class);
        });
  }

  @Configuration(proxyBeanMethods = false)
  @Import({
    LinkPreviewProviderCatalog.class,
    LinkPreviewFetchPreflightService.class,
    LinkPreviewFetchPlanningService.class,
    LinkPreviewResolutionService.class,
    LinkPreviewUrlExtractionService.class,
    LinkPreviewHttpHeaderCatalog.class,
    LinkPreviewHttpAdapterHeaders.class,
    ImageFetchPlanningService.class,
    ImageFetchHttpHeaders.class,
    ImageFetchDownloadPolicy.class,
    ImageFetchResponseReader.class,
    ImageFetchResponsePolicy.class,
    EmbedLoadPolicyDecisionService.class,
    EmbedLoadPolicyTagFactsParser.class,
    EmbedRenderRequestService.class
  })
  static class FeatureRuntimeConfiguration {
    @Bean
    WikipediaLinkPreviewResolver wikipediaLinkPreviewResolver() {
      return new WikipediaLinkPreviewResolver();
    }

    @Bean
    YouTubeLinkPreviewResolver youTubeLinkPreviewResolver() {
      return new YouTubeLinkPreviewResolver();
    }

    @Bean
    OpenGraphLinkPreviewResolver openGraphLinkPreviewResolver() {
      return new OpenGraphLinkPreviewResolver(1024 * 1024);
    }

    @Bean
    XLinkPreviewResolver xLinkPreviewResolver() {
      return new XLinkPreviewResolver(1024 * 1024);
    }

    @Bean
    ImgurLinkPreviewResolver imgurLinkPreviewResolver() {
      return new ImgurLinkPreviewResolver(1024 * 1024);
    }

    @Bean
    GitHubLinkPreviewResolver gitHubLinkPreviewResolver() {
      return new GitHubLinkPreviewResolver();
    }

    @Bean
    InstagramLinkPreviewResolver instagramLinkPreviewResolver() {
      return new InstagramLinkPreviewResolver(1024 * 1024);
    }

    @Bean
    NewsLinkPreviewResolver newsLinkPreviewResolver() {
      return new NewsLinkPreviewResolver(1024 * 1024);
    }

    @Bean
    SlashdotLinkPreviewResolver slashdotLinkPreviewResolver() {
      return new SlashdotLinkPreviewResolver(1024 * 1024);
    }

    @Bean
    ImdbLinkPreviewResolver imdbLinkPreviewResolver() {
      return new ImdbLinkPreviewResolver();
    }

    @Bean
    RottenTomatoesLinkPreviewResolver rottenTomatoesLinkPreviewResolver() {
      return new RottenTomatoesLinkPreviewResolver();
    }

    @Bean
    RedditLinkPreviewResolver redditLinkPreviewResolver() {
      return new RedditLinkPreviewResolver();
    }

    @Bean
    MastodonStatusApiPreviewResolver mastodonStatusApiPreviewResolver() {
      return new MastodonStatusApiPreviewResolver();
    }
  }
}

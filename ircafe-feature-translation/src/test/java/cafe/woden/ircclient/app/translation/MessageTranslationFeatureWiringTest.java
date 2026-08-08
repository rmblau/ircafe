package cafe.woden.ircclient.app.translation;

import static org.assertj.core.api.Assertions.assertThat;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

class MessageTranslationFeatureWiringTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withUserConfiguration(FeatureRuntimeConfiguration.class, TestDependencies.class);

  @Test
  void wiresFeatureRuntimeServices() {
    runner.run(
        context -> {
          assertThat(context).hasSingleBean(MessageTranslationPreflightService.class);
          assertThat(context).hasSingleBean(MessageTranslationExecutionService.class);
          assertThat(context).hasSingleBean(MessageTranslationDispatchPlanningService.class);
          assertThat(context).hasSingleBean(MessageTranslationBackendRegistry.class);
        });
  }

  @Configuration(proxyBeanMethods = false)
  @Import({
    MessageTranslationPreflightService.class,
    MessageTranslationExecutionService.class,
    MessageTranslationDispatchPlanningService.class
  })
  static class FeatureRuntimeConfiguration {}

  @Configuration(proxyBeanMethods = false)
  static class TestDependencies {
    @Bean
    MessageLanguageDetector messageLanguageDetector() {
      return new MessageLanguageDetector() {
        @Override
        public Optional<String> detectLanguageCode(String text) {
          return Optional.of("en");
        }

        @Override
        public Optional<String> detectLanguageCode(String text, Collection<String> languageCodes) {
          return Optional.of("en");
        }
      };
    }

    @Bean
    MessageTranslationBackendRegistry messageTranslationBackendRegistry() {
      return new MessageTranslationBackendRegistry(List.of(new TestBackend()));
    }
  }

  private static final class TestBackend implements MessageTranslationBackendProvider {
    @Override
    public String backendId() {
      return "test";
    }

    @Override
    public CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request) {
      return CompletableFuture.completedFuture(
          new MessageTranslationResult("hola", "en", "es", "test"));
    }
  }
}

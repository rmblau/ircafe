package cafe.woden.ircclient.translation;

import static org.assertj.core.api.Assertions.assertThat;

import cafe.woden.ircclient.app.translation.MessageTranslationSettingsProvider;
import cafe.woden.ircclient.app.translation.MessageTranslationSettingsSnapshot;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

class LibreTranslateMessageTranslationBackendWiringTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withUserConfiguration(LibreTranslateBackendConfiguration.class, TestDependencies.class);

  @Test
  void wiresLibreTranslateBackendAsTranslationProvider() {
    runner.run(
        context -> {
          assertThat(context).hasSingleBean(LibreTranslateMessageTranslationBackend.class);
          assertThat(context).hasSingleBean(MessageTranslationBackendProvider.class);
          assertThat(context.getBean(MessageTranslationBackendProvider.class).backendId())
              .isEqualTo(LibreTranslateMessageTranslationBackend.BACKEND_ID);
        });
  }

  @Configuration(proxyBeanMethods = false)
  @Import(LibreTranslateMessageTranslationBackend.class)
  static class LibreTranslateBackendConfiguration {}

  @Configuration(proxyBeanMethods = false)
  static class TestDependencies {
    @Bean
    MessageTranslationSettingsProvider messageTranslationSettingsProvider() {
      return MessageTranslationSettingsSnapshot::defaults;
    }

    @Bean
    MessageTranslationHttpClient messageTranslationHttpClient() {
      return new MessageTranslationHttpClient() {
        @Override
        public MessageTranslationHttpResponse getString(
            URI endpoint, Map<String, String> headers, long timeoutMs) throws IOException {
          throw new IOException("HTTP should not be called by wiring tests.");
        }

        @Override
        public MessageTranslationHttpResponse postString(
            URI endpoint, Map<String, String> headers, String body, long timeoutMs)
            throws IOException {
          throw new IOException("HTTP should not be called by wiring tests.");
        }
      };
    }
  }
}

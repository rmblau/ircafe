package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.properties.IgnoreProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

class IgnorePropertiesBindingTest {

  @TempDir Path tempDir;

  @Test
  void legacyWildcardMaskMetadataKeysAreSanitizedBeforeRuntimeYamlBinding() throws Exception {
    Path configPath = tempDir.resolve("ircafe.yml");
    Files.writeString(
        configPath,
        """
        ircafe:
          ignore:
            servers:
              libera:
                masks:
                 - '*!*@*'
                maskPatterns:
                  '*!*@*': trump
                maskPatternModes:
                  '*!*@*': regexp
                maskReplies:
                  '*!*@*': true
        """);

    try (ConfigurableApplicationContext context =
        new SpringApplicationBuilder(BindingConfig.class)
            .properties(
                "spring.main.banner-mode=off",
                "spring.main.web-application-type=none",
                "ircafe.runtime-config=" + configPath,
                "spring.config.import=optional:" + configPath.toUri())
            .run()) {
      IgnoreProperties props = context.getBean(IgnoreProperties.class);
      IgnoreProperties.ServerIgnore server = props.servers().get("libera");

      assertEquals(List.of("*!*@*"), server.masks());
      assertEquals("trump", server.maskPatterns().get("*!*@*"));
      assertEquals("regexp", server.maskPatternModes().get("*!*@*"));
      assertTrue(server.maskReplies().get("*!*@*"));
    }

    assertTrue(Files.readString(configPath).contains("[*!*@*]"));
  }

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(IgnoreProperties.class)
  private static class BindingConfig {}
}

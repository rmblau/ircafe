package cafe.woden.ircclient.config.runtime.ignore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

class RuntimeConfigIgnoreLegacyMapKeySanitizerTest {

  @TempDir Path tempDir;

  @Test
  void skipsUnresolvedRuntimeConfigPlaceholders() {
    StandardEnvironment environment = new StandardEnvironment();
    environment
        .getPropertySources()
        .addFirst(
            new MapPropertySource(
                "test", Map.of("ircafe.runtime-config", "build/tmp/${random.uuid}/ircafe.yml")));

    assertThatCode(
            () ->
                new RuntimeConfigIgnoreLegacyMapKeySanitizer()
                    .postProcessEnvironment(environment, null))
        .doesNotThrowAnyException();
  }

  @Test
  void sanitizesLegacyIgnoreMaskMapKeys() throws Exception {
    Path config = tempDir.resolve("ircafe.yml");
    Files.writeString(
        config,
        "ircafe:\n"
            + "  ignore:\n"
            + "    servers:\n"
            + "      libera:\n"
            + "        masks:\n"
            + "          - '*!*@*'\n"
            + "        maskPatterns:\n"
            + "          '*!*@*': trump\n");
    StandardEnvironment environment = new StandardEnvironment();
    environment
        .getPropertySources()
        .addFirst(
            new MapPropertySource("test", Map.of("ircafe.runtime-config", config.toString())));

    new RuntimeConfigIgnoreLegacyMapKeySanitizer().postProcessEnvironment(environment, null);

    String sanitized = Files.readString(config);
    assertThat(sanitized).contains("'[*!*@*]': trump");
    assertThat(sanitized).doesNotContain("'*!*@*': trump");
  }
}

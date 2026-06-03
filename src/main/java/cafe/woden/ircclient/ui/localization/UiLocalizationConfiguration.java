package cafe.woden.ircclient.ui.localization;

import java.nio.charset.StandardCharsets;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration(proxyBeanMethods = false)
@InterfaceLayer
public class UiLocalizationConfiguration {
  public static final String UI_MESSAGE_SOURCE_BEAN = "uiMessageSource";
  public static final String UI_MESSAGE_BASENAME = "i18n/ui";

  @Bean(name = UI_MESSAGE_SOURCE_BEAN)
  MessageSource uiMessageSource() {
    return createMessageSource();
  }

  public static ResourceBundleMessageSource createMessageSource() {
    ResourceBundleMessageSource source = new ResourceBundleMessageSource();
    source.setBasename(UI_MESSAGE_BASENAME);
    source.setDefaultEncoding(StandardCharsets.UTF_8.name());
    source.setFallbackToSystemLocale(false);
    return source;
  }
}

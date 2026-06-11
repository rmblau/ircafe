package cafe.woden.ircclient.ui.localization;

import java.util.Locale;
import java.util.Objects;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@InterfaceLayer
public class UiMessages {
  private final MessageSource messageSource;

  public UiMessages(
      @Qualifier(UiLocalizationConfiguration.UI_MESSAGE_SOURCE_BEAN) MessageSource messageSource) {
    this.messageSource = Objects.requireNonNull(messageSource, "messageSource");
  }

  public static UiMessages bundledDefaults() {
    return new UiMessages(UiLocalizationConfiguration.createMessageSource());
  }

  public String text(String code, Object... args) {
    try {
      return messageSource.getMessage(code, args, currentLocale());
    } catch (NoSuchMessageException ignored) {
      return code;
    }
  }

  public String textOrDefault(String code, String defaultMessage, Object... args) {
    return messageSource.getMessage(code, args, defaultMessage, currentLocale());
  }

  private static Locale currentLocale() {
    Locale locale = LocaleContextHolder.getLocale();
    return locale == null ? Locale.getDefault() : locale;
  }
}

package cafe.woden.ircclient.ui.settings.translation;

import cafe.woden.ircclient.app.translation.MessageTranslationLanguage;
import cafe.woden.ircclient.config.IrcProperties;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;

public record TranslationControls(
    JCheckBox enabled,
    JComboBox<IrcProperties.Client.Translation.Mode> mode,
    JComboBox<TranslationServiceChoice> backend,
    JTextField endpoint,
    JPasswordField apiKey,
    JComboBox<TranslationLanguageChoice> sourceLanguage,
    JComboBox<TranslationLanguageChoice> targetLanguage,
    JCheckBox translateUnknownMessages,
    JCheckBox detectAllLanguages,
    JList<MessageTranslationLanguage> disabledDetectionLanguages,
    JList<MessageTranslationLanguage> enabledDetectionLanguages,
    JSpinner requestTimeoutSeconds,
    JSpinner maxRequestChars,
    JSpinner maxConcurrentRequests,
    JPanel panel) {}

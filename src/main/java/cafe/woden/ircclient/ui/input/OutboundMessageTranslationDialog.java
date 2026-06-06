package cafe.woden.ircclient.ui.input;

import cafe.woden.ircclient.app.translation.MessageTranslationLanguage;
import cafe.woden.ircclient.app.translation.MessageTranslationResult;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.translation.TranslationLanguageChoice;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Modal dialog for translating the current outbound draft before sending. */
public final class OutboundMessageTranslationDialog {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private OutboundMessageTranslationDialog() {}

  public static void showDialog(
      Component owner,
      String originalText,
      List<MessageTranslationLanguage> targetLanguages,
      String initialTargetLanguage,
      Function<String, CompletionStage<MessageTranslationResult>> translator,
      Consumer<String> onAccepted) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(
          () ->
              showDialog(
                  owner,
                  originalText,
                  targetLanguages,
                  initialTargetLanguage,
                  translator,
                  onAccepted));
      return;
    }

    List<MessageTranslationLanguage> languages = normalizeLanguages(targetLanguages);
    if (languages.isEmpty()) {
      JOptionPane.showMessageDialog(
          owner,
          MESSAGES.text("messageInput.translation.noTargetLanguages"),
          MESSAGES.text("messageInput.translation.title"),
          JOptionPane.WARNING_MESSAGE);
      return;
    }

    Window window = owner == null ? null : SwingUtilities.getWindowAncestor(owner);
    JDialog dialog =
        new JDialog(
            window,
            MESSAGES.text("messageInput.translation.title"),
            Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setName("outboundMessageTranslationDialog");

    JTextArea original = textArea(Objects.toString(originalText, ""), false, 5);
    original.setName("outboundOriginalDraft");
    JTextArea translated = textArea("", true, 7);
    translated.setName("outboundTranslatedDraft");

    JComboBox<TranslationLanguageChoice> targetLanguage = new JComboBox<>(languageModel(languages));
    targetLanguage.setName("outboundTranslationTargetLanguage");
    selectLanguage(targetLanguage, initialTargetLanguage);

    JLabel status = new JLabel(" ");
    status.setName("outboundTranslationStatus");

    JButton translate = new JButton(MESSAGES.text("messageInput.translation.translate"));
    translate.setName("outboundTranslateButton");
    JButton ok = new JButton(MESSAGES.text("common.button.ok"));
    ok.setName("outboundTranslationOkButton");
    ok.setEnabled(false);
    JButton cancel = new JButton(MESSAGES.text("common.button.cancel"));
    cancel.setName("outboundTranslationCancelButton");

    translated
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                refreshOk();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                refreshOk();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                refreshOk();
              }

              private void refreshOk() {
                ok.setEnabled(!translated.getText().isBlank());
              }
            });

    translate.addActionListener(
        e -> {
          TranslationLanguageChoice selected =
              targetLanguage.getSelectedItem() instanceof TranslationLanguageChoice choice
                  ? choice
                  : null;
          String language = selected == null ? "" : selected.code();
          if (language.isBlank()) {
            status.setText(
                MESSAGES.text("messageInput.translation.validation.chooseTargetLanguage"));
            return;
          }

          setBusy(true, targetLanguage, translate, ok, cancel);
          status.setText(MESSAGES.text("messageInput.translation.status.translating"));
          CompletionStage<MessageTranslationResult> stage;
          try {
            stage = translator == null ? null : translator.apply(language);
          } catch (Exception ex) {
            showFailure(status, targetLanguage, translate, ok, cancel, ex);
            return;
          }
          if (stage == null) {
            showFailure(
                status,
                targetLanguage,
                translate,
                ok,
                cancel,
                new IllegalStateException(
                    MESSAGES.text("messageInput.translation.error.noResult")));
            return;
          }

          stage.whenComplete(
              (result, error) ->
                  SwingUtilities.invokeLater(
                      () -> {
                        if (error != null) {
                          showFailure(status, targetLanguage, translate, ok, cancel, error);
                          return;
                        }
                        translated.setText(result == null ? "" : result.translatedText());
                        translated.setCaretPosition(0);
                        status.setText(" ");
                        setBusy(false, targetLanguage, translate, ok, cancel);
                      }));
        });

    ok.addActionListener(
        e -> {
          String replacement = translated.getText();
          if (replacement.isBlank()) return;
          if (onAccepted != null) {
            onAccepted.accept(replacement);
          }
          dialog.dispose();
        });
    cancel.addActionListener(e -> dialog.dispose());

    JPanel languageRow = new JPanel(new BorderLayout(8, 0));
    languageRow.add(
        new JLabel(MESSAGES.text("messageInput.translation.targetLanguage")), BorderLayout.WEST);
    languageRow.add(targetLanguage, BorderLayout.CENTER);

    JPanel form = new JPanel(new GridBagLayout());
    form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 8, 0);
    form.add(languageRow, c);

    c.gridy++;
    form.add(new JLabel(MESSAGES.text("messageInput.translation.originalMessage")), c);
    c.gridy++;
    c.fill = GridBagConstraints.BOTH;
    c.weighty = 0.4;
    form.add(new JScrollPane(original), c);

    c.gridy++;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weighty = 0.0;
    c.insets = new Insets(10, 0, 8, 0);
    form.add(new JLabel(MESSAGES.text("messageInput.translation.translatedMessage")), c);
    c.gridy++;
    c.fill = GridBagConstraints.BOTH;
    c.weighty = 0.6;
    c.insets = new Insets(0, 0, 8, 0);
    form.add(new JScrollPane(translated), c);

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    buttons.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
    buttons.add(status);
    buttons.add(translate);
    buttons.add(ok);
    buttons.add(cancel);

    JPanel root = new JPanel(new BorderLayout());
    root.add(form, BorderLayout.CENTER);
    root.add(buttons, BorderLayout.SOUTH);
    root.setPreferredSize(new Dimension(560, 430));

    dialog.setContentPane(root);
    dialog.getRootPane().setDefaultButton(translate);
    dialog.pack();
    dialog.setLocationRelativeTo(owner);
    dialog.setVisible(true);
  }

  private static JTextArea textArea(String text, boolean editable, int rows) {
    JTextArea area = new JTextArea(text, rows, 48);
    area.setEditable(editable);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setCaretPosition(0);
    return area;
  }

  private static DefaultComboBoxModel<TranslationLanguageChoice> languageModel(
      List<MessageTranslationLanguage> languages) {
    DefaultComboBoxModel<TranslationLanguageChoice> model = new DefaultComboBoxModel<>();
    for (MessageTranslationLanguage language : languages) {
      model.addElement(TranslationLanguageChoice.from(language));
    }
    return model;
  }

  private static List<MessageTranslationLanguage> normalizeLanguages(
      List<MessageTranslationLanguage> languages) {
    return (languages == null ? List.<MessageTranslationLanguage>of() : languages)
        .stream()
            .filter(Objects::nonNull)
            .filter(language -> !language.code().isBlank())
            .sorted(Comparator.comparing(MessageTranslationLanguage::label))
            .toList();
  }

  private static void selectLanguage(
      JComboBox<TranslationLanguageChoice> combo, String languageCode) {
    String normalized =
        Objects.toString(languageCode, "").trim().toLowerCase(java.util.Locale.ROOT);
    for (int i = 0; i < combo.getItemCount(); i++) {
      TranslationLanguageChoice item = combo.getItemAt(i);
      if (item.code().equals(normalized)) {
        combo.setSelectedIndex(i);
        return;
      }
    }
    combo.setSelectedIndex(0);
  }

  private static void setBusy(
      boolean busy,
      JComboBox<TranslationLanguageChoice> language,
      JButton translate,
      JButton ok,
      JButton cancel) {
    language.setEnabled(!busy);
    translate.setEnabled(!busy);
    cancel.setEnabled(!busy);
    if (busy) {
      ok.setEnabled(false);
    }
  }

  private static void showFailure(
      JLabel status,
      JComboBox<TranslationLanguageChoice> language,
      JButton translate,
      JButton ok,
      JButton cancel,
      Throwable error) {
    status.setText(
        MESSAGES.text("messageInput.translation.status.failed", describe(error)));
    setBusy(false, language, translate, ok, cancel);
  }

  private static String describe(Throwable error) {
    Throwable current = error;
    while (current instanceof CompletionException || current instanceof ExecutionException) {
      if (current.getCause() == null) break;
      current = current.getCause();
    }
    String message = current == null ? "" : Objects.toString(current.getMessage(), "").trim();
    return message.isBlank() ? MESSAGES.text("messageInput.translation.error.unknown") : message;
  }
}

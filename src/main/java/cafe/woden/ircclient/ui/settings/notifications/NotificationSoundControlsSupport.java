package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.notify.api.NotificationSoundPort;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsDocumentListener;
import java.awt.Component;
import java.io.File;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class NotificationSoundControlsSupport {
  private NotificationSoundControlsSupport() {}

  public static Controls buildControls(Request request) {
    JCheckBox enabled = new JCheckBox(request.enabledLabel(), request.enabledSelected());
    JCheckBox useCustom = new JCheckBox(request.useCustomLabel(), request.useCustomSelected());
    JTextField customPath = new JTextField(Objects.toString(request.customPath(), ""));

    JComboBox<BuiltInSound> builtInSound = new JComboBox<>(BuiltInSound.valuesForUi());
    PreferencesUiSupport.configureBuiltInSoundCombo(builtInSound);
    builtInSound.setSelectedItem(BuiltInSound.fromId(request.soundId()));

    JButton browseCustom = new JButton(request.browseButtonText());
    JButton clearCustom = new JButton(request.clearButtonText());
    JButton testSound = new JButton(request.testButtonText());
    if (request.buttonStyle() == ButtonStyle.ICON_ONLY) {
      PreferencesUiSupport.configureIconOnlyButton(
          browseCustom, "folder-open", "Browse/import custom sound file");
      PreferencesUiSupport.configureIconOnlyButton(clearCustom, "close", "Clear custom sound path");
      PreferencesUiSupport.configureIconOnlyButton(testSound, "play", "Test selected sound");
    } else {
      browseCustom.setToolTipText(
          "Choose an MP3 or WAV file and copy it into IRCafe's runtime config directory.");
      clearCustom.setToolTipText("Stop using a custom file and revert to bundled sounds.");
      testSound.setToolTipText("Play the selected sound.");
    }

    Controls controls =
        new Controls(
            enabled,
            useCustom,
            customPath,
            builtInSound,
            browseCustom,
            clearCustom,
            testSound,
            request.owner(),
            request.notificationSoundService(),
            request.soundFileImporter(),
            request.availableSupplier(),
            request.customPathEditableWhenEnabled(),
            request.customFileControlsRequireUseCustom());

    enabled.addActionListener(e -> controls.refresh());
    useCustom.addActionListener(e -> controls.refresh());
    customPath.getDocument().addDocumentListener(new SettingsDocumentListener(controls::refresh));

    browseCustom.addActionListener(e -> controls.browseCustomSound());
    clearCustom.addActionListener(e -> controls.clearCustomSound());
    testSound.addActionListener(e -> controls.previewSound());
    controls.refresh();
    return controls;
  }

  public enum ButtonStyle {
    TEXT,
    ICON_ONLY
  }

  public record Request(
      String enabledLabel,
      boolean enabledSelected,
      String useCustomLabel,
      boolean useCustomSelected,
      String soundId,
      String customPath,
      String browseButtonText,
      String clearButtonText,
      String testButtonText,
      ButtonStyle buttonStyle,
      Component owner,
      NotificationSoundPort notificationSoundService,
      SoundFileImporter soundFileImporter,
      BooleanSupplier availableSupplier,
      boolean customPathEditableWhenEnabled,
      boolean customFileControlsRequireUseCustom) {
    public Request {
      enabledLabel = Objects.toString(enabledLabel, "Play sound");
      useCustomLabel = Objects.toString(useCustomLabel, "Use custom file");
      browseButtonText = Objects.toString(browseButtonText, "Browse...");
      clearButtonText = Objects.toString(clearButtonText, "Clear");
      testButtonText = Objects.toString(testButtonText, "Test sound");
      if (buttonStyle == null) buttonStyle = ButtonStyle.TEXT;
      if (availableSupplier == null) availableSupplier = () -> true;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static final class Builder {
      private String enabledLabel;
      private boolean enabledSelected;
      private String useCustomLabel;
      private boolean useCustomSelected;
      private String soundId;
      private String customPath;
      private String browseButtonText;
      private String clearButtonText;
      private String testButtonText;
      private ButtonStyle buttonStyle;
      private Component owner;
      private NotificationSoundPort notificationSoundService;
      private SoundFileImporter soundFileImporter;
      private BooleanSupplier availableSupplier;
      private boolean customPathEditableWhenEnabled;
      private boolean customFileControlsRequireUseCustom;

      private Builder() {}

      public Builder enabledLabel(String enabledLabel) {
        this.enabledLabel = enabledLabel;
        return this;
      }

      public Builder enabledSelected(boolean enabledSelected) {
        this.enabledSelected = enabledSelected;
        return this;
      }

      public Builder useCustomLabel(String useCustomLabel) {
        this.useCustomLabel = useCustomLabel;
        return this;
      }

      public Builder useCustomSelected(boolean useCustomSelected) {
        this.useCustomSelected = useCustomSelected;
        return this;
      }

      public Builder soundId(String soundId) {
        this.soundId = soundId;
        return this;
      }

      public Builder customPath(String customPath) {
        this.customPath = customPath;
        return this;
      }

      public Builder browseButtonText(String browseButtonText) {
        this.browseButtonText = browseButtonText;
        return this;
      }

      public Builder clearButtonText(String clearButtonText) {
        this.clearButtonText = clearButtonText;
        return this;
      }

      public Builder testButtonText(String testButtonText) {
        this.testButtonText = testButtonText;
        return this;
      }

      public Builder buttonStyle(ButtonStyle buttonStyle) {
        this.buttonStyle = buttonStyle;
        return this;
      }

      public Builder owner(Component owner) {
        this.owner = owner;
        return this;
      }

      public Builder notificationSoundService(NotificationSoundPort notificationSoundService) {
        this.notificationSoundService = notificationSoundService;
        return this;
      }

      public Builder soundFileImporter(SoundFileImporter soundFileImporter) {
        this.soundFileImporter = soundFileImporter;
        return this;
      }

      public Builder availableSupplier(BooleanSupplier availableSupplier) {
        this.availableSupplier = availableSupplier;
        return this;
      }

      public Builder customPathEditableWhenEnabled(boolean customPathEditableWhenEnabled) {
        this.customPathEditableWhenEnabled = customPathEditableWhenEnabled;
        return this;
      }

      public Builder customFileControlsRequireUseCustom(
          boolean customFileControlsRequireUseCustom) {
        this.customFileControlsRequireUseCustom = customFileControlsRequireUseCustom;
        return this;
      }

      public Request build() {
        return new Request(
            enabledLabel,
            enabledSelected,
            useCustomLabel,
            useCustomSelected,
            soundId,
            customPath,
            browseButtonText,
            clearButtonText,
            testButtonText,
            buttonStyle,
            owner,
            notificationSoundService,
            soundFileImporter,
            availableSupplier,
            customPathEditableWhenEnabled,
            customFileControlsRequireUseCustom);
      }
    }
  }

  public record Controls(
      JCheckBox enabled,
      JCheckBox useCustom,
      JTextField customPath,
      JComboBox<BuiltInSound> builtInSound,
      JButton browseCustom,
      JButton clearCustom,
      JButton testSound,
      Component owner,
      NotificationSoundPort notificationSoundService,
      SoundFileImporter soundFileImporter,
      BooleanSupplier availableSupplier,
      boolean customPathEditableWhenEnabled,
      boolean customFileControlsRequireUseCustom) {
    public void refresh() {
      boolean available = availableSupplier == null || availableSupplier.getAsBoolean();
      enabled.setEnabled(available);

      boolean soundOn = available && enabled.isSelected();
      useCustom.setEnabled(soundOn);

      boolean useCustomSelected = useCustom.isSelected();
      boolean customControlsEnabled =
          soundOn && (!customFileControlsRequireUseCustom || useCustomSelected);
      builtInSound.setEnabled(soundOn && !useCustomSelected);
      customPath.setEnabled(customControlsEnabled);
      customPath.setEditable(customControlsEnabled && customPathEditableWhenEnabled);
      browseCustom.setEnabled(customControlsEnabled);

      String custom = customPathValue();
      clearCustom.setEnabled(customControlsEnabled && !custom.isBlank());
      testSound.setEnabled(soundOn);
    }

    public String customPathValue() {
      return Objects.toString(customPath.getText(), "").trim();
    }

    public BuiltInSound selectedBuiltInSound(BuiltInSound fallback) {
      return builtInSound.getSelectedItem() instanceof BuiltInSound sound ? sound : fallback;
    }

    private void browseCustomSound() {
      try {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose notification sound (MP3 or WAV)");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(
            new FileNameExtensionFilter("Audio files (MP3, WAV)", "mp3", "wav"));
        int result = chooser.showOpenDialog(dialogOwner());
        if (result != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = chooser.getSelectedFile();
        if (selectedFile == null || soundFileImporter == null) return;
        String relativePath = soundFileImporter.importFile(selectedFile);
        if (relativePath != null && !relativePath.isBlank()) {
          customPath.setText(relativePath);
          useCustom.setSelected(true);
          refresh();
        }
      } catch (Exception ex) {
        PreferencesUiSupport.showErrorMessage(
            dialogOwner(), "Could not import sound file.\n\n" + ex.getMessage(), "Import failed");
      }
    }

    private void clearCustomSound() {
      useCustom.setSelected(false);
      customPath.setText("");
      refresh();
    }

    private void previewSound() {
      try {
        if (notificationSoundService == null) return;
        if (useCustom.isSelected()) {
          String relativePath = customPathValue();
          if (!relativePath.isBlank()) {
            notificationSoundService.previewCustom(relativePath);
          }
        } else {
          notificationSoundService.preview(selectedBuiltInSound(null));
        }
      } catch (Throwable ignored) {
      }
    }

    private Component dialogOwner() {
      if (owner != null) return owner;
      return SwingUtilities.getWindowAncestor(browseCustom);
    }
  }

  @FunctionalInterface
  public interface SoundFileImporter {
    String importFile(File source) throws Exception;
  }
}

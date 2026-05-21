package cafe.woden.ircclient.ui.settings;

import java.awt.Component;
import java.io.File;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public final class PathChooserControlsSupport {
  private PathChooserControlsSupport() {}

  public static Controls buildControls(Request request) {
    JTextField path = new JTextField(Objects.toString(request.initialPath(), ""));
    JButton browse = new JButton(request.browseButtonText());
    JButton clear = new JButton(request.clearButtonText());

    configureButton(browse, request.browseIconName(), request.browseTooltip());
    configureButton(clear, request.clearIconName(), request.clearTooltip());

    Controls controls =
        new Controls(
            path,
            browse,
            clear,
            request.chooserDialogTitle(),
            request.selectionMode(),
            request.owner(),
            request.availableSupplier(),
            request.editableWhenAvailable());

    path.getDocument().addDocumentListener(new SettingsDocumentListener(controls::refresh));
    browse.addActionListener(event -> controls.choosePath());
    clear.addActionListener(event -> controls.clearPath());
    controls.refresh();
    return controls;
  }

  private static void configureButton(JButton button, String iconName, String tooltip) {
    if (iconName != null && !iconName.isBlank()) {
      PreferencesUiSupport.configureIconOnlyButton(button, iconName, tooltip);
      return;
    }
    button.setToolTipText(tooltip);
  }

  public enum SelectionMode {
    FILES,
    DIRECTORIES
  }

  public record Request(
      String initialPath,
      String browseButtonText,
      String clearButtonText,
      String browseIconName,
      String browseTooltip,
      String clearIconName,
      String clearTooltip,
      String chooserDialogTitle,
      SelectionMode selectionMode,
      Component owner,
      BooleanSupplier availableSupplier,
      boolean editableWhenAvailable) {
    public Request {
      browseButtonText = Objects.toString(browseButtonText, "Browse...");
      clearButtonText = Objects.toString(clearButtonText, "Clear");
      chooserDialogTitle = Objects.toString(chooserDialogTitle, "Select path");
      if (selectionMode == null) selectionMode = SelectionMode.FILES;
      if (availableSupplier == null) availableSupplier = () -> true;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static final class Builder {
      private String initialPath;
      private String browseButtonText;
      private String clearButtonText;
      private String browseIconName;
      private String browseTooltip;
      private String clearIconName;
      private String clearTooltip;
      private String chooserDialogTitle;
      private SelectionMode selectionMode;
      private Component owner;
      private BooleanSupplier availableSupplier;
      private boolean editableWhenAvailable;

      private Builder() {}

      public Builder initialPath(String initialPath) {
        this.initialPath = initialPath;
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

      public Builder browseIconName(String browseIconName) {
        this.browseIconName = browseIconName;
        return this;
      }

      public Builder browseTooltip(String browseTooltip) {
        this.browseTooltip = browseTooltip;
        return this;
      }

      public Builder clearIconName(String clearIconName) {
        this.clearIconName = clearIconName;
        return this;
      }

      public Builder clearTooltip(String clearTooltip) {
        this.clearTooltip = clearTooltip;
        return this;
      }

      public Builder chooserDialogTitle(String chooserDialogTitle) {
        this.chooserDialogTitle = chooserDialogTitle;
        return this;
      }

      public Builder selectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
        return this;
      }

      public Builder owner(Component owner) {
        this.owner = owner;
        return this;
      }

      public Builder availableSupplier(BooleanSupplier availableSupplier) {
        this.availableSupplier = availableSupplier;
        return this;
      }

      public Builder editableWhenAvailable(boolean editableWhenAvailable) {
        this.editableWhenAvailable = editableWhenAvailable;
        return this;
      }

      public Request build() {
        return new Request(
            initialPath,
            browseButtonText,
            clearButtonText,
            browseIconName,
            browseTooltip,
            clearIconName,
            clearTooltip,
            chooserDialogTitle,
            selectionMode,
            owner,
            availableSupplier,
            editableWhenAvailable);
      }
    }
  }

  public record Controls(
      JTextField path,
      JButton browseButton,
      JButton clearButton,
      String chooserDialogTitle,
      SelectionMode selectionMode,
      Component owner,
      BooleanSupplier availableSupplier,
      boolean editableWhenAvailable) {
    public void refresh() {
      boolean available = availableSupplier == null || availableSupplier.getAsBoolean();
      path.setEnabled(available);
      path.setEditable(available && editableWhenAvailable);
      browseButton.setEnabled(available);
      clearButton.setEnabled(available && !pathValue().isBlank());
    }

    public String pathValue() {
      return PreferencesUiSupport.trimmedText(path);
    }

    private void choosePath() {
      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle(chooserDialogTitle);
      chooser.setFileSelectionMode(fileSelectionMode());
      chooser.setAcceptAllFileFilterUsed(true);
      int result = chooser.showOpenDialog(dialogOwner());
      if (result != JFileChooser.APPROVE_OPTION) return;

      File selected = chooser.getSelectedFile();
      if (selected == null) return;
      path.setText(selected.getAbsolutePath());
      refresh();
    }

    private void clearPath() {
      path.setText("");
      refresh();
    }

    private int fileSelectionMode() {
      return switch (selectionMode) {
        case DIRECTORIES -> JFileChooser.DIRECTORIES_ONLY;
        case FILES -> JFileChooser.FILES_ONLY;
      };
    }

    private Component dialogOwner() {
      if (owner != null) return owner;
      return SwingUtilities.getWindowAncestor(browseButton);
    }
  }
}

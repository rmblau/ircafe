package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.config.UiProperties;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettings;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;

final class AppearancePanelSupport {
  private AppearancePanelSupport() {}

  static JPanel buildPanel(
      ThemeControls theme,
      AccentControls accent,
      ChatThemeControls chatTheme,
      FontControls fonts,
      TweakControls tweaks,
      AppearanceServerTreeControls serverTree) {
    JPanel form =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]8[grow,push]8[]"));

    form.add(PreferencesUiSupport.tabTitle("Appearance"), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JTabbedPane appearanceTabs = new JTabbedPane();
    appearanceTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    appearanceTabs.addTab(
        "Theme", PreferencesUiSupport.padSubTab(buildThemeSubTab(theme, accent, tweaks)));
    appearanceTabs.addTab("UI font", PreferencesUiSupport.padSubTab(buildUiFontSubTab(tweaks)));
    appearanceTabs.addTab(
        "Chat colors", PreferencesUiSupport.padSubTab(buildChatColorsSubTab(chatTheme)));
    appearanceTabs.addTab("Chat text", PreferencesUiSupport.padSubTab(buildChatTextSubTab(fonts)));
    appearanceTabs.addTab(
        "Server tree", PreferencesUiSupport.padSubTab(buildServerTreeSubTab(serverTree)));
    form.add(appearanceTabs, MigLayoutConstraints.GROW_PUSH_WMIN_0);

    JButton reset = new JButton("Reset to defaults");
    reset.setToolTipText(
        "Revert the appearance controls to default values. Changes preview live; Apply/OK saves.");
    reset.addActionListener(
        event -> {
          theme.combo.setSelectedItem("darcula");
          fonts.fontFamily.setSelectedItem("Monospaced");
          fonts.fontSize.setValue(12);
          accent.preset.setSelectedItem(AccentPreset.IRCAFE_COBALT);
          accent.enabled.setSelected(true);
          accent.hex.setText(UiProperties.DEFAULT_ACCENT_COLOR);
          accent.strength.setValue(UiProperties.DEFAULT_ACCENT_STRENGTH);

          for (int i = 0; i < tweaks.density.getItemCount(); i++) {
            DensityOption option = tweaks.density.getItemAt(i);
            if (option != null && "auto".equalsIgnoreCase(option.id)) {
              tweaks.density.setSelectedIndex(i);
              break;
            }
          }
          tweaks.cornerRadius.setValue(10);
          tweaks.uiFontOverrideEnabled.setSelected(false);
          tweaks.uiFontFamily.setSelectedItem(ThemeTweakSettings.DEFAULT_UI_FONT_FAMILY);
          tweaks.uiFontSize.setValue(ThemeTweakSettings.DEFAULT_UI_FONT_SIZE);

          chatTheme.preset.setSelectedItem(ChatThemeSettings.Preset.DEFAULT);
          clearColorField(chatTheme.timestamp);
          clearColorField(chatTheme.system);
          clearColorField(chatTheme.mention);
          clearColorField(chatTheme.message);
          clearColorField(chatTheme.notice);
          clearColorField(chatTheme.action);
          clearColorField(chatTheme.error);
          clearColorField(chatTheme.presence);
          chatTheme.mentionStrength.setValue(35);

          clearColorField(serverTree.unreadChannelColor);
          clearColorField(serverTree.highlightChannelColor);
          serverTree.preserveDockLayoutBetweenSessions.setSelected(false);

          accent.applyEnabledState.run();
          accent.syncPresetFromHex.run();
          tweaks.applyUiFontEnabledState.run();
        });
    form.add(reset, "split 2, alignx left");
    form.add(
        PreferencesUiSupport.helpText("Changes preview live. Use Apply or OK to save."),
        "alignx left, gapleft 12, growx, wmin 0");

    return form;
  }

  private static JPanel buildThemeSubTab(
      ThemeControls theme, AccentControls accent, TweakControls tweaks) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
                MigLayoutConstraints.RIGHT_12_GROW_FILL,
                "[]8[]6[]6[]6[]6[]"));
    panel.setOpaque(false);

    panel.add(
        PreferencesUiSupport.sectionTitle("Look & feel"),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    panel.add(new JLabel("Theme"));
    panel.add(theme.combo, MigLayoutConstraints.GROW_X);

    JPanel accentLabel = new JPanel(new MigLayout(MigLayoutConstraints.INSETS_0, "[]6[]", "[]"));
    accentLabel.setOpaque(false);
    accentLabel.add(new JLabel("Accent"));
    accentLabel.add(accent.chip);
    panel.add(accentLabel);
    panel.add(accent.panel, MigLayoutConstraints.GROW_X);

    panel.add(new JLabel("Accent strength"));
    panel.add(accent.strength, MigLayoutConstraints.GROW_X);

    panel.add(new JLabel("Density"));
    panel.add(tweaks.density, MigLayoutConstraints.GROW_X);

    panel.add(new JLabel("Corner radius"));
    panel.add(tweaks.cornerRadius, MigLayoutConstraints.GROW_X);

    JTextArea tweakHint = PreferencesUiSupport.subtleInfoText();
    tweakHint.setText("Density and corner radius are available for FlatLaf-based themes.");
    panel.add(new JLabel(""));
    panel.add(tweakHint, MigLayoutConstraints.GROW_X_WMIN_0);

    return panel;
  }

  private static JPanel buildUiFontSubTab(TweakControls tweaks) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
                MigLayoutConstraints.RIGHT_12_GROW_FILL,
                "[]8[]6[]6[]"));
    panel.setOpaque(false);

    panel.add(
        PreferencesUiSupport.sectionTitle("UI text"),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    panel.add(new JLabel("Font override"));
    panel.add(tweaks.uiFontOverrideEnabled, MigLayoutConstraints.GROW_X);
    panel.add(new JLabel("Font family"));
    panel.add(tweaks.uiFontFamily, MigLayoutConstraints.GROW_X);
    panel.add(new JLabel("Font size"));
    panel.add(tweaks.uiFontSize, MigLayoutConstraints.WIDTH_110);

    JTextArea uiFontHint = PreferencesUiSupport.subtleInfoText();
    uiFontHint.setText(
        "Applies globally to menus, dialogs, tabs, forms, and controls for all themes.");
    panel.add(new JLabel(""));
    panel.add(uiFontHint, MigLayoutConstraints.GROW_X_WMIN_0);

    return panel;
  }

  private static JPanel buildChatColorsSubTab(ChatThemeControls chatTheme) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]8[]12[]8[]"));
    panel.setOpaque(false);

    panel.add(
        PreferencesUiSupport.sectionTitle("Palette"), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(buildChatThemePaletteSubTab(chatTheme), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    panel.add(
        PreferencesUiSupport.sectionTitle("Message colors"),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(buildChatMessageColorsSubTab(chatTheme), MigLayoutConstraints.GROW_X_WMIN_0);

    return panel;
  }

  private static JPanel buildChatTextSubTab(FontControls fonts) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
                MigLayoutConstraints.RIGHT_12_GROW_FILL,
                "[]8[]6[]"));
    panel.setOpaque(false);

    panel.add(
        PreferencesUiSupport.sectionTitle("Chat text"),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    panel.add(new JLabel("Font family"));
    panel.add(fonts.fontFamily, MigLayoutConstraints.GROW_X);
    panel.add(new JLabel("Font size"));
    panel.add(fonts.fontSize, MigLayoutConstraints.WIDTH_110);

    return panel;
  }

  private static JPanel buildServerTreeSubTab(AppearanceServerTreeControls serverTree) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
                MigLayoutConstraints.RIGHT_12_GROW_FILL,
                "[]8[]6[]"));
    panel.setOpaque(false);

    panel.add(
        PreferencesUiSupport.sectionTitle("Server tree"),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    panel.add(new JLabel("Unread channel color"));
    panel.add(serverTree.unreadChannelColor.panel, MigLayoutConstraints.GROW_X);
    panel.add(new JLabel("Highlight channel color"));
    panel.add(serverTree.highlightChannelColor.panel, MigLayoutConstraints.GROW_X);
    panel.add(new JLabel("Dock layout"));
    panel.add(serverTree.preserveDockLayoutBetweenSessions, MigLayoutConstraints.GROW_X);

    JTextArea hint = PreferencesUiSupport.subtleInfoText();
    hint.setText(
        "Leave colors blank to use theme defaults. Dock layout restore applies on next launch.");
    panel.add(new JLabel(""));
    panel.add(hint, MigLayoutConstraints.GROW_X_WMIN_0);

    return panel;
  }

  private static JPanel buildChatThemePaletteSubTab(ChatThemeControls chatTheme) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
                MigLayoutConstraints.RIGHT_12_GROW_FILL,
                "[]6[]6[]6[]"));
    panel.setOpaque(false);

    panel.add(new JLabel("Chat theme preset"));
    panel.add(chatTheme.preset, MigLayoutConstraints.GROW_X);

    panel.add(new JLabel("Timestamp color"));
    panel.add(chatTheme.timestamp.panel, MigLayoutConstraints.GROW_X);

    panel.add(new JLabel("Mention highlight"));
    panel.add(chatTheme.mention.panel, MigLayoutConstraints.GROW_X);

    panel.add(new JLabel("Mention strength"));
    panel.add(chatTheme.mentionStrength, MigLayoutConstraints.GROW_X);

    panel.add(new JLabel(""));
    panel.add(
        PreferencesUiSupport.helpText(
            "Use Message colors when you want to override specific line types."),
        MigLayoutConstraints.GROW_X_WMIN_0);
    return panel;
  }

  private static JPanel buildChatMessageColorsSubTab(ChatThemeControls chatTheme) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
                MigLayoutConstraints.RIGHT_12_GROW_FILL,
                "[]6[]6[]6[]6[]6[]6[]"));
    panel.setOpaque(false);

    panel.add(new JLabel("Server/system"));
    panel.add(chatTheme.system.panel, MigLayoutConstraints.GROW_X);

    panel.add(new JLabel("User messages"));
    panel.add(chatTheme.message.panel, MigLayoutConstraints.GROW_X);

    panel.add(new JLabel("Notice messages"));
    panel.add(chatTheme.notice.panel, MigLayoutConstraints.GROW_X);

    panel.add(new JLabel("Action messages"));
    panel.add(chatTheme.action.panel, MigLayoutConstraints.GROW_X);

    panel.add(new JLabel("Presence messages"));
    panel.add(chatTheme.presence.panel, MigLayoutConstraints.GROW_X);

    panel.add(new JLabel("Error messages"));
    panel.add(chatTheme.error.panel, MigLayoutConstraints.GROW_X);

    panel.add(new JLabel(""));
    panel.add(
        PreferencesUiSupport.helpText("Leave any field blank to use the theme default."),
        MigLayoutConstraints.GROW_X_WMIN_0);

    return panel;
  }

  private static void clearColorField(ColorField colorField) {
    colorField.hex.setText("");
    colorField.updateIcon.run();
  }
}

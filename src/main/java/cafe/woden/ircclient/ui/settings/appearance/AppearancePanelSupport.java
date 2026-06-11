package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.config.properties.UiProperties;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettings;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

final class AppearancePanelSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private AppearancePanelSupport() {}

  static JPanel buildPanel(
      ThemeControls theme,
      AccentControls accent,
      ChatThemeControls chatTheme,
      FontControls fonts,
      TweakControls tweaks,
      AppearanceServerTreeControls serverTree) {
    JPanel form = new JPanel(MigLayouts.singleColumnFill(12, "[]8[grow,push]8[]"));

    form.add(
        PreferencesUiSupport.tabTitle(MESSAGES.text("preferences.appearance.title")),
        MigConstraints.growXMinWidth0Wrap());

    JTabbedPane appearanceTabs = new JTabbedPane();
    appearanceTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    appearanceTabs.addTab(
        MESSAGES.text("preferences.appearance.tab.theme"),
        PreferencesUiSupport.padSubTab(buildThemeSubTab(theme, accent, tweaks)));
    appearanceTabs.addTab(
        MESSAGES.text("preferences.appearance.tab.uiFont"),
        PreferencesUiSupport.padSubTab(buildUiFontSubTab(tweaks)));
    appearanceTabs.addTab(
        MESSAGES.text("preferences.appearance.tab.chatColors"),
        PreferencesUiSupport.padSubTab(buildChatColorsSubTab(chatTheme)));
    appearanceTabs.addTab(
        MESSAGES.text("preferences.appearance.tab.chatText"),
        PreferencesUiSupport.padSubTab(buildChatTextSubTab(fonts)));
    appearanceTabs.addTab(
        MESSAGES.text("preferences.appearance.tab.serverTree"),
        PreferencesUiSupport.padSubTab(buildServerTreeSubTab(serverTree)));
    form.add(appearanceTabs, MigConstraints.growPushMinWidth0());

    JButton reset = new JButton(MESSAGES.text("preferences.appearance.button.resetDefaults"));
    reset.setToolTipText(MESSAGES.text("preferences.appearance.button.resetDefaults.tooltip"));
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
    form.add(reset, MigConstraints.splitAlignXLeft(2));
    form.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.appearance.help.livePreview")),
        MigConstraints.alignXLeftGrowXMinWidthGapLeft(0, 12));

    return form;
  }

  private static JPanel buildThemeSubTab(
      ThemeControls theme, AccentControls accent, TweakControls tweaks) {
    JPanel panel = new JPanel(MigLayouts.twoColumnForm(12, MigLayouts.rowGaps(8, 6, 6, 6, 6)));
    panel.setOpaque(false);

    panel.add(
        PreferencesUiSupport.sectionTitle(
            MESSAGES.text("preferences.appearance.section.lookAndFeel")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.theme")));
    panel.add(theme.combo, MigConstraints.growX());

    JPanel accentLabel = new JPanel(MigLayouts.insets0("[]6[]", "[]"));
    accentLabel.setOpaque(false);
    accentLabel.add(new JLabel(MESSAGES.text("preferences.appearance.field.accent")));
    accentLabel.add(accent.chip);
    panel.add(accentLabel);
    panel.add(accent.panel, MigConstraints.growX());

    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.accentStrength")));
    panel.add(accent.strength, MigConstraints.growX());

    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.density")));
    panel.add(tweaks.density, MigConstraints.growX());

    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.cornerRadius")));
    panel.add(tweaks.cornerRadius, MigConstraints.growX());

    JTextArea tweakHint = PreferencesUiSupport.subtleInfoText();
    tweakHint.setText(MESSAGES.text("preferences.appearance.help.flatlafTweaks"));
    panel.add(new JLabel(""));
    panel.add(tweakHint, MigConstraints.growXMinWidth0());

    return panel;
  }

  private static JPanel buildUiFontSubTab(TweakControls tweaks) {
    JPanel panel = new JPanel(MigLayouts.twoColumnForm(12, MigLayouts.rowGaps(8, 6, 6)));
    panel.setOpaque(false);

    panel.add(
        PreferencesUiSupport.sectionTitle(MESSAGES.text("preferences.appearance.section.uiText")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.fontOverride")));
    panel.add(tweaks.uiFontOverrideEnabled, MigConstraints.growX());
    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.fontFamily")));
    panel.add(tweaks.uiFontFamily, MigConstraints.growX());
    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.fontSize")));
    panel.add(tweaks.uiFontSize, MigConstraints.width(110));

    JTextArea uiFontHint = PreferencesUiSupport.subtleInfoText();
    uiFontHint.setText(MESSAGES.text("preferences.appearance.help.uiFont"));
    panel.add(new JLabel(""));
    panel.add(uiFontHint, MigConstraints.growXMinWidth0());

    return panel;
  }

  private static JPanel buildChatColorsSubTab(ChatThemeControls chatTheme) {
    JPanel panel = new JPanel(MigLayouts.singleColumn(MigLayouts.rowGaps(8, 12, 8)));
    panel.setOpaque(false);

    panel.add(
        PreferencesUiSupport.sectionTitle(MESSAGES.text("preferences.appearance.section.palette")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(buildChatThemePaletteSubTab(chatTheme), MigConstraints.growXMinWidth0Wrap());

    panel.add(
        PreferencesUiSupport.sectionTitle(
            MESSAGES.text("preferences.appearance.section.messageColors")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(buildChatMessageColorsSubTab(chatTheme), MigConstraints.growXMinWidth0());

    return panel;
  }

  private static JPanel buildChatTextSubTab(FontControls fonts) {
    JPanel panel = new JPanel(MigLayouts.twoColumnForm(12, MigLayouts.rowGaps(8, 6)));
    panel.setOpaque(false);

    panel.add(
        PreferencesUiSupport.sectionTitle(MESSAGES.text("preferences.appearance.section.chatText")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.fontFamily")));
    panel.add(fonts.fontFamily, MigConstraints.growX());
    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.fontSize")));
    panel.add(fonts.fontSize, MigConstraints.width(110));

    return panel;
  }

  private static JPanel buildServerTreeSubTab(AppearanceServerTreeControls serverTree) {
    JPanel panel = new JPanel(MigLayouts.twoColumnForm(12, MigLayouts.rowGaps(8, 6)));
    panel.setOpaque(false);

    panel.add(
        PreferencesUiSupport.sectionTitle(
            MESSAGES.text("preferences.appearance.section.serverTree")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.unreadChannelColor")));
    panel.add(serverTree.unreadChannelColor.panel, MigConstraints.growX());
    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.highlightChannelColor")));
    panel.add(serverTree.highlightChannelColor.panel, MigConstraints.growX());
    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.dockLayout")));
    panel.add(serverTree.preserveDockLayoutBetweenSessions, MigConstraints.growX());

    JTextArea hint = PreferencesUiSupport.subtleInfoText();
    hint.setText(MESSAGES.text("preferences.appearance.help.serverTree"));
    panel.add(new JLabel(""));
    panel.add(hint, MigConstraints.growXMinWidth0());

    return panel;
  }

  private static JPanel buildChatThemePaletteSubTab(ChatThemeControls chatTheme) {
    JPanel panel = new JPanel(MigLayouts.twoColumnForm(12, MigLayouts.rows(4, 6)));
    panel.setOpaque(false);

    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.chatThemePreset")));
    panel.add(chatTheme.preset, MigConstraints.growX());

    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.timestampColor")));
    panel.add(chatTheme.timestamp.panel, MigConstraints.growX());

    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.mentionHighlight")));
    panel.add(chatTheme.mention.panel, MigConstraints.growX());

    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.mentionStrength")));
    panel.add(chatTheme.mentionStrength, MigConstraints.growX());

    panel.add(new JLabel(""));
    panel.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.appearance.help.messageColors")),
        MigConstraints.growXMinWidth0());
    return panel;
  }

  private static JPanel buildChatMessageColorsSubTab(ChatThemeControls chatTheme) {
    JPanel panel = new JPanel(MigLayouts.twoColumnForm(12, MigLayouts.rows(7, 6)));
    panel.setOpaque(false);

    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.serverSystem")));
    panel.add(chatTheme.system.panel, MigConstraints.growX());

    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.userMessages")));
    panel.add(chatTheme.message.panel, MigConstraints.growX());

    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.noticeMessages")));
    panel.add(chatTheme.notice.panel, MigConstraints.growX());

    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.actionMessages")));
    panel.add(chatTheme.action.panel, MigConstraints.growX());

    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.presenceMessages")));
    panel.add(chatTheme.presence.panel, MigConstraints.growX());

    panel.add(new JLabel(MESSAGES.text("preferences.appearance.field.errorMessages")));
    panel.add(chatTheme.error.panel, MigConstraints.growX());

    panel.add(new JLabel(""));
    panel.add(
        PreferencesUiSupport.helpText(
            MESSAGES.text("preferences.appearance.help.blankUsesThemeDefault")),
        MigConstraints.growXMinWidth0());

    return panel;
  }

  private static void clearColorField(ColorField colorField) {
    colorField.hex.setText("");
    colorField.updateIcon.run();
  }
}

package cafe.woden.ircclient.ui.settings.ircv3;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.irc.ircv3.Ircv3ExtensionCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3ExtensionRegistry;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

public final class Ircv3PanelSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private Ircv3PanelSupport() {}

  public static Ircv3CapabilitiesControls buildCapabilitiesControls(
      RuntimeConfigStore runtimeConfig, Ircv3ExtensionCatalog ircv3ExtensionCatalog) {
    Map<String, Boolean> persisted = runtimeConfig.readIrcv3Capabilities();
    Ircv3ExtensionCatalog catalog =
        ircv3ExtensionCatalog == null
            ? Ircv3ExtensionCatalog.builtInCatalog()
            : ircv3ExtensionCatalog;

    LinkedHashMap<String, JCheckBox> checkboxes = new LinkedHashMap<>();
    JPanel panel =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                0, 1, 3, MigLayoutConstraints.GROW_FILL, MigLayouts.rows(2, 6)));
    panel.setOpaque(false);

    LinkedHashMap<Ircv3ExtensionRegistry.UiGroup, List<Ircv3ExtensionRegistry.ExtensionDefinition>>
        grouped = new LinkedHashMap<>();
    for (Ircv3ExtensionRegistry.ExtensionDefinition definition :
        catalog.requestableCapabilities()) {
      grouped
          .computeIfAbsent(definition.uiMetadata().group(), __ -> new ArrayList<>())
          .add(definition);
    }

    for (Map.Entry<Ircv3ExtensionRegistry.UiGroup, List<Ircv3ExtensionRegistry.ExtensionDefinition>>
        group : grouped.entrySet()) {
      List<Ircv3ExtensionRegistry.ExtensionDefinition> caps = group.getValue();
      if (caps == null || caps.isEmpty()) continue;

      List<Ircv3ExtensionRegistry.ExtensionDefinition> orderedCaps = new ArrayList<>(caps);
      orderedCaps.sort(
          (left, right) -> {
            int leftOrder = left.uiMetadata().sortOrder();
            int rightOrder = right.uiMetadata().sortOrder();
            if (leftOrder != rightOrder) return Integer.compare(leftOrder, rightOrder);
            return left.uiMetadata().label().compareToIgnoreCase(right.uiMetadata().label());
          });

      JPanel groupPanel =
          new JPanel(
              MigLayouts.fillXWrapWithHideMode(
                  "6 8 8 8",
                  2,
                  3,
                  MigLayoutConstraints.GROW_FILL_GAP_12_GROW_FILL,
                  MigLayouts.rows(2, 2)));
      groupPanel.setBorder(
          BorderFactory.createCompoundBorder(
              BorderFactory.createTitledBorder(group.getKey().title()),
              BorderFactory.createEmptyBorder(4, 6, 4, 6)));
      groupPanel.setOpaque(false);

      for (Ircv3ExtensionRegistry.ExtensionDefinition definition : orderedCaps) {
        String preferenceKey = definition.preferenceKey();
        JCheckBox checkbox = new JCheckBox(definition.uiMetadata().label());
        checkbox.setSelected(persisted.getOrDefault(preferenceKey, Boolean.TRUE));
        checkbox.setToolTipText(definition.uiMetadata().impactSummary());
        checkboxes.put(preferenceKey, checkbox);

        JButton help =
            PreferencesUiSupport.whyHelpButton(helpTitle(definition), helpMessage(definition));
        help.setToolTipText(MESSAGES.text("preferences.ircv3.capabilityHelp.tooltip"));

        JPanel row = new JPanel(MigLayouts.fillX("[grow,fill]4[]", "[]"));
        row.setOpaque(false);
        row.add(checkbox, MigConstraints.growXMinWidth0());
        row.add(help, MigConstraints.alignYCenter());

        groupPanel.add(row, MigConstraints.growXMinWidth0());
      }

      panel.add(groupPanel, MigConstraints.growXMinWidth0Wrap());
    }

    return new Ircv3CapabilitiesControls(checkboxes, panel);
  }

  public static JPanel buildPanel(
      JCheckBox typingIndicatorsSendEnabled,
      JCheckBox typingIndicatorsReceiveEnabled,
      JCheckBox typingIndicatorsTreeDisplayEnabled,
      JCheckBox typingIndicatorsUsersListDisplayEnabled,
      JCheckBox typingIndicatorsTranscriptDisplayEnabled,
      JCheckBox typingIndicatorsSendSignalDisplayEnabled,
      JComboBox<?> typingTreeIndicatorStyle,
      JComboBox<?> matrixUserListNameDisplayMode,
      JCheckBox serverTreeNotificationBadgesEnabled,
      JSpinner serverTreeUnreadBadgeScalePercent,
      Ircv3CapabilitiesControls ircv3Capabilities) {
    JPanel form =
        new JPanel(
            MigLayouts.fillWrapWithHideMode(
                12, 1, 3, MigLayoutConstraints.GROW_FILL, "[]8[]8[grow,fill]"));

    form.add(
        PreferencesUiSupport.tabTitle(MESSAGES.text("preferences.ircv3.title")),
        MigConstraints.growXMinWidth0Wrap());
    form.add(
        PreferencesUiSupport.subtleInfoTextWith(
            MESSAGES.text("preferences.ircv3.subtitle")),
        MigConstraints.growXMinWidth0Wrap());

    JButton typingHelp =
        PreferencesUiSupport.whyHelpButton(
            MESSAGES.text("preferences.ircv3.typing.help.title"),
            MESSAGES.text("preferences.ircv3.typing.help.message"));
    typingHelp.setToolTipText(MESSAGES.text("preferences.ircv3.typing.help.tooltip"));

    JPanel typingRow =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                8, 1, 3, MigLayoutConstraints.GROW_FILL_GAP_6_TRAILING, MigLayouts.rows(5, 2)));
    typingRow.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(MESSAGES.text("preferences.ircv3.typing.section")),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)));
    typingRow.setOpaque(false);
    typingRow.add(typingIndicatorsSendEnabled, MigConstraints.growXMinWidthSplit(0, 2));
    typingRow.add(typingHelp, MigConstraints.alignYCenter());
    typingRow.add(typingIndicatorsReceiveEnabled, MigConstraints.growXMinWidth0());

    JPanel treeStyleRow = new JPanel(MigLayouts.fillX(MigLayoutConstraints.ROW_8_GROW_FILL, "[]"));
    treeStyleRow.setOpaque(false);
    treeStyleRow.add(new JLabel(MESSAGES.text("preferences.ircv3.field.serverTreeMarkerStyle")));
    treeStyleRow.add(typingTreeIndicatorStyle, MigConstraints.growXMinWidth(180));
    typingRow.add(treeStyleRow, MigConstraints.growXMinWidth0());

    JPanel displaysRow =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                0, 2, 3, "[grow,fill]16[grow,fill]", MigLayouts.rows(2, 2)));
    displaysRow.setOpaque(false);
    displaysRow.add(typingIndicatorsTreeDisplayEnabled, MigConstraints.growXMinWidth0());
    displaysRow.add(typingIndicatorsUsersListDisplayEnabled, MigConstraints.growXMinWidth0());
    displaysRow.add(typingIndicatorsTranscriptDisplayEnabled, MigConstraints.growXMinWidth0());
    displaysRow.add(typingIndicatorsSendSignalDisplayEnabled, MigConstraints.growXMinWidth0());
    typingRow.add(displaysRow, MigConstraints.growXMinWidth0());

    JPanel matrixNamesRow =
        new JPanel(MigLayouts.fillX(MigLayoutConstraints.ROW_8_GROW_FILL, "[]"));
    matrixNamesRow.setOpaque(false);
    matrixNamesRow.add(new JLabel(MESSAGES.text("preferences.ircv3.field.matrixUserListNames")));
    matrixNamesRow.add(matrixUserListNameDisplayMode, MigConstraints.growXMinWidth(220));
    typingRow.add(matrixNamesRow, MigConstraints.growXMinWidth0());

    typingRow.add(serverTreeNotificationBadgesEnabled, MigConstraints.growXMinWidth0());

    JPanel badgeScaleRow = new JPanel(MigLayouts.fillX("[]8[]6[]", "[]"));
    badgeScaleRow.setOpaque(false);
    badgeScaleRow.add(new JLabel(MESSAGES.text("preferences.ircv3.field.unreadBadgeSize")));
    badgeScaleRow.add(serverTreeUnreadBadgeScalePercent, MigConstraints.width(90));
    badgeScaleRow.add(new JLabel("%"));
    typingRow.add(badgeScaleRow, MigConstraints.growXMinWidth0());

    JTextArea typingImpact = PreferencesUiSupport.subtleInfoText();
    typingImpact.setText(
        MESSAGES.text("preferences.ircv3.typing.impact"));
    typingImpact.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));

    JPanel typingTab =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                6, 1, 3, MigLayoutConstraints.GROW_FILL, MigLayouts.rows(2, 6)));
    typingTab.setOpaque(false);
    typingTab.add(typingRow, MigConstraints.growXMinWidth0Wrap());
    typingTab.add(typingImpact, MigConstraints.growXMinWidth0Wrap());

    JPanel capabilityBlock =
        new JPanel(
            MigLayouts.fillWrapWithHideMode(
                8, 1, 3, MigLayoutConstraints.GROW_FILL, MigLayoutConstraints.ROW_6_GROW_FILL));
    capabilityBlock.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                MESSAGES.text("preferences.ircv3.capabilities.section")),
            BorderFactory.createEmptyBorder(4, 6, 6, 6)));
    capabilityBlock.setOpaque(false);
    capabilityBlock.add(
        PreferencesUiSupport.subtleInfoTextWith(
            MESSAGES.text("preferences.ircv3.capabilities.help")),
        MigConstraints.growXMinWidth0Wrap());

    JScrollPane capabilityScroll =
        new JScrollPane(
            ircv3Capabilities.panel(),
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    capabilityScroll.setBorder(BorderFactory.createEmptyBorder());
    capabilityScroll.setViewportBorder(null);
    capabilityScroll.getVerticalScrollBar().setUnitIncrement(16);
    capabilityScroll.setPreferredSize(new Dimension(1, 320));
    capabilityBlock.add(capabilityScroll, MigConstraints.growPushMinWidth0MinHeight(180));

    JPanel capabilitiesTab =
        new JPanel(
            MigLayouts.fillWrapWithHideMode(
                6, 1, 3, MigLayoutConstraints.GROW_FILL, MigLayoutConstraints.GROW_FILL));
    capabilitiesTab.setOpaque(false);
    capabilitiesTab.add(capabilityBlock, MigConstraints.growPushMinWidth0());

    JButton typingHeader = new JButton();
    typingHeader.setHorizontalAlignment(SwingConstants.LEFT);
    typingHeader.setFocusable(false);
    typingHeader.setMargin(new Insets(6, 10, 6, 10));
    typingHeader.setToolTipText(MESSAGES.text("preferences.ircv3.accordion.typing.tooltip"));

    JButton capabilitiesHeader = new JButton();
    capabilitiesHeader.setHorizontalAlignment(SwingConstants.LEFT);
    capabilitiesHeader.setFocusable(false);
    capabilitiesHeader.setMargin(new Insets(6, 10, 6, 10));
    capabilitiesHeader.setToolTipText(
        MESSAGES.text("preferences.ircv3.accordion.capabilities.tooltip"));

    final boolean[] typingExpanded = new boolean[] {true};
    final boolean[] capabilitiesExpanded = new boolean[] {false};

    Runnable refreshAccordion =
        () -> {
          typingHeader.setText(
              (typingExpanded[0] ? "▾ " : "▸ ")
                  + MESSAGES.text("preferences.ircv3.typing.section"));
          capabilitiesHeader.setText(
              (capabilitiesExpanded[0] ? "▾ " : "▸ ")
                  + MESSAGES.text("preferences.ircv3.capabilities.section"));
          typingTab.setVisible(typingExpanded[0]);
          capabilitiesTab.setVisible(capabilitiesExpanded[0]);
          form.revalidate();
          form.repaint();
        };

    typingHeader.addActionListener(
        event -> {
          if (typingExpanded[0]) return;
          typingExpanded[0] = true;
          capabilitiesExpanded[0] = false;
          refreshAccordion.run();
        });

    capabilitiesHeader.addActionListener(
        event -> {
          if (capabilitiesExpanded[0]) return;
          capabilitiesExpanded[0] = true;
          typingExpanded[0] = false;
          refreshAccordion.run();
        });

    form.add(typingHeader, MigConstraints.growXMinWidth0Wrap());
    form.add(typingTab, MigConstraints.growXMinWidthHideModeWrap(0, 3));
    form.add(capabilitiesHeader, MigConstraints.growXMinWidth0Wrap());
    form.add(capabilitiesTab, MigConstraints.growPushMinWidth0MinHeightHideMode(180, 3));
    refreshAccordion.run();

    return form;
  }

  public static void persistCapabilities(
      RuntimeConfigStore runtimeConfig, Map<String, Boolean> capabilities) {
    if (capabilities == null || capabilities.isEmpty()) return;
    for (Map.Entry<String, Boolean> entry : capabilities.entrySet()) {
      String key = Ircv3ExtensionRegistry.preferenceKeyFor(entry.getKey());
      if (key.isEmpty()) continue;
      boolean enabled = Boolean.TRUE.equals(entry.getValue());
      runtimeConfig.rememberIrcv3CapabilityEnabled(key, enabled);
    }
  }

  private static String helpTitle(Ircv3ExtensionRegistry.ExtensionDefinition definition) {
    return MESSAGES.text(
        "preferences.ircv3.capabilityHelp.title",
        definition.uiMetadata().label(),
        definition.requestToken());
  }

  private static String helpMessage(Ircv3ExtensionRegistry.ExtensionDefinition definition) {
    return MESSAGES.text(
        "preferences.ircv3.capabilityHelp.message",
        definition.requestToken(),
        definition.uiMetadata().impactSummary());
  }
}

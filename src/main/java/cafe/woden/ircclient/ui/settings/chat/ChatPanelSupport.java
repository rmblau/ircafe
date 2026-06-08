package cafe.woden.ircclient.ui.settings.chat;

import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.nickcolor.NickColorControls;
import cafe.woden.ircclient.ui.settings.outgoing.OutgoingColorControls;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckControls;
import cafe.woden.ircclient.ui.settings.timestamp.TimestampControls;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public final class ChatPanelSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private ChatPanelSupport() {}

  public static JPanel buildPanel(
      JCheckBox presenceFolds,
      JCheckBox ctcpRequestsInActiveTarget,
      JTextField defaultQuitMessage,
      JCheckBox nickCompletionCycleWithTab,
      JCheckBox nickCompletionAppendAddressSuffix,
      SpellcheckControls spellcheck,
      NickColorControls nickColors,
      TimestampControls timestamps,
      OutgoingColorControls outgoing,
      JCheckBox outgoingDeliveryIndicators) {
    JPanel form = new JPanel(MigLayouts.singleColumnFill(12, "[]10[grow,fill]"));
    form.add(
        PreferencesUiSupport.tabTitle(MESSAGES.text("preferences.chat.title")),
        MigConstraints.growXMinWidth0Wrap());

    JTabbedPane chatTabs = new JTabbedPane();
    chatTabs.addTab(
        MESSAGES.text("preferences.chat.tab.general"),
        PreferencesUiSupport.padSubTab(
            buildGeneralSubTab(
                presenceFolds,
                ctcpRequestsInActiveTarget,
                defaultQuitMessage,
                nickCompletionCycleWithTab,
                nickCompletionAppendAddressSuffix,
                nickColors,
                timestamps,
                outgoing,
                outgoingDeliveryIndicators)));
    chatTabs.addTab(
        MESSAGES.text("preferences.chat.tab.spellcheck"),
        PreferencesUiSupport.padSubTab(buildSpellcheckSubTab(spellcheck)));
    form.add(chatTabs, MigConstraints.growPushMinWidth0());
    return form;
  }

  private static JPanel buildGeneralSubTab(
      JCheckBox presenceFolds,
      JCheckBox ctcpRequestsInActiveTarget,
      JTextField defaultQuitMessage,
      JCheckBox nickCompletionCycleWithTab,
      JCheckBox nickCompletionAppendAddressSuffix,
      NickColorControls nickColors,
      TimestampControls timestamps,
      OutgoingColorControls outgoing,
      JCheckBox outgoingDeliveryIndicators) {
    JPanel panel = new JPanel(MigLayouts.twoColumnForm(12, MigLayouts.rowGaps(8, 6, 6, 6, 10, 6)));
    panel.setOpaque(false);

    panel.add(
        PreferencesUiSupport.sectionTitle(MESSAGES.text("preferences.chat.section.display")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    panel.add(
        new JLabel(MESSAGES.text("preferences.chat.field.presenceEvents")),
        MigConstraints.alignYTop());
    panel.add(presenceFolds, MigConstraints.alignXLeft());

    panel.add(
        new JLabel(MESSAGES.text("preferences.chat.field.ctcpRequests")),
        MigConstraints.alignYTop());
    panel.add(ctcpRequestsInActiveTarget, MigConstraints.alignXLeft());

    panel.add(
        new JLabel(MESSAGES.text("preferences.chat.field.nickColors")), MigConstraints.alignYTop());
    panel.add(nickColors.panel(), MigConstraints.growXMinWidth0());

    panel.add(
        new JLabel(MESSAGES.text("preferences.chat.field.timestamps")), MigConstraints.alignYTop());
    panel.add(timestamps.panel(), MigConstraints.growXMinWidth0());

    panel.add(
        PreferencesUiSupport.sectionTitle(MESSAGES.text("preferences.chat.section.yourMessages")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    panel.add(
        new JLabel(MESSAGES.text("preferences.chat.field.outgoingMessages")),
        MigConstraints.alignYTop());
    panel.add(outgoing.panel(), MigConstraints.growXMinWidth0());
    panel.add(
        new JLabel(MESSAGES.text("preferences.chat.field.deliveryIndicators")),
        MigConstraints.alignYTop());
    panel.add(outgoingDeliveryIndicators, MigConstraints.alignXLeft());
    panel.add(
        new JLabel(MESSAGES.text("preferences.chat.field.defaultQuitMessage")),
        MigConstraints.alignYTop());
    panel.add(defaultQuitMessage, MigConstraints.growXMinWidth0());

    panel.add(
        PreferencesUiSupport.sectionTitle(MESSAGES.text("preferences.chat.section.nickCompletion")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    panel.add(
        new JLabel(MESSAGES.text("preferences.chat.field.tabBehavior")),
        MigConstraints.alignYTop());
    panel.add(nickCompletionCycleWithTab, MigConstraints.alignXLeft());
    panel.add(
        new JLabel(MESSAGES.text("preferences.chat.field.addressingSuffix")),
        MigConstraints.alignYTop());
    panel.add(nickCompletionAppendAddressSuffix, MigConstraints.alignXLeft());

    return panel;
  }

  private static JPanel buildSpellcheckSubTab(SpellcheckControls spellcheck) {
    JPanel panel = new JPanel(MigLayouts.singleColumn(MigLayouts.rows(2, 8)));
    panel.setOpaque(false);
    panel.add(
        PreferencesUiSupport.sectionTitle(MESSAGES.text("preferences.chat.section.input")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(spellcheck.panel(), MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.chat.spellcheck.help")),
        MigConstraints.growXMinWidth0Wrap());
    return panel;
  }
}

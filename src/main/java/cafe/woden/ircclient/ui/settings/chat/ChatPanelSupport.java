package cafe.woden.ircclient.ui.settings.chat;

import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.nickcolor.NickColorControls;
import cafe.woden.ircclient.ui.settings.outgoing.OutgoingColorControls;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckControls;
import cafe.woden.ircclient.ui.settings.timestamp.TimestampControls;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

public final class ChatPanelSupport {
  private ChatPanelSupport() {}

  public static JPanel buildPanel(
      JCheckBox presenceFolds,
      JCheckBox ctcpRequestsInActiveTarget,
      JTextField defaultQuitMessage,
      SpellcheckControls spellcheck,
      NickColorControls nickColors,
      TimestampControls timestamps,
      OutgoingColorControls outgoing,
      JCheckBox outgoingDeliveryIndicators) {
    JPanel form =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]10[grow,fill]"));
    form.add(PreferencesUiSupport.tabTitle("Chat"), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JTabbedPane chatTabs = new JTabbedPane();
    chatTabs.addTab(
        "General",
        PreferencesUiSupport.padSubTab(
            buildGeneralSubTab(
                presenceFolds,
                ctcpRequestsInActiveTarget,
                defaultQuitMessage,
                nickColors,
                timestamps,
                outgoing,
                outgoingDeliveryIndicators)));
    chatTabs.addTab(
        "Spellcheck", PreferencesUiSupport.padSubTab(buildSpellcheckSubTab(spellcheck)));
    form.add(chatTabs, MigLayoutConstraints.GROW_PUSH_WMIN_0);
    return form;
  }

  private static JPanel buildGeneralSubTab(
      JCheckBox presenceFolds,
      JCheckBox ctcpRequestsInActiveTarget,
      JTextField defaultQuitMessage,
      NickColorControls nickColors,
      TimestampControls timestamps,
      OutgoingColorControls outgoing,
      JCheckBox outgoingDeliveryIndicators) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
                MigLayoutConstraints.RIGHT_12_GROW_FILL,
                "[]8[]6[]6[]6[]10[]6[]"));
    panel.setOpaque(false);

    panel.add(
        PreferencesUiSupport.sectionTitle("Display"),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    panel.add(new JLabel("Presence events"), MigLayoutConstraints.ALIGN_Y_TOP);
    panel.add(presenceFolds, "alignx left");

    panel.add(new JLabel("CTCP requests"), MigLayoutConstraints.ALIGN_Y_TOP);
    panel.add(ctcpRequestsInActiveTarget, "alignx left");

    panel.add(new JLabel("Nick colors"), MigLayoutConstraints.ALIGN_Y_TOP);
    panel.add(nickColors.panel(), MigLayoutConstraints.GROW_X_WMIN_0);

    panel.add(new JLabel("Timestamps"), MigLayoutConstraints.ALIGN_Y_TOP);
    panel.add(timestamps.panel(), MigLayoutConstraints.GROW_X_WMIN_0);

    panel.add(
        PreferencesUiSupport.sectionTitle("Your messages"),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    panel.add(new JLabel("Outgoing messages"), MigLayoutConstraints.ALIGN_Y_TOP);
    panel.add(outgoing.panel(), MigLayoutConstraints.GROW_X_WMIN_0);
    panel.add(new JLabel("Delivery indicators"), MigLayoutConstraints.ALIGN_Y_TOP);
    panel.add(outgoingDeliveryIndicators, "alignx left");
    panel.add(new JLabel("Default /quit message"), MigLayoutConstraints.ALIGN_Y_TOP);
    panel.add(defaultQuitMessage, MigLayoutConstraints.GROW_X_WMIN_0);

    return panel;
  }

  private static JPanel buildSpellcheckSubTab(SpellcheckControls spellcheck) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]8[]"));
    panel.setOpaque(false);
    panel.add(PreferencesUiSupport.sectionTitle("Input"), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(spellcheck.panel(), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(
        PreferencesUiSupport.helpText("Spellcheck settings are scoped to the message input bar."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    return panel;
  }
}

package cafe.woden.ircclient.ui.servers;

import cafe.woden.ircclient.ui.localization.UiMessages;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Builds the auto-join and perform tabs from the dialog's existing text areas. */
final class ServerEditorCommandTabBuilder {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();
  private ServerEditorCommandTabBuilder() {}

  static JPanel buildAutoJoinPanel(AutoJoinWidgets widgets) {
    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    JLabel hint =
        disabledHintLabel(MESSAGES.text("servers.editor.autoJoin.hint"));
    panel.add(hint, BorderLayout.NORTH);

    JPanel center = new JPanel(new GridLayout(2, 1, 0, 8));

    JPanel channels = new JPanel(new BorderLayout(6, 6));
    channels.add(new JLabel(MESSAGES.text("servers.editor.autoJoin.channels")), BorderLayout.NORTH);
    prepareTextArea(widgets.autoJoinChannelsArea());
    channels.add(wrappedArea(widgets.autoJoinChannelsArea()), BorderLayout.CENTER);
    center.add(channels);

    JPanel privateMessages = new JPanel(new BorderLayout(6, 6));
    privateMessages.add(new JLabel(MESSAGES.text("servers.editor.autoJoin.privateMessages")), BorderLayout.NORTH);
    prepareTextArea(widgets.autoJoinPrivateMessagesArea());
    privateMessages.add(wrappedArea(widgets.autoJoinPrivateMessagesArea()), BorderLayout.CENTER);
    center.add(privateMessages);

    panel.add(center, BorderLayout.CENTER);
    return panel;
  }

  static JPanel buildPerformPanel(PerformWidgets widgets) {
    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    JLabel hint =
        disabledHintLabel(MESSAGES.text("servers.editor.perform.hint"));
    panel.add(hint, BorderLayout.NORTH);

    prepareTextArea(widgets.performArea());
    panel.add(wrappedArea(widgets.performArea()), BorderLayout.CENTER);

    JLabel footer =
        disabledHintLabel(MESSAGES.text("servers.editor.perform.footer"));
    panel.add(footer, BorderLayout.SOUTH);
    return panel;
  }

  private static void prepareTextArea(JTextArea area) {
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
  }

  private static JScrollPane wrappedArea(JTextArea area) {
    JScrollPane scrollPane = new JScrollPane(area);
    scrollPane.putClientProperty(FlatClientProperties.STYLE, "arc:12;");
    return scrollPane;
  }

  private static JLabel disabledHintLabel(String text) {
    JLabel label = new JLabel(text);
    label.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground");
    return label;
  }

  record AutoJoinWidgets(JTextArea autoJoinChannelsArea, JTextArea autoJoinPrivateMessagesArea) {}

  record PerformWidgets(JTextArea performArea) {}
}

package cafe.woden.ircclient.ui.settings.commands;

import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public final class UserCommandsPanelSupport {
  private UserCommandsPanelSupport() {}

  public static JPanel buildPanel(UserCommandAliasesControls controls) {
    JPanel panel = new JPanel(MigLayouts.singleColumnFill(12, "[]8[]6[]8[grow,fill]8[]"));

    panel.add(PreferencesUiSupport.tabTitle("Commands"), MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.sectionTitle("User command aliases"),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.helpText(
            "Define custom /commands that expand before built-in parsing.\n"
                + "Placeholders: %1..%9 (positional), %1- (rest from arg), %* (all args), &1..&9 (from end), %c (channel), %t (target), %s/%e (server), %n (nick).\n"
                + "HexChat import maps %t (time), %m and %v into IRCafe-compatible placeholders.\n"
                + "Multi-command expansion: separate commands with ';' or new lines."),
        MigConstraints.growXMinWidth0Wrap());

    JPanel behavior = PreferencesUiSupport.captionPanel("Behavior");
    behavior.add(controls.unknownCommandAsRaw(), MigConstraints.growXMinWidth0Wrap());
    panel.add(behavior, MigConstraints.growXMinWidth0Wrap());

    JPanel buttons =
        PreferencesUiSupport.actionButtonRow(
            controls.add(),
            controls.importHexChat(),
            controls.duplicate(),
            controls.remove(),
            controls.up(),
            controls.down());

    JScrollPane tableScroll = new JScrollPane(controls.table());
    tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JScrollPane templateScroll = new JScrollPane(controls.template());
    templateScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    templateScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JPanel aliasList =
        PreferencesUiSupport.captionPanel(
            "Alias list",
            MigLayoutConstraints.INSETS_0_FILL_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            MigLayoutConstraints.ROW_6_GROW_FILL);
    aliasList.add(buttons, MigConstraints.growXMinWidth0Wrap());
    aliasList.add(tableScroll, "grow, push, h 220!, wmin 0");
    panel.add(aliasList, MigConstraints.growPushMinWidth0Wrap());

    JPanel editor =
        PreferencesUiSupport.captionPanel("Expansion editor", MigLayouts.singleColumn("[]6[]"));
    editor.add(controls.hint(), MigConstraints.growXMinWidth0Wrap());
    editor.add(templateScroll, "growx, h 140!, wmin 0, wrap");
    panel.add(editor, MigConstraints.growXMinWidth0Wrap());

    return panel;
  }
}

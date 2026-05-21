package cafe.woden.ircclient.ui.settings.commands;

import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import net.miginfocom.swing.MigLayout;

public final class UserCommandsPanelSupport {
  private UserCommandsPanelSupport() {}

  public static JPanel buildPanel(UserCommandAliasesControls controls) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]8[]6[]8[grow,fill]8[]"));

    panel.add(PreferencesUiSupport.tabTitle("Commands"), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(
        PreferencesUiSupport.sectionTitle("User command aliases"),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(
        PreferencesUiSupport.helpText(
            "Define custom /commands that expand before built-in parsing.\n"
                + "Placeholders: %1..%9 (positional), %1- (rest from arg), %* (all args), &1..&9 (from end), %c (channel), %t (target), %s/%e (server), %n (nick).\n"
                + "HexChat import maps %t (time), %m and %v into IRCafe-compatible placeholders.\n"
                + "Multi-command expansion: separate commands with ';' or new lines."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JPanel behavior =
        PreferencesUiSupport.captionPanel(
            "Behavior",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    behavior.add(controls.unknownCommandAsRaw(), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(behavior, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

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
    aliasList.add(buttons, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    aliasList.add(tableScroll, "grow, push, h 220!, wmin 0");
    panel.add(aliasList, MigLayoutConstraints.GROW_PUSH_WMIN_0_WRAP);

    JPanel editor =
        PreferencesUiSupport.captionPanel(
            "Expansion editor",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "[]6[]");
    editor.add(controls.hint(), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    editor.add(templateScroll, "growx, h 140!, wmin 0, wrap");
    panel.add(editor, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    return panel;
  }
}

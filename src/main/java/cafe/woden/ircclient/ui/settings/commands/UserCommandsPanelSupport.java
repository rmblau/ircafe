package cafe.woden.ircclient.ui.settings.commands;

import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public final class UserCommandsPanelSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private UserCommandsPanelSupport() {}

  public static JPanel buildPanel(UserCommandAliasesControls controls) {
    JPanel panel = new JPanel(MigLayouts.singleColumnFill(12, "[]8[]6[]8[grow,fill]8[]"));

    panel.add(
        PreferencesUiSupport.tabTitle(MESSAGES.text("preferences.commands.title")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.sectionTitle(MESSAGES.text("preferences.commands.aliases.section")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.commands.aliases.help")),
        MigConstraints.growXMinWidth0Wrap());

    JPanel behavior =
        PreferencesUiSupport.captionPanel(MESSAGES.text("preferences.commands.behavior.section"));
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
            MESSAGES.text("preferences.commands.aliases.list.section"),
            MigLayoutConstraints.INSETS_0_FILL_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            MigLayoutConstraints.ROW_6_GROW_FILL);
    aliasList.add(buttons, MigConstraints.growXMinWidth0Wrap());
    aliasList.add(tableScroll, MigConstraints.growPushMinWidth0Height(220));
    panel.add(aliasList, MigConstraints.growPushMinWidth0Wrap());

    JPanel editor =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.commands.aliases.editor.section"),
            MigLayouts.singleColumn(MigLayouts.rows(2, 6)));
    editor.add(controls.hint(), MigConstraints.growXMinWidth0Wrap());
    editor.add(templateScroll, MigConstraints.growXMinWidthHeightWrap(0, 140));
    panel.add(editor, MigConstraints.growXMinWidth0Wrap());

    return panel;
  }
}

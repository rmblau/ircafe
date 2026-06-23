package cafe.woden.ircclient.ui.settings.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.app.commands.UserCommandAliasesPort;
import cafe.woden.ircclient.config.api.UserCommandAliasesConfigPort;
import cafe.woden.ircclient.model.UserCommandAlias;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import org.junit.jupiter.api.Test;

class UserCommandAliasesControlsSupportTest {

  @Test
  void readSettingsSnapshotsAliasesUnknownFallbackAndValidationError() {
    List<UserCommandAlias> aliases =
        List.of(
            new UserCommandAlias(true, "hi", "/msg %1 hi"),
            new UserCommandAlias(true, "hi", "/msg %1 again"));
    UserCommandAliasesControls controls = controls(aliases, true);

    UserCommandAliasesControlsSupport.UserCommandAliasSettings settings =
        UserCommandAliasesControlsSupport.readSettings(controls);

    assertEquals(aliases, settings.aliases());
    assertTrue(settings.unknownCommandAsRawEnabled());
    assertNotNull(settings.validationError());
  }

  @Test
  void rememberSettingsPersistsAliasesAndUpdatesBus() {
    UserCommandAliasesConfigPort runtimeConfig = mock(UserCommandAliasesConfigPort.class);
    UserCommandAliasesPort aliasesBus = mock(UserCommandAliasesPort.class);
    List<UserCommandAlias> aliases = List.of(new UserCommandAlias(true, "hi", "/msg %1 hi"));
    UserCommandAliasesControlsSupport.UserCommandAliasSettings settings =
        new UserCommandAliasesControlsSupport.UserCommandAliasSettings(aliases, true, null);

    UserCommandAliasesControlsSupport.rememberSettings(runtimeConfig, aliasesBus, settings);

    verify(runtimeConfig).rememberUserCommandAliases(aliases);
    verify(runtimeConfig).rememberUnknownCommandAsRawEnabled(true);
    verify(aliasesBus).set(aliases);
    verify(aliasesBus).setUnknownCommandAsRawEnabled(true);
  }

  @Test
  void tableModelTrimsCommandsAndValidationDialogLabels() {
    UserCommandAliasesTableModel model =
        new UserCommandAliasesTableModel(List.of(new UserCommandAlias(true, "/ hi ", "   ")));

    assertEquals("hi", model.getValueAt(0, UserCommandAliasesTableModel.COL_COMMAND));

    UserCommandAliasValidationError error = model.firstValidationError();

    assertNotNull(error);
    assertEquals("Row 1 (/hi):\nEnabled aliases require an expansion.", error.formatForDialog());
  }

  private static UserCommandAliasesControls controls(
      List<UserCommandAlias> aliases, boolean unknownCommandAsRawEnabled) {
    UserCommandAliasesTableModel model = new UserCommandAliasesTableModel(aliases);
    JCheckBox unknownCommandAsRaw = new JCheckBox();
    unknownCommandAsRaw.setSelected(unknownCommandAsRawEnabled);
    return new UserCommandAliasesControls(
        new JTable(model),
        model,
        new JTextArea(),
        unknownCommandAsRaw,
        new JButton(),
        new JButton(),
        new JButton(),
        new JButton(),
        new JButton(),
        new JButton(),
        new JLabel());
  }
}

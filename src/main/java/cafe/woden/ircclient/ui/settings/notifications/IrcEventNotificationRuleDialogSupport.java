package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.notify.api.NotificationSoundPort;
import cafe.woden.ircclient.ui.settings.PathChooserControlsSupport;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.awt.Dimension;
import java.awt.Window;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public final class IrcEventNotificationRuleDialogSupport {
  private IrcEventNotificationRuleDialogSupport() {}

  public static IrcEventNotificationRule promptIrcEventNotificationRuleDialog(
      Window owner,
      String title,
      IrcEventNotificationRule seed,
      NotificationSoundPort notificationSoundService,
      NotificationSoundControlsSupport.SoundFileImporter soundFileImporter) {
    IrcEventNotificationRule base =
        seed != null
            ? seed
            : new IrcEventNotificationRule(
                false,
                IrcEventNotificationRule.EventType.INVITE_RECEIVED,
                IrcEventNotificationRule.SourceMode.ANY,
                null,
                IrcEventNotificationRule.ChannelScope.ALL,
                null,
                true,
                IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY,
                true,
                true,
                false,
                IrcEventNotificationPresetSupport.defaultBuiltInSoundForEvent(
                        IrcEventNotificationRule.EventType.INVITE_RECEIVED)
                    .name(),
                false,
                null,
                false,
                null,
                null,
                null);

    JCheckBox enabled = new JCheckBox("Enabled", base.enabled());

    JComboBox<IrcEventNotificationRule.EventType> eventType =
        new JComboBox<>(IrcEventNotificationRule.EventType.values());
    eventType.setSelectedItem(
        base.eventType() != null
            ? base.eventType()
            : IrcEventNotificationRule.EventType.INVITE_RECEIVED);

    JComboBox<IrcEventNotificationRule.SourceMode> sourceMode =
        new JComboBox<>(IrcEventNotificationRule.SourceMode.values());
    sourceMode.setSelectedItem(
        base.sourceMode() != null ? base.sourceMode() : IrcEventNotificationRule.SourceMode.ANY);

    JTextField sourcePattern = new JTextField(Objects.toString(base.sourcePattern(), ""));
    sourcePattern.setToolTipText(
        "For Specific nicks: comma-separated list.\n"
            + "For Nick glob: wildcard patterns (* and ?).\n"
            + "For Nick regex: Java regular expression.");

    JComboBox<IrcEventNotificationRule.ChannelScope> channelScope =
        new JComboBox<>(IrcEventNotificationRule.ChannelScope.values());
    channelScope.setSelectedItem(
        base.channelScope() != null
            ? base.channelScope()
            : IrcEventNotificationRule.ChannelScope.ALL);

    JTextField channelPatterns = new JTextField(Objects.toString(base.channelPatterns(), ""));
    channelPatterns.setToolTipText("Comma-separated channel masks (for example: #staff*, #ops).");

    JComboBox<IrcEventNotificationRule.CtcpMatchMode> ctcpCommandMode =
        new JComboBox<>(IrcEventNotificationRule.CtcpMatchMode.values());
    ctcpCommandMode.setSelectedItem(
        base.ctcpCommandMode() != null
            ? base.ctcpCommandMode()
            : IrcEventNotificationRule.CtcpMatchMode.ANY);
    JTextField ctcpCommandPattern = new JTextField(Objects.toString(base.ctcpCommandPattern(), ""));
    ctcpCommandPattern.setToolTipText(
        "Filter CTCP command by mode (for example: VERSION, PING, TIME, CLIENTINFO).");

    JComboBox<IrcEventNotificationRule.CtcpMatchMode> ctcpValueMode =
        new JComboBox<>(IrcEventNotificationRule.CtcpMatchMode.values());
    ctcpValueMode.setSelectedItem(
        base.ctcpValueMode() != null
            ? base.ctcpValueMode()
            : IrcEventNotificationRule.CtcpMatchMode.ANY);
    JTextField ctcpValuePattern = new JTextField(Objects.toString(base.ctcpValuePattern(), ""));
    ctcpValuePattern.setToolTipText("Filter CTCP value/argument by mode.");

    JComboBox<CtcpNotificationRuleTemplate> ctcpTemplate =
        new JComboBox<>(CtcpNotificationRuleTemplate.values());
    JButton applyCtcpTemplate =
        PreferencesUiSupport.iconOnlyButton("Apply", "check", "Apply selected CTCP template");

    JCheckBox toastEnabled = new JCheckBox("Desktop toast", base.toastEnabled());

    JComboBox<IrcEventNotificationRule.FocusScope> focusScope =
        new JComboBox<>(IrcEventNotificationRule.FocusScope.values());
    focusScope.setSelectedItem(
        base.focusScope() != null
            ? base.focusScope()
            : IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY);

    JCheckBox statusBarEnabled = new JCheckBox("Status bar message", base.statusBarEnabled());
    JCheckBox notificationsNodeEnabled =
        new JCheckBox("Notifications node entry", base.notificationsNodeEnabled());

    NotificationSoundControlsSupport.Controls soundControls =
        NotificationSoundControlsSupport.buildControls(
            NotificationSoundControlsSupport.Request.builder()
                .enabledLabel("Play sound")
                .enabledSelected(base.soundEnabled())
                .useCustomLabel("Use custom file")
                .useCustomSelected(base.soundUseCustom())
                .soundId(base.soundId())
                .customPath(base.soundCustomPath())
                .browseButtonText("Browse...")
                .clearButtonText("Clear")
                .testButtonText("Test")
                .buttonStyle(NotificationSoundControlsSupport.ButtonStyle.ICON_ONLY)
                .owner(owner)
                .notificationSoundService(notificationSoundService)
                .soundFileImporter(soundFileImporter)
                .customPathEditableWhenEnabled(true)
                .customFileControlsRequireUseCustom(true)
                .build());
    JCheckBox soundEnabled = soundControls.enabled();
    JComboBox<BuiltInSound> builtInSound = soundControls.builtInSound();
    JCheckBox soundUseCustom = soundControls.useCustom();
    JTextField soundCustomPath = soundControls.customPath();
    JButton browseCustomSound = soundControls.browseCustom();
    JButton clearCustomSound = soundControls.clearCustom();
    JButton testSound = soundControls.testSound();

    JCheckBox scriptEnabled = new JCheckBox("Run script/program", base.scriptEnabled());
    PathChooserControlsSupport.Controls scriptPathControls =
        PathChooserControlsSupport.buildControls(
            PathChooserControlsSupport.Request.builder()
                .initialPath(base.scriptPath())
                .browseButtonText("Browse...")
                .clearButtonText("Clear")
                .browseIconName("terminal")
                .browseTooltip("Browse for script/program")
                .clearIconName("close")
                .clearTooltip("Clear script path")
                .chooserDialogTitle("Select script/program")
                .selectionMode(PathChooserControlsSupport.SelectionMode.FILES)
                .owner(owner)
                .availableSupplier(scriptEnabled::isSelected)
                .editableWhenAvailable(true)
                .build());
    JTextField scriptPath = scriptPathControls.path();
    JButton browseScript = scriptPathControls.browseButton();
    JButton clearScript = scriptPathControls.clearButton();

    JTextField scriptArgs = new JTextField(Objects.toString(base.scriptArgs(), ""));
    PathChooserControlsSupport.Controls scriptWorkingDirectoryControls =
        PathChooserControlsSupport.buildControls(
            PathChooserControlsSupport.Request.builder()
                .initialPath(base.scriptWorkingDirectory())
                .browseButtonText("Browse...")
                .clearButtonText("Clear")
                .browseIconName("settings")
                .browseTooltip("Browse for script working directory")
                .clearIconName("close")
                .clearTooltip("Clear script working directory")
                .chooserDialogTitle("Select script working directory")
                .selectionMode(PathChooserControlsSupport.SelectionMode.DIRECTORIES)
                .owner(owner)
                .availableSupplier(scriptEnabled::isSelected)
                .editableWhenAvailable(true)
                .build());
    JTextField scriptWorkingDirectory = scriptWorkingDirectoryControls.path();
    JButton browseScriptWorkingDirectory = scriptWorkingDirectoryControls.browseButton();
    JButton clearScriptWorkingDirectory = scriptWorkingDirectoryControls.clearButton();

    Runnable refreshSourceFieldState =
        () -> {
          IrcEventNotificationRule.SourceMode mode =
              PreferencesUiSupport.selectedComboItem(
                  sourceMode,
                  IrcEventNotificationRule.SourceMode.class,
                  IrcEventNotificationRule.SourceMode.ANY);
          boolean needsPattern =
              mode == IrcEventNotificationRule.SourceMode.NICK_LIST
                  || mode == IrcEventNotificationRule.SourceMode.GLOB
                  || mode == IrcEventNotificationRule.SourceMode.REGEX;
          PreferencesUiSupport.setTextInputAvailable(sourcePattern, needsPattern);
          String placeholder =
              switch (mode) {
                case NICK_LIST -> "alice, bob";
                case GLOB -> "op*, admin?";
                case REGEX -> "^op[0-9]+$";
                default -> "";
              };
          PreferencesUiSupport.placeholder(sourcePattern, placeholder);
        };

    Runnable refreshChannelFieldState =
        () -> {
          IrcEventNotificationRule.ChannelScope scope =
              PreferencesUiSupport.selectedComboItem(
                  channelScope,
                  IrcEventNotificationRule.ChannelScope.class,
                  IrcEventNotificationRule.ChannelScope.ALL);
          boolean needsPattern =
              scope == IrcEventNotificationRule.ChannelScope.ONLY
                  || scope == IrcEventNotificationRule.ChannelScope.ALL_EXCEPT;
          PreferencesUiSupport.setTextInputAvailable(channelPatterns, needsPattern);
          PreferencesUiSupport.placeholder(channelPatterns, needsPattern ? "#staff*, #ops" : "");
        };

    Runnable refreshCtcpFieldState =
        () -> {
          IrcEventNotificationRule.EventType selectedEvent =
              PreferencesUiSupport.selectedComboItem(
                  eventType,
                  IrcEventNotificationRule.EventType.class,
                  IrcEventNotificationRule.EventType.INVITE_RECEIVED);
          boolean ctcp = selectedEvent == IrcEventNotificationRule.EventType.CTCP_RECEIVED;

          IrcEventNotificationRule.CtcpMatchMode selectedCommandMode =
              PreferencesUiSupport.selectedComboItem(
                  ctcpCommandMode,
                  IrcEventNotificationRule.CtcpMatchMode.class,
                  IrcEventNotificationRule.CtcpMatchMode.ANY);
          boolean commandNeedsPattern =
              ctcp && selectedCommandMode != IrcEventNotificationRule.CtcpMatchMode.ANY;
          ctcpCommandMode.setEnabled(ctcp);
          PreferencesUiSupport.setTextInputAvailable(ctcpCommandPattern, commandNeedsPattern);
          PreferencesUiSupport.placeholder(
              ctcpCommandPattern, commandNeedsPattern ? "VERSION / PING / TIME / CLIENTINFO" : "");

          IrcEventNotificationRule.CtcpMatchMode selectedValueMode =
              PreferencesUiSupport.selectedComboItem(
                  ctcpValueMode,
                  IrcEventNotificationRule.CtcpMatchMode.class,
                  IrcEventNotificationRule.CtcpMatchMode.ANY);
          boolean valueNeedsPattern =
              ctcp && selectedValueMode != IrcEventNotificationRule.CtcpMatchMode.ANY;
          ctcpValueMode.setEnabled(ctcp);
          PreferencesUiSupport.setTextInputAvailable(ctcpValuePattern, valueNeedsPattern);
          PreferencesUiSupport.placeholder(
              ctcpValuePattern, valueNeedsPattern ? "argument pattern" : "");

          ctcpTemplate.setEnabled(ctcp);
          applyCtcpTemplate.setEnabled(ctcp);
        };

    Runnable refreshScriptState =
        () -> {
          boolean run = scriptEnabled.isSelected();
          scriptPathControls.refresh();
          PreferencesUiSupport.setTextInputAvailable(scriptArgs, run);
          scriptWorkingDirectoryControls.refresh();
        };

    final IrcEventNotificationRule.EventType[] priorEvent =
        new IrcEventNotificationRule.EventType[] {
          PreferencesUiSupport.selectedComboItem(
              eventType,
              IrcEventNotificationRule.EventType.class,
              IrcEventNotificationRule.EventType.INVITE_RECEIVED)
        };

    eventType.addActionListener(
        e -> {
          IrcEventNotificationRule.EventType selectedEvent =
              PreferencesUiSupport.selectedComboItem(
                  eventType,
                  IrcEventNotificationRule.EventType.class,
                  IrcEventNotificationRule.EventType.INVITE_RECEIVED);
          IrcEventNotificationRule.EventType previous = priorEvent[0];
          if (previous == null) previous = IrcEventNotificationRule.EventType.INVITE_RECEIVED;
          if (!soundUseCustom.isSelected()) {
            Object selectedSound = builtInSound.getSelectedItem();
            if (selectedSound instanceof BuiltInSound currentSound) {
              BuiltInSound previousDefault =
                  IrcEventNotificationPresetSupport.defaultBuiltInSoundForEvent(previous);
              if (currentSound == previousDefault) {
                builtInSound.setSelectedItem(
                    IrcEventNotificationPresetSupport.defaultBuiltInSoundForEvent(selectedEvent));
              }
            }
          }
          priorEvent[0] = selectedEvent;
          refreshCtcpFieldState.run();
        });

    sourceMode.addActionListener(e -> refreshSourceFieldState.run());
    channelScope.addActionListener(e -> refreshChannelFieldState.run());
    ctcpCommandMode.addActionListener(e -> refreshCtcpFieldState.run());
    ctcpValueMode.addActionListener(e -> refreshCtcpFieldState.run());
    scriptEnabled.addActionListener(e -> refreshScriptState.run());

    applyCtcpTemplate.addActionListener(
        e -> {
          CtcpNotificationRuleTemplate template =
              PreferencesUiSupport.selectedComboItem(
                  ctcpTemplate,
                  CtcpNotificationRuleTemplate.class,
                  CtcpNotificationRuleTemplate.CUSTOM);
          eventType.setSelectedItem(IrcEventNotificationRule.EventType.CTCP_RECEIVED);
          if (template == CtcpNotificationRuleTemplate.CUSTOM) {
            ctcpCommandMode.setSelectedItem(IrcEventNotificationRule.CtcpMatchMode.ANY);
            ctcpCommandPattern.setText("");
            ctcpValueMode.setSelectedItem(IrcEventNotificationRule.CtcpMatchMode.ANY);
            ctcpValuePattern.setText("");
            refreshCtcpFieldState.run();
            return;
          }
          ctcpCommandMode.setSelectedItem(IrcEventNotificationRule.CtcpMatchMode.LIKE);
          ctcpCommandPattern.setText(template.command());
          ctcpValueMode.setSelectedItem(IrcEventNotificationRule.CtcpMatchMode.ANY);
          ctcpValuePattern.setText("");
          refreshCtcpFieldState.run();
        });

    refreshSourceFieldState.run();
    refreshChannelFieldState.run();
    refreshCtcpFieldState.run();
    soundControls.refresh();
    refreshScriptState.run();

    JPanel filtersPanel =
        new JPanel(MigLayouts.twoColumnFormWithHideMode(10, 8, 3, MigLayouts.rows(9, 6)));
    filtersPanel.add(enabled, MigConstraints.spanXWrap(2));
    filtersPanel.add(new JLabel("Event"));
    filtersPanel.add(eventType, MigConstraints.growXMinWidthWrap(220));
    filtersPanel.add(new JLabel("Source"));
    filtersPanel.add(sourceMode, MigConstraints.growXWrap());
    filtersPanel.add(new JLabel("Source match"));
    filtersPanel.add(sourcePattern, MigConstraints.growXWrap());
    filtersPanel.add(new JLabel("Channel scope"));
    filtersPanel.add(channelScope, MigConstraints.growXWrap());
    filtersPanel.add(new JLabel("Channels"));
    filtersPanel.add(channelPatterns, MigConstraints.growXWrap());

    JPanel ctcpCommandRow = new JPanel(MigLayouts.fillX("[pref!]8[grow,fill]", "[]"));
    ctcpCommandRow.add(ctcpCommandMode, MigConstraints.width(110));
    ctcpCommandRow.add(ctcpCommandPattern, MigConstraints.growXPushXMinWidth0());
    filtersPanel.add(new JLabel("CTCP command"));
    filtersPanel.add(ctcpCommandRow, MigConstraints.growXMinWidth0Wrap());

    JPanel ctcpValueRow = new JPanel(MigLayouts.fillX("[pref!]8[grow,fill]", "[]"));
    ctcpValueRow.add(ctcpValueMode, MigConstraints.width(110));
    ctcpValueRow.add(ctcpValuePattern, MigConstraints.growXPushXMinWidth0());
    filtersPanel.add(new JLabel("CTCP value"));
    filtersPanel.add(ctcpValueRow, MigConstraints.growXMinWidth0Wrap());

    JPanel ctcpTemplateRow = new JPanel(MigLayouts.fillX("[grow,fill]8[]", "[]"));
    ctcpTemplateRow.add(ctcpTemplate, MigConstraints.growXPushXMinWidth0());
    ctcpTemplateRow.add(applyCtcpTemplate, MigConstraints.widthHeight(36, 28));
    filtersPanel.add(new JLabel("CTCP template"));
    filtersPanel.add(ctcpTemplateRow, MigConstraints.growXMinWidth0Wrap());
    filtersPanel.add(new JLabel(""));
    filtersPanel.add(
        PreferencesUiSupport.helpText(
            "Active channel only means the event target must match the currently selected channel on the same server.\n"
                + "CTCP command/value filters only apply when Event is CTCP Request Received."),
        MigConstraints.growXMinWidth0Wrap());

    JPanel actionsPanel =
        new JPanel(MigLayouts.twoColumnFormWithHideMode(10, 8, 3, MigLayouts.rows(3, 6)));
    actionsPanel.add(toastEnabled, MigConstraints.span2GrowXWrap());
    actionsPanel.add(new JLabel("Toast focus"));
    actionsPanel.add(focusScope, MigConstraints.growXWrap());
    actionsPanel.add(statusBarEnabled, MigConstraints.span2GrowXWrap());
    actionsPanel.add(notificationsNodeEnabled, MigConstraints.span2GrowXWrap());
    actionsPanel.add(new JLabel(""));
    actionsPanel.add(
        PreferencesUiSupport.helpText(
            "Tip: combine multiple rules for the same event to split foreground/background behavior."),
        MigConstraints.growXMinWidth0Wrap());

    JPanel soundPanel =
        new JPanel(
            MigLayouts.labelFieldActionsFormWithHideMode(10, 8, 2, 3, MigLayouts.rowGaps(6, 4)));
    soundPanel.add(soundEnabled, MigConstraints.spanXGrowXWrap(4));
    soundPanel.add(new JLabel("Built-in"));
    soundPanel.add(builtInSound, MigConstraints.growXMinWidth(180));
    soundPanel.add(testSound, MigConstraints.widthHeight(36, 28));
    soundPanel.add(soundUseCustom, MigConstraints.wrap());
    soundPanel.add(new JLabel("Custom file"));
    soundPanel.add(soundCustomPath, MigConstraints.growXPushXMinWidth0());
    soundPanel.add(browseCustomSound, MigConstraints.widthHeight(36, 28));
    soundPanel.add(clearCustomSound, MigConstraints.widthHeightWrap(36, 28));
    soundPanel.add(new JLabel(""));
    soundPanel.add(
        PreferencesUiSupport.helpText(
            "When Sound is disabled on a rule, no sound is played for that event."),
        MigConstraints.spanXGrowXMinWidthWrap(3, 0));

    JPanel scriptPanel =
        new JPanel(
            MigLayouts.labelFieldActionsFormWithHideMode(10, 8, 2, 3, MigLayouts.rowGaps(6, 4)));
    scriptPanel.add(scriptEnabled, MigConstraints.spanXGrowXWrap(4));
    scriptPanel.add(new JLabel("Script path"));
    scriptPanel.add(scriptPath, MigConstraints.growXPushXMinWidth0());
    scriptPanel.add(browseScript, MigConstraints.widthHeight(36, 28));
    scriptPanel.add(clearScript, MigConstraints.widthHeightWrap(36, 28));
    scriptPanel.add(new JLabel("Arguments"));
    scriptPanel.add(scriptArgs, MigConstraints.spanXGrowXMinWidthWrap(3, 0));
    scriptPanel.add(new JLabel("Working dir"));
    scriptPanel.add(scriptWorkingDirectory, MigConstraints.growXPushXMinWidth0());
    scriptPanel.add(browseScriptWorkingDirectory, MigConstraints.widthHeight(36, 28));
    scriptPanel.add(clearScriptWorkingDirectory, MigConstraints.widthHeightWrap(36, 28));
    scriptPanel.add(new JLabel(""));
    scriptPanel.add(
        PreferencesUiSupport.helpText(
            "If enabled, IRCafe executes the script and sets env vars:\n"
                + "IRCAFE_EVENT_TYPE, IRCAFE_SERVER_ID, IRCAFE_CHANNEL, IRCAFE_SOURCE_NICK,\n"
                + "IRCAFE_SOURCE_IS_SELF, IRCAFE_TITLE, IRCAFE_BODY,\n"
                + "IRCAFE_CTCP_COMMAND, IRCAFE_CTCP_VALUE, IRCAFE_TIMESTAMP_MS.\n"
                + "Arguments support quotes/escapes and are passed directly (no shell expansion)."),
        MigConstraints.spanXGrowXMinWidthWrap(3, 0));

    JTabbedPane tabs = new JTabbedPane();
    tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabs.addTab("Filters", filtersPanel);
    tabs.addTab("Actions", actionsPanel);
    tabs.addTab("Sound", soundPanel);
    tabs.addTab("Script", scriptPanel);
    tabs.setPreferredSize(new Dimension(640, 420));

    JPanel form =
        new JPanel(
            MigLayouts.fillWrap(
                0, 1, MigLayoutConstraints.GROW_FILL, MigLayoutConstraints.GROW_FILL));
    form.add(tabs, MigConstraints.growPushMinWidth0());

    String dialogTitle = Objects.toString(title, "IRC Event Rule");
    while (true) {
      if (!PreferencesUiSupport.confirmPlainOkCancel(owner, form, dialogTitle)) return null;

      IrcEventNotificationRule.EventType selectedEvent =
          PreferencesUiSupport.selectedComboItem(
              eventType,
              IrcEventNotificationRule.EventType.class,
              IrcEventNotificationRule.EventType.INVITE_RECEIVED);
      IrcEventNotificationRule.SourceMode selectedSourceMode =
          PreferencesUiSupport.selectedComboItem(
              sourceMode,
              IrcEventNotificationRule.SourceMode.class,
              IrcEventNotificationRule.SourceMode.ANY);
      IrcEventNotificationRule.ChannelScope selectedChannelScope =
          PreferencesUiSupport.selectedComboItem(
              channelScope,
              IrcEventNotificationRule.ChannelScope.class,
              IrcEventNotificationRule.ChannelScope.ALL);
      IrcEventNotificationRule.FocusScope selectedFocusScope =
          PreferencesUiSupport.selectedComboItem(
              focusScope,
              IrcEventNotificationRule.FocusScope.class,
              IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY);

      String sourcePatternValue = PreferencesUiSupport.trimmedTextOrNull(sourcePattern);
      boolean sourceNeedsPattern =
          selectedSourceMode == IrcEventNotificationRule.SourceMode.NICK_LIST
              || selectedSourceMode == IrcEventNotificationRule.SourceMode.GLOB
              || selectedSourceMode == IrcEventNotificationRule.SourceMode.REGEX;
      if (sourceNeedsPattern && sourcePatternValue == null) {
        showInvalidRuleMessage(
            owner,
            tabs,
            0,
            "Source mode \"" + selectedSourceMode + "\" requires a source pattern.");
        continue;
      }
      if (selectedSourceMode == IrcEventNotificationRule.SourceMode.REGEX
          && sourcePatternValue != null) {
        try {
          Pattern.compile(sourcePatternValue);
        } catch (Exception ex) {
          showInvalidRuleMessage(
              owner,
              tabs,
              0,
              "Invalid source regex pattern:\n"
                  + Objects.toString(ex.getMessage(), "Invalid regex"));
          continue;
        }
      }
      if (!sourceNeedsPattern) sourcePatternValue = null;

      String channelPatternsValue = PreferencesUiSupport.trimmedTextOrNull(channelPatterns);
      boolean channelNeedsPattern =
          selectedChannelScope == IrcEventNotificationRule.ChannelScope.ONLY
              || selectedChannelScope == IrcEventNotificationRule.ChannelScope.ALL_EXCEPT;
      if (channelNeedsPattern && channelPatternsValue == null) {
        showInvalidRuleMessage(
            owner,
            tabs,
            0,
            "Channel scope \"" + selectedChannelScope + "\" requires channel patterns.");
        continue;
      }
      if (!channelNeedsPattern) channelPatternsValue = null;

      IrcEventNotificationRule.CtcpMatchMode selectedCtcpCommandMode =
          PreferencesUiSupport.selectedComboItem(
              ctcpCommandMode,
              IrcEventNotificationRule.CtcpMatchMode.class,
              IrcEventNotificationRule.CtcpMatchMode.ANY);
      IrcEventNotificationRule.CtcpMatchMode selectedCtcpValueMode =
          PreferencesUiSupport.selectedComboItem(
              ctcpValueMode,
              IrcEventNotificationRule.CtcpMatchMode.class,
              IrcEventNotificationRule.CtcpMatchMode.ANY);
      String ctcpCommandPatternValue = PreferencesUiSupport.trimmedTextOrNull(ctcpCommandPattern);
      String ctcpValuePatternValue = PreferencesUiSupport.trimmedTextOrNull(ctcpValuePattern);

      boolean ctcpEvent = selectedEvent == IrcEventNotificationRule.EventType.CTCP_RECEIVED;
      if (ctcpEvent && selectedCtcpCommandMode != IrcEventNotificationRule.CtcpMatchMode.ANY) {
        if (ctcpCommandPatternValue == null) {
          showInvalidRuleMessage(
              owner,
              tabs,
              0,
              "CTCP command mode \"" + selectedCtcpCommandMode + "\" requires a pattern.");
          continue;
        }
        if (selectedCtcpCommandMode == IrcEventNotificationRule.CtcpMatchMode.REGEX) {
          try {
            Pattern.compile(ctcpCommandPatternValue);
          } catch (Exception ex) {
            showInvalidRuleMessage(
                owner,
                tabs,
                0,
                "Invalid CTCP command regex pattern:\n"
                    + Objects.toString(ex.getMessage(), "Invalid regex"));
            continue;
          }
        }
      }
      if (ctcpEvent && selectedCtcpValueMode != IrcEventNotificationRule.CtcpMatchMode.ANY) {
        if (ctcpValuePatternValue == null) {
          showInvalidRuleMessage(
              owner,
              tabs,
              0,
              "CTCP value mode \"" + selectedCtcpValueMode + "\" requires a pattern.");
          continue;
        }
        if (selectedCtcpValueMode == IrcEventNotificationRule.CtcpMatchMode.REGEX) {
          try {
            Pattern.compile(ctcpValuePatternValue);
          } catch (Exception ex) {
            showInvalidRuleMessage(
                owner,
                tabs,
                0,
                "Invalid CTCP value regex pattern:\n"
                    + Objects.toString(ex.getMessage(), "Invalid regex"));
            continue;
          }
        }
      }
      if (!ctcpEvent) {
        selectedCtcpCommandMode = IrcEventNotificationRule.CtcpMatchMode.ANY;
        ctcpCommandPatternValue = null;
        selectedCtcpValueMode = IrcEventNotificationRule.CtcpMatchMode.ANY;
        ctcpValuePatternValue = null;
      }

      BuiltInSound selectedSound =
          PreferencesUiSupport.selectedComboItem(
              builtInSound,
              BuiltInSound.class,
              IrcEventNotificationPresetSupport.defaultBuiltInSoundForEvent(selectedEvent));
      String soundCustomPathValue = PreferencesUiSupport.trimmedTextOrNull(soundCustomPath);
      boolean useCustomSound = soundUseCustom.isSelected() && soundCustomPathValue != null;

      String scriptPathValue = PreferencesUiSupport.trimmedTextOrNull(scriptPath);
      String scriptArgsValue = PreferencesUiSupport.trimmedTextOrNull(scriptArgs);
      String scriptWorkingDirectoryValue =
          PreferencesUiSupport.trimmedTextOrNull(scriptWorkingDirectory);
      boolean runScript = scriptEnabled.isSelected();
      if (runScript && scriptPathValue == null) {
        showInvalidRuleMessage(
            owner, tabs, 3, "Script path is required when Run script/program is enabled.");
        continue;
      }

      return new IrcEventNotificationRule(
          enabled.isSelected(),
          selectedEvent,
          selectedSourceMode,
          sourcePatternValue,
          selectedChannelScope,
          channelPatternsValue,
          toastEnabled.isSelected(),
          selectedFocusScope,
          statusBarEnabled.isSelected(),
          notificationsNodeEnabled.isSelected(),
          soundEnabled.isSelected(),
          selectedSound.name(),
          useCustomSound,
          soundCustomPathValue,
          runScript,
          scriptPathValue,
          scriptArgsValue,
          scriptWorkingDirectoryValue,
          selectedCtcpCommandMode,
          ctcpCommandPatternValue,
          selectedCtcpValueMode,
          ctcpValuePatternValue);
    }
  }

  private static void showInvalidRuleMessage(
      Window owner, JTabbedPane tabs, int tabIndex, String message) {
    PreferencesUiSupport.showErrorMessage(owner, message, "Invalid IRC Event Rule");
    tabs.setSelectedIndex(tabIndex);
  }

  private enum CtcpNotificationRuleTemplate {
    CUSTOM("Custom", null),
    VERSION("VERSION request", "VERSION"),
    PING("PING request", "PING"),
    TIME("TIME request", "TIME"),
    CLIENTINFO("CLIENTINFO request", "CLIENTINFO"),
    SOURCE("SOURCE request", "SOURCE"),
    USERINFO("USERINFO request", "USERINFO");

    private final String label;
    private final String command;

    CtcpNotificationRuleTemplate(String label, String command) {
      this.label = label;
      this.command = command;
    }

    String command() {
      return command;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}

package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.notify.api.NotificationSoundPort;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationCtcpTemplatePlan;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationCtcpTemplatePlanner;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditFieldPlan;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditFieldPlanner;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditPolicy;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditSubmissionPlan;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditSubmissionPlanner;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditValidationDisplayPlanner;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditValidationError;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditValues;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationSoundSelectionPlan;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationSoundSelectionPlanner;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PathChooserControlsSupport;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.awt.Dimension;
import java.awt.Window;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public final class IrcEventNotificationRuleDialogSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private IrcEventNotificationRuleDialogSupport() {}

  public static IrcEventNotificationRule promptIrcEventNotificationRuleDialog(
      Window owner,
      String title,
      IrcEventNotificationRule seed,
      NotificationSoundPort notificationSoundService,
      NotificationSoundControlsSupport.SoundFileImporter soundFileImporter) {
    IrcEventNotificationRule base = seedFromPlan(seedPlan(seed));

    JCheckBox enabled =
        new JCheckBox(
            MESSAGES.text("preferences.notifications.ircEvents.dialog.enabled"), base.enabled());

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
        MESSAGES.text("preferences.notifications.ircEvents.dialog.sourceMatch.tooltip"));

    JComboBox<IrcEventNotificationRule.ChannelScope> channelScope =
        new JComboBox<>(IrcEventNotificationRule.ChannelScope.values());
    channelScope.setSelectedItem(
        base.channelScope() != null
            ? base.channelScope()
            : IrcEventNotificationRule.ChannelScope.ALL);

    JTextField channelPatterns = new JTextField(Objects.toString(base.channelPatterns(), ""));
    channelPatterns.setToolTipText(
        MESSAGES.text("preferences.notifications.ircEvents.dialog.channels.tooltip"));

    JComboBox<IrcEventNotificationRule.CtcpMatchMode> ctcpCommandMode =
        new JComboBox<>(IrcEventNotificationRule.CtcpMatchMode.values());
    ctcpCommandMode.setSelectedItem(
        base.ctcpCommandMode() != null
            ? base.ctcpCommandMode()
            : IrcEventNotificationRule.CtcpMatchMode.ANY);
    JTextField ctcpCommandPattern = new JTextField(Objects.toString(base.ctcpCommandPattern(), ""));
    ctcpCommandPattern.setToolTipText(
        MESSAGES.text("preferences.notifications.ircEvents.dialog.ctcpCommand.tooltip"));

    JComboBox<IrcEventNotificationRule.CtcpMatchMode> ctcpValueMode =
        new JComboBox<>(IrcEventNotificationRule.CtcpMatchMode.values());
    ctcpValueMode.setSelectedItem(
        base.ctcpValueMode() != null
            ? base.ctcpValueMode()
            : IrcEventNotificationRule.CtcpMatchMode.ANY);
    JTextField ctcpValuePattern = new JTextField(Objects.toString(base.ctcpValuePattern(), ""));
    ctcpValuePattern.setToolTipText(
        MESSAGES.text("preferences.notifications.ircEvents.dialog.ctcpValue.tooltip"));

    JComboBox<CtcpNotificationRuleTemplate> ctcpTemplate =
        new JComboBox<>(CtcpNotificationRuleTemplate.values());
    JButton applyCtcpTemplate =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("common.button.apply"),
            "check",
            MESSAGES.text("preferences.notifications.ircEvents.dialog.ctcpTemplate.apply.tooltip"));

    JCheckBox toastEnabled =
        new JCheckBox(
            MESSAGES.text("preferences.notifications.ircEvents.dialog.toast"), base.toastEnabled());

    JComboBox<IrcEventNotificationRule.FocusScope> focusScope =
        new JComboBox<>(IrcEventNotificationRule.FocusScope.values());
    focusScope.setSelectedItem(
        base.focusScope() != null
            ? base.focusScope()
            : IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY);

    JCheckBox statusBarEnabled =
        new JCheckBox(
            MESSAGES.text("preferences.notifications.ircEvents.dialog.statusBar"),
            base.statusBarEnabled());
    JCheckBox notificationsNodeEnabled =
        new JCheckBox(
            MESSAGES.text("preferences.notifications.ircEvents.dialog.notificationsNode"),
            base.notificationsNodeEnabled());

    NotificationSoundControlsSupport.Controls soundControls =
        NotificationSoundControlsSupport.buildControls(
            NotificationSoundControlsSupport.Request.builder()
                .enabledLabel(
                    MESSAGES.text("preferences.notifications.ircEvents.dialog.sound.enabled"))
                .enabledSelected(base.soundEnabled())
                .useCustomLabel(
                    MESSAGES.text("preferences.notifications.ircEvents.dialog.sound.useCustom"))
                .useCustomSelected(base.soundUseCustom())
                .soundId(base.soundId())
                .customPath(base.soundCustomPath())
                .browseButtonText(MESSAGES.text("common.button.browse.ellipsis"))
                .clearButtonText(MESSAGES.text("common.button.clear"))
                .testButtonText(
                    MESSAGES.text("preferences.notifications.ircEvents.dialog.sound.test"))
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

    JCheckBox scriptEnabled =
        new JCheckBox(
            MESSAGES.text("preferences.notifications.ircEvents.dialog.script.enabled"),
            base.scriptEnabled());
    PathChooserControlsSupport.Controls scriptPathControls =
        PathChooserControlsSupport.buildControls(
            PathChooserControlsSupport.Request.builder()
                .initialPath(base.scriptPath())
                .browseButtonText(MESSAGES.text("common.button.browse.ellipsis"))
                .clearButtonText(MESSAGES.text("common.button.clear"))
                .browseIconName("terminal")
                .browseTooltip(
                    MESSAGES.text(
                        "preferences.notifications.ircEvents.dialog.script.path.browse.tooltip"))
                .clearIconName("close")
                .clearTooltip(
                    MESSAGES.text(
                        "preferences.notifications.ircEvents.dialog.script.path.clear.tooltip"))
                .chooserDialogTitle(
                    MESSAGES.text("preferences.notifications.ircEvents.dialog.script.path.chooser"))
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
                .browseButtonText(MESSAGES.text("common.button.browse.ellipsis"))
                .clearButtonText(MESSAGES.text("common.button.clear"))
                .browseIconName("settings")
                .browseTooltip(
                    MESSAGES.text(
                        "preferences.notifications.ircEvents.dialog.script.workingDirectory.browse.tooltip"))
                .clearIconName("close")
                .clearTooltip(
                    MESSAGES.text(
                        "preferences.notifications.ircEvents.dialog.script.workingDirectory.clear.tooltip"))
                .chooserDialogTitle(
                    MESSAGES.text(
                        "preferences.notifications.ircEvents.dialog.script.workingDirectory.chooser"))
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
          IrcEventNotificationRuleEditFieldPlan plan =
              currentEditFieldPlan(
                  eventType,
                  sourceMode,
                  channelScope,
                  ctcpCommandMode,
                  ctcpValueMode,
                  scriptEnabled.isSelected());
          PreferencesUiSupport.setTextInputAvailable(sourcePattern, plan.sourcePatternAvailable());
          PreferencesUiSupport.placeholder(
              sourcePattern, sourcePlaceholder(plan.sourcePatternHint()));
        };

    Runnable refreshChannelFieldState =
        () -> {
          IrcEventNotificationRuleEditFieldPlan plan =
              currentEditFieldPlan(
                  eventType,
                  sourceMode,
                  channelScope,
                  ctcpCommandMode,
                  ctcpValueMode,
                  scriptEnabled.isSelected());
          PreferencesUiSupport.setTextInputAvailable(
              channelPatterns, plan.channelPatternsAvailable());
          PreferencesUiSupport.placeholder(
              channelPatterns,
              plan.channelPatternsAvailable()
                  ? MESSAGES.text("preferences.notifications.ircEvents.dialog.placeholder.channels")
                  : "");
        };

    Runnable refreshCtcpFieldState =
        () -> {
          IrcEventNotificationRuleEditFieldPlan plan =
              currentEditFieldPlan(
                  eventType,
                  sourceMode,
                  channelScope,
                  ctcpCommandMode,
                  ctcpValueMode,
                  scriptEnabled.isSelected());
          ctcpCommandMode.setEnabled(plan.ctcpFiltersAvailable());
          PreferencesUiSupport.setTextInputAvailable(
              ctcpCommandPattern, plan.ctcpCommandPatternAvailable());
          PreferencesUiSupport.placeholder(
              ctcpCommandPattern,
              plan.ctcpCommandPatternAvailable()
                  ? MESSAGES.text(
                      "preferences.notifications.ircEvents.dialog.placeholder.ctcpCommand")
                  : "");

          ctcpValueMode.setEnabled(plan.ctcpFiltersAvailable());
          PreferencesUiSupport.setTextInputAvailable(
              ctcpValuePattern, plan.ctcpValuePatternAvailable());
          PreferencesUiSupport.placeholder(
              ctcpValuePattern,
              plan.ctcpValuePatternAvailable()
                  ? MESSAGES.text(
                      "preferences.notifications.ircEvents.dialog.placeholder.ctcpValue")
                  : "");

          ctcpTemplate.setEnabled(plan.ctcpFiltersAvailable());
          applyCtcpTemplate.setEnabled(plan.ctcpFiltersAvailable());
        };

    Runnable refreshScriptState =
        () -> {
          IrcEventNotificationRuleEditFieldPlan plan =
              currentEditFieldPlan(
                  eventType,
                  sourceMode,
                  channelScope,
                  ctcpCommandMode,
                  ctcpValueMode,
                  scriptEnabled.isSelected());
          scriptPathControls.refresh();
          PreferencesUiSupport.setTextInputAvailable(scriptArgs, plan.scriptFieldsAvailable());
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
          Object selectedSound = builtInSound.getSelectedItem();
          if (selectedSound instanceof BuiltInSound currentSound) {
            IrcEventNotificationSoundSelectionPlan plan =
                IrcEventNotificationSoundSelectionPlanner.planDefaultSoundForEventChange(
                    enumName(previous),
                    enumName(selectedEvent),
                    currentSound.name(),
                    soundUseCustom.isSelected());
            if (plan.updateBuiltInSound()) {
              builtInSound.setSelectedItem(BuiltInSound.fromId(plan.soundId()));
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
          IrcEventNotificationCtcpTemplatePlan plan =
              IrcEventNotificationCtcpTemplatePlanner.plan(template.templateId());
          eventType.setSelectedItem(
              eventTypeValue(plan.eventType(), IrcEventNotificationRule.EventType.CTCP_RECEIVED));
          ctcpCommandMode.setSelectedItem(
              ctcpMatchModeValue(
                  plan.ctcpCommandMode(), IrcEventNotificationRule.CtcpMatchMode.ANY));
          ctcpCommandPattern.setText(plan.ctcpCommandPattern());
          ctcpValueMode.setSelectedItem(
              ctcpMatchModeValue(plan.ctcpValueMode(), IrcEventNotificationRule.CtcpMatchMode.ANY));
          ctcpValuePattern.setText(plan.ctcpValuePattern());
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
    filtersPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.ircEvents.dialog.field.event")));
    filtersPanel.add(eventType, MigConstraints.growXMinWidthWrap(220));
    filtersPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.ircEvents.dialog.field.source")));
    filtersPanel.add(sourceMode, MigConstraints.growXWrap());
    filtersPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.ircEvents.dialog.field.sourceMatch")));
    filtersPanel.add(sourcePattern, MigConstraints.growXWrap());
    filtersPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.ircEvents.dialog.field.channelScope")));
    filtersPanel.add(channelScope, MigConstraints.growXWrap());
    filtersPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.ircEvents.dialog.field.channels")));
    filtersPanel.add(channelPatterns, MigConstraints.growXWrap());

    JPanel ctcpCommandRow = new JPanel(MigLayouts.fillX("[pref!]8[grow,fill]", "[]"));
    ctcpCommandRow.add(ctcpCommandMode, MigConstraints.width(110));
    ctcpCommandRow.add(ctcpCommandPattern, MigConstraints.growXPushXMinWidth0());
    filtersPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.ircEvents.dialog.field.ctcpCommand")));
    filtersPanel.add(ctcpCommandRow, MigConstraints.growXMinWidth0Wrap());

    JPanel ctcpValueRow = new JPanel(MigLayouts.fillX("[pref!]8[grow,fill]", "[]"));
    ctcpValueRow.add(ctcpValueMode, MigConstraints.width(110));
    ctcpValueRow.add(ctcpValuePattern, MigConstraints.growXPushXMinWidth0());
    filtersPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.ircEvents.dialog.field.ctcpValue")));
    filtersPanel.add(ctcpValueRow, MigConstraints.growXMinWidth0Wrap());

    JPanel ctcpTemplateRow = new JPanel(MigLayouts.fillX("[grow,fill]8[]", "[]"));
    ctcpTemplateRow.add(ctcpTemplate, MigConstraints.growXPushXMinWidth0());
    ctcpTemplateRow.add(applyCtcpTemplate, MigConstraints.widthHeight(36, 28));
    filtersPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.ircEvents.dialog.field.ctcpTemplate")));
    filtersPanel.add(ctcpTemplateRow, MigConstraints.growXMinWidth0Wrap());
    filtersPanel.add(new JLabel(""));
    filtersPanel.add(
        PreferencesUiSupport.helpText(
            MESSAGES.text("preferences.notifications.ircEvents.dialog.filters.help")),
        MigConstraints.growXMinWidth0Wrap());

    JPanel actionsPanel =
        new JPanel(MigLayouts.twoColumnFormWithHideMode(10, 8, 3, MigLayouts.rows(3, 6)));
    actionsPanel.add(toastEnabled, MigConstraints.span2GrowXWrap());
    actionsPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.ircEvents.dialog.field.toastFocus")));
    actionsPanel.add(focusScope, MigConstraints.growXWrap());
    actionsPanel.add(statusBarEnabled, MigConstraints.span2GrowXWrap());
    actionsPanel.add(notificationsNodeEnabled, MigConstraints.span2GrowXWrap());
    actionsPanel.add(new JLabel(""));
    actionsPanel.add(
        PreferencesUiSupport.helpText(
            MESSAGES.text("preferences.notifications.ircEvents.dialog.actions.help")),
        MigConstraints.growXMinWidth0Wrap());

    JPanel soundPanel =
        new JPanel(
            MigLayouts.labelFieldActionsFormWithHideMode(10, 8, 2, 3, MigLayouts.rowGaps(6, 4)));
    soundPanel.add(soundEnabled, MigConstraints.spanXGrowXWrap(4));
    soundPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.ircEvents.dialog.field.soundBuiltIn")));
    soundPanel.add(builtInSound, MigConstraints.growXMinWidth(180));
    soundPanel.add(testSound, MigConstraints.widthHeight(36, 28));
    soundPanel.add(soundUseCustom, MigConstraints.wrap());
    soundPanel.add(
        new JLabel(
            MESSAGES.text("preferences.notifications.ircEvents.dialog.field.soundCustomFile")));
    soundPanel.add(soundCustomPath, MigConstraints.growXPushXMinWidth0());
    soundPanel.add(browseCustomSound, MigConstraints.widthHeight(36, 28));
    soundPanel.add(clearCustomSound, MigConstraints.widthHeightWrap(36, 28));
    soundPanel.add(new JLabel(""));
    soundPanel.add(
        PreferencesUiSupport.helpText(
            MESSAGES.text("preferences.notifications.ircEvents.dialog.sound.help")),
        MigConstraints.spanXGrowXMinWidthWrap(3, 0));

    JPanel scriptPanel =
        new JPanel(
            MigLayouts.labelFieldActionsFormWithHideMode(10, 8, 2, 3, MigLayouts.rowGaps(6, 4)));
    scriptPanel.add(scriptEnabled, MigConstraints.spanXGrowXWrap(4));
    scriptPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.ircEvents.dialog.field.scriptPath")));
    scriptPanel.add(scriptPath, MigConstraints.growXPushXMinWidth0());
    scriptPanel.add(browseScript, MigConstraints.widthHeight(36, 28));
    scriptPanel.add(clearScript, MigConstraints.widthHeightWrap(36, 28));
    scriptPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.ircEvents.dialog.field.scriptArgs")));
    scriptPanel.add(scriptArgs, MigConstraints.spanXGrowXMinWidthWrap(3, 0));
    scriptPanel.add(
        new JLabel(
            MESSAGES.text(
                "preferences.notifications.ircEvents.dialog.field.scriptWorkingDirectory")));
    scriptPanel.add(scriptWorkingDirectory, MigConstraints.growXPushXMinWidth0());
    scriptPanel.add(browseScriptWorkingDirectory, MigConstraints.widthHeight(36, 28));
    scriptPanel.add(clearScriptWorkingDirectory, MigConstraints.widthHeightWrap(36, 28));
    scriptPanel.add(new JLabel(""));
    scriptPanel.add(
        PreferencesUiSupport.helpText(
            MESSAGES.text("preferences.notifications.ircEvents.dialog.script.help")),
        MigConstraints.spanXGrowXMinWidthWrap(3, 0));

    JTabbedPane tabs = new JTabbedPane();
    tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabs.addTab(
        MESSAGES.text("preferences.notifications.ircEvents.dialog.tab.filters"), filtersPanel);
    tabs.addTab(
        MESSAGES.text("preferences.notifications.ircEvents.dialog.tab.actions"), actionsPanel);
    tabs.addTab(MESSAGES.text("preferences.notifications.ircEvents.dialog.tab.sound"), soundPanel);
    tabs.addTab(
        MESSAGES.text("preferences.notifications.ircEvents.dialog.tab.script"), scriptPanel);
    tabs.setPreferredSize(new Dimension(640, 420));

    JPanel form =
        new JPanel(
            MigLayouts.fillWrap(
                0, 1, MigLayoutConstraints.GROW_FILL, MigLayoutConstraints.GROW_FILL));
    form.add(tabs, MigConstraints.growPushMinWidth0());

    String dialogTitle =
        Objects.toString(title, MESSAGES.text("preferences.notifications.ircEvents.dialog.title"));
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
      String channelPatternsValue = PreferencesUiSupport.trimmedTextOrNull(channelPatterns);

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

      String scriptPathValue = PreferencesUiSupport.trimmedTextOrNull(scriptPath);
      String scriptArgsValue = PreferencesUiSupport.trimmedTextOrNull(scriptArgs);
      String scriptWorkingDirectoryValue =
          PreferencesUiSupport.trimmedTextOrNull(scriptWorkingDirectory);
      boolean runScript = scriptEnabled.isSelected();

      IrcEventNotificationRuleEditValidationError validationError =
          IrcEventNotificationRuleEditPolicy.validate(
              new IrcEventNotificationRuleEditValues(
                  enumName(selectedEvent),
                  enumName(selectedSourceMode),
                  sourcePatternValue,
                  enumName(selectedChannelScope),
                  channelPatternsValue,
                  enumName(selectedCtcpCommandMode),
                  ctcpCommandPatternValue,
                  enumName(selectedCtcpValueMode),
                  ctcpValuePatternValue,
                  runScript,
                  scriptPathValue));
      if (validationError != null) {
        showInvalidRuleMessage(
            owner,
            tabs,
            validationTabIndex(validationError),
            validationMessage(
                validationError,
                selectedSourceMode,
                selectedChannelScope,
                selectedCtcpCommandMode,
                selectedCtcpValueMode));
        continue;
      }
      BuiltInSound selectedSound =
          PreferencesUiSupport.selectedComboItem(
              builtInSound,
              BuiltInSound.class,
              IrcEventNotificationPresetSupport.defaultBuiltInSoundForEvent(selectedEvent));
      String soundCustomPathValue = PreferencesUiSupport.trimmedTextOrNull(soundCustomPath);
      IrcEventNotificationRuleEditSubmissionPlan submission =
          IrcEventNotificationRuleEditSubmissionPlanner.plan(
              enumName(selectedEvent),
              enumName(selectedSourceMode),
              sourcePatternValue,
              enumName(selectedChannelScope),
              channelPatternsValue,
              enumName(selectedCtcpCommandMode),
              ctcpCommandPatternValue,
              enumName(selectedCtcpValueMode),
              ctcpValuePatternValue,
              soundUseCustom.isSelected(),
              soundCustomPathValue,
              runScript,
              scriptPathValue,
              scriptArgsValue,
              scriptWorkingDirectoryValue);

      return new IrcEventNotificationRule(
          enabled.isSelected(),
          selectedEvent,
          selectedSourceMode,
          submission.sourcePattern(),
          selectedChannelScope,
          submission.channelPatterns(),
          toastEnabled.isSelected(),
          selectedFocusScope,
          statusBarEnabled.isSelected(),
          notificationsNodeEnabled.isSelected(),
          soundEnabled.isSelected(),
          selectedSound.name(),
          submission.soundUseCustom(),
          submission.soundCustomPath(),
          submission.scriptEnabled(),
          submission.scriptPath(),
          submission.scriptArgs(),
          submission.scriptWorkingDirectory(),
          ctcpMatchModeValue(
              submission.ctcpCommandMode(), IrcEventNotificationRule.CtcpMatchMode.ANY),
          submission.ctcpCommandPattern(),
          ctcpMatchModeValue(
              submission.ctcpValueMode(), IrcEventNotificationRule.CtcpMatchMode.ANY),
          submission.ctcpValuePattern());
    }
  }

  private static cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditSeedPlan seedPlan(
      IrcEventNotificationRule seed) {
    if (seed == null) {
      return cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditSeedPlanner
          .defaultSeed();
    }
    return cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditSeedPlanner.plan(
        seed.enabled(),
        enumName(seed.eventType()),
        enumName(seed.sourceMode()),
        seed.sourcePattern(),
        enumName(seed.channelScope()),
        seed.channelPatterns(),
        seed.toastEnabled(),
        enumName(seed.focusScope()),
        seed.statusBarEnabled(),
        seed.notificationsNodeEnabled(),
        seed.soundEnabled(),
        seed.soundId(),
        seed.soundUseCustom(),
        seed.soundCustomPath(),
        seed.scriptEnabled(),
        seed.scriptPath(),
        seed.scriptArgs(),
        seed.scriptWorkingDirectory(),
        enumName(seed.ctcpCommandMode()),
        seed.ctcpCommandPattern(),
        enumName(seed.ctcpValueMode()),
        seed.ctcpValuePattern());
  }

  private static IrcEventNotificationRule seedFromPlan(
      cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditSeedPlan plan) {
    return new IrcEventNotificationRule(
        plan.enabled(),
        eventTypeValue(plan.eventType(), IrcEventNotificationRule.EventType.INVITE_RECEIVED),
        sourceModeValue(plan.sourceMode(), IrcEventNotificationRule.SourceMode.ANY),
        plan.sourcePattern(),
        channelScopeValue(plan.channelScope(), IrcEventNotificationRule.ChannelScope.ALL),
        plan.channelPatterns(),
        plan.toastEnabled(),
        focusScopeValue(plan.focusScope(), IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY),
        plan.statusBarEnabled(),
        plan.notificationsNodeEnabled(),
        plan.soundEnabled(),
        plan.soundId(),
        plan.soundUseCustom(),
        plan.soundCustomPath(),
        plan.scriptEnabled(),
        plan.scriptPath(),
        plan.scriptArgs(),
        plan.scriptWorkingDirectory(),
        ctcpMatchModeValue(plan.ctcpCommandMode(), IrcEventNotificationRule.CtcpMatchMode.ANY),
        plan.ctcpCommandPattern(),
        ctcpMatchModeValue(plan.ctcpValueMode(), IrcEventNotificationRule.CtcpMatchMode.ANY),
        plan.ctcpValuePattern());
  }

  private static IrcEventNotificationRuleEditFieldPlan currentEditFieldPlan(
      JComboBox<IrcEventNotificationRule.EventType> eventType,
      JComboBox<IrcEventNotificationRule.SourceMode> sourceMode,
      JComboBox<IrcEventNotificationRule.ChannelScope> channelScope,
      JComboBox<IrcEventNotificationRule.CtcpMatchMode> ctcpCommandMode,
      JComboBox<IrcEventNotificationRule.CtcpMatchMode> ctcpValueMode,
      boolean scriptEnabled) {
    return IrcEventNotificationRuleEditFieldPlanner.plan(
        enumName(
            PreferencesUiSupport.selectedComboItem(
                eventType,
                IrcEventNotificationRule.EventType.class,
                IrcEventNotificationRule.EventType.INVITE_RECEIVED)),
        enumName(
            PreferencesUiSupport.selectedComboItem(
                sourceMode,
                IrcEventNotificationRule.SourceMode.class,
                IrcEventNotificationRule.SourceMode.ANY)),
        enumName(
            PreferencesUiSupport.selectedComboItem(
                channelScope,
                IrcEventNotificationRule.ChannelScope.class,
                IrcEventNotificationRule.ChannelScope.ALL)),
        enumName(
            PreferencesUiSupport.selectedComboItem(
                ctcpCommandMode,
                IrcEventNotificationRule.CtcpMatchMode.class,
                IrcEventNotificationRule.CtcpMatchMode.ANY)),
        enumName(
            PreferencesUiSupport.selectedComboItem(
                ctcpValueMode,
                IrcEventNotificationRule.CtcpMatchMode.class,
                IrcEventNotificationRule.CtcpMatchMode.ANY)),
        scriptEnabled);
  }

  private static String sourcePlaceholder(
      IrcEventNotificationRuleEditFieldPlan.SourcePatternHint hint) {
    return switch (hint) {
      case NICK_LIST ->
          MESSAGES.text("preferences.notifications.ircEvents.dialog.placeholder.source.nickList");
      case GLOB ->
          MESSAGES.text("preferences.notifications.ircEvents.dialog.placeholder.source.glob");
      case REGEX ->
          MESSAGES.text("preferences.notifications.ircEvents.dialog.placeholder.source.regex");
      case NONE -> "";
    };
  }

  private static String enumName(Enum<?> value) {
    return value == null ? null : value.name();
  }

  private static int validationTabIndex(IrcEventNotificationRuleEditValidationError error) {
    return IrcEventNotificationRuleEditValidationDisplayPlanner.plan(error).tabIndex();
  }

  private static String validationMessage(
      IrcEventNotificationRuleEditValidationError error,
      IrcEventNotificationRule.SourceMode sourceMode,
      IrcEventNotificationRule.ChannelScope channelScope,
      IrcEventNotificationRule.CtcpMatchMode ctcpCommandMode,
      IrcEventNotificationRule.CtcpMatchMode ctcpValueMode) {
    if (error == null) return "";
    String regexMessage =
        !error.message().isBlank()
            ? error.message()
            : MESSAGES.text("preferences.notifications.ircEvents.dialog.validation.invalidRegex");
    return switch (error.field()) {
      case SOURCE_PATTERN ->
          error.reason() == IrcEventNotificationRuleEditValidationError.Reason.REQUIRED
              ? MESSAGES.text(
                  "preferences.notifications.ircEvents.dialog.validation.sourcePatternRequired",
                  sourceMode)
              : MESSAGES.text(
                  "preferences.notifications.ircEvents.dialog.validation.sourceRegexInvalid",
                  regexMessage);
      case CHANNEL_PATTERNS ->
          MESSAGES.text(
              "preferences.notifications.ircEvents.dialog.validation.channelPatternsRequired",
              channelScope);
      case CTCP_COMMAND_PATTERN ->
          error.reason() == IrcEventNotificationRuleEditValidationError.Reason.REQUIRED
              ? MESSAGES.text(
                  "preferences.notifications.ircEvents.dialog.validation.ctcpCommandPatternRequired",
                  ctcpCommandMode)
              : MESSAGES.text(
                  "preferences.notifications.ircEvents.dialog.validation.ctcpCommandRegexInvalid",
                  regexMessage);
      case CTCP_VALUE_PATTERN ->
          error.reason() == IrcEventNotificationRuleEditValidationError.Reason.REQUIRED
              ? MESSAGES.text(
                  "preferences.notifications.ircEvents.dialog.validation.ctcpValuePatternRequired",
                  ctcpValueMode)
              : MESSAGES.text(
                  "preferences.notifications.ircEvents.dialog.validation.ctcpValueRegexInvalid",
                  regexMessage);
      case SCRIPT_PATH ->
          MESSAGES.text("preferences.notifications.ircEvents.dialog.validation.scriptPathRequired");
    };
  }

  private static IrcEventNotificationRule.EventType eventTypeValue(
      String value, IrcEventNotificationRule.EventType fallback) {
    try {
      return IrcEventNotificationRule.EventType.valueOf(Objects.toString(value, "").trim());
    } catch (Exception ignored) {
      return fallback;
    }
  }

  private static IrcEventNotificationRule.SourceMode sourceModeValue(
      String value, IrcEventNotificationRule.SourceMode fallback) {
    try {
      return IrcEventNotificationRule.SourceMode.valueOf(Objects.toString(value, "").trim());
    } catch (Exception ignored) {
      return fallback;
    }
  }

  private static IrcEventNotificationRule.ChannelScope channelScopeValue(
      String value, IrcEventNotificationRule.ChannelScope fallback) {
    try {
      return IrcEventNotificationRule.ChannelScope.valueOf(Objects.toString(value, "").trim());
    } catch (Exception ignored) {
      return fallback;
    }
  }

  private static IrcEventNotificationRule.FocusScope focusScopeValue(
      String value, IrcEventNotificationRule.FocusScope fallback) {
    try {
      return IrcEventNotificationRule.FocusScope.valueOf(Objects.toString(value, "").trim());
    } catch (Exception ignored) {
      return fallback;
    }
  }

  private static IrcEventNotificationRule.CtcpMatchMode ctcpMatchModeValue(
      String value, IrcEventNotificationRule.CtcpMatchMode fallback) {
    try {
      return IrcEventNotificationRule.CtcpMatchMode.valueOf(Objects.toString(value, "").trim());
    } catch (Exception ignored) {
      return fallback;
    }
  }

  private static void showInvalidRuleMessage(
      Window owner, JTabbedPane tabs, int tabIndex, String message) {
    PreferencesUiSupport.showErrorMessage(
        owner, message, MESSAGES.text("preferences.notifications.ircEvents.dialog.invalid.title"));
    tabs.setSelectedIndex(tabIndex);
  }

  private enum CtcpNotificationRuleTemplate {
    CUSTOM(
        "preferences.notifications.ircEvents.dialog.ctcpTemplate.custom",
        IrcEventNotificationCtcpTemplatePlanner.CUSTOM),
    VERSION(
        "preferences.notifications.ircEvents.dialog.ctcpTemplate.version",
        IrcEventNotificationCtcpTemplatePlanner.VERSION),
    PING(
        "preferences.notifications.ircEvents.dialog.ctcpTemplate.ping",
        IrcEventNotificationCtcpTemplatePlanner.PING),
    TIME(
        "preferences.notifications.ircEvents.dialog.ctcpTemplate.time",
        IrcEventNotificationCtcpTemplatePlanner.TIME),
    CLIENTINFO(
        "preferences.notifications.ircEvents.dialog.ctcpTemplate.clientinfo",
        IrcEventNotificationCtcpTemplatePlanner.CLIENTINFO),
    SOURCE(
        "preferences.notifications.ircEvents.dialog.ctcpTemplate.source",
        IrcEventNotificationCtcpTemplatePlanner.SOURCE),
    USERINFO(
        "preferences.notifications.ircEvents.dialog.ctcpTemplate.userinfo",
        IrcEventNotificationCtcpTemplatePlanner.USERINFO);

    private final String labelKey;
    private final String templateId;

    CtcpNotificationRuleTemplate(String labelKey, String templateId) {
      this.labelKey = labelKey;
      this.templateId = templateId;
    }

    String templateId() {
      return templateId;
    }

    @Override
    public String toString() {
      return MESSAGES.text(labelKey);
    }
  }
}

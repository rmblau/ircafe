package cafe.woden.ircclient.ui.settings.network;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.awt.Font;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public final class UserLookupsPanelSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private UserLookupsPanelSupport() {}

  static UserLookupsPanelControls buildControls(
      UiSettings current, List<AutoCloseable> closeables) {
    JPanel userLookupsPanel =
        new JPanel(MigLayouts.fillXWrapWithHideMode(12, 1, 3, MigLayoutConstraints.GROW_FILL, ""));
    userLookupsPanel.add(
        PreferencesUiSupport.tabTitle(MESSAGES.text("preferences.network.userLookups.title")),
        MigConstraints.growXWrap());

    JPanel userLookupsIntro = new JPanel(MigLayouts.fillXGrowTrailing(6));
    userLookupsIntro.setOpaque(false);
    JTextArea userLookupsBlurb =
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.network.userLookups.help"));
    JButton userLookupsHelp =
        PreferencesUiSupport.whyHelpButton(
            MESSAGES.text("preferences.network.userLookups.helpButton.title"),
            MESSAGES.text("preferences.network.userLookups.helpButton.message"));
    userLookupsIntro.add(userLookupsBlurb, MigConstraints.growXMinWidth0());
    userLookupsIntro.add(userLookupsHelp, MigConstraints.alignXRight());

    JPanel lookupPresetPanel = new JPanel(MigLayouts.twoColumnForm(12, MigLayouts.rows(2, 6)));
    lookupPresetPanel.setOpaque(false);

    JComboBox<LookupRatePreset> lookupPreset = new JComboBox<>(LookupRatePreset.values());
    lookupPreset.setSelectedItem(detectLookupRatePreset(current));

    JTextArea lookupPresetHint = PreferencesUiSupport.subtleInfoText();

    Runnable updateLookupPresetHint =
        () -> {
          LookupRatePreset preset =
              PreferencesUiSupport.selectedComboItem(
                  lookupPreset, LookupRatePreset.class, LookupRatePreset.CUSTOM);

          String message =
              switch (preset) {
                case CONSERVATIVE ->
                    MESSAGES.text("preferences.network.userLookups.preset.conservative.hint");
                case BALANCED ->
                    MESSAGES.text("preferences.network.userLookups.preset.balanced.hint");
                case RAPID -> MESSAGES.text("preferences.network.userLookups.preset.rapid.hint");
                case CUSTOM -> MESSAGES.text("preferences.network.userLookups.preset.custom.hint");
              };
          lookupPresetHint.setText(message);
        };
    updateLookupPresetHint.run();

    lookupPresetPanel.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.ratePreset")));
    lookupPresetPanel.add(lookupPreset, MigConstraints.width(220));
    lookupPresetPanel.add(lookupPresetHint, MigConstraints.span2GrowXMinWidth0Wrap());

    JSpinner monitorIsonPollIntervalSeconds =
        PreferencesUiSupport.numberSpinner(
            current.monitorIsonFallbackPollIntervalSeconds(), 5, 600, 5, closeables);
    monitorIsonPollIntervalSeconds.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.monitorIsonPoll.tooltip"));
    lookupPresetPanel.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.monitorIsonPoll")));
    lookupPresetPanel.add(monitorIsonPollIntervalSeconds, MigConstraints.widthWrap(110));

    JPanel hostmaskPanel = new JPanel(MigLayouts.twoColumnFormWithHideMode(8, 12, 3, ""));
    hostmaskPanel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                MESSAGES.text("preferences.network.userLookups.hostmask.section")),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)));
    hostmaskPanel.setOpaque(false);

    JCheckBox userhostEnabled =
        new JCheckBox(MESSAGES.text("preferences.network.userLookups.hostmask.enabled"));
    userhostEnabled.setSelected(current.userhostDiscoveryEnabled());
    userhostEnabled.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.hostmask.enabled.tooltip"));

    JButton hostmaskHelp =
        PreferencesUiSupport.whyHelpButton(
            MESSAGES.text("preferences.network.userLookups.hostmask.help.title"),
            MESSAGES.text("preferences.network.userLookups.hostmask.help.message"));

    JTextArea hostmaskSummary = PreferencesUiSupport.subtleInfoText();
    hostmaskSummary.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));

    JSpinner userhostMinIntervalSeconds =
        PreferencesUiSupport.numberSpinner(
            current.userhostMinIntervalSeconds(), 1, 60, 1, closeables);
    userhostMinIntervalSeconds.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.userhost.minInterval.tooltip"));

    JSpinner userhostMaxPerMinute =
        PreferencesUiSupport.numberSpinner(
            current.userhostMaxCommandsPerMinute(), 1, 60, 1, closeables);
    userhostMaxPerMinute.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.userhost.maxPerMinute.tooltip"));

    JSpinner userhostNickCooldownMinutes =
        PreferencesUiSupport.numberSpinner(
            current.userhostNickCooldownMinutes(), 1, 240, 1, closeables);
    userhostNickCooldownMinutes.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.userhost.nickCooldown.tooltip"));

    JSpinner userhostMaxNicksPerCommand =
        PreferencesUiSupport.numberSpinner(
            current.userhostMaxNicksPerCommand(), 1, 5, 1, closeables);
    userhostMaxNicksPerCommand.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.userhost.maxNicks.tooltip"));

    JPanel hostmaskAdvanced = new JPanel(MigLayouts.twoColumnForm(12, MigLayouts.rows(4, 6)));
    hostmaskAdvanced.setOpaque(false);
    hostmaskAdvanced.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.minInterval")));
    hostmaskAdvanced.add(userhostMinIntervalSeconds, MigConstraints.width(110));
    hostmaskAdvanced.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.maxCommands")));
    hostmaskAdvanced.add(userhostMaxPerMinute, MigConstraints.width(110));
    hostmaskAdvanced.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.nickCooldown")));
    hostmaskAdvanced.add(userhostNickCooldownMinutes, MigConstraints.width(110));
    hostmaskAdvanced.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.maxNicks")));
    hostmaskAdvanced.add(userhostMaxNicksPerCommand, MigConstraints.width(110));

    JPanel enrichmentPanel = new JPanel(MigLayouts.twoColumnFormWithHideMode(8, 12, 3, ""));
    enrichmentPanel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                MESSAGES.text("preferences.network.userLookups.enrichment.section")),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)));
    enrichmentPanel.setOpaque(false);

    JCheckBox enrichmentEnabled =
        new JCheckBox(MESSAGES.text("preferences.network.userLookups.enrichment.enabled"));
    enrichmentEnabled.setSelected(current.userInfoEnrichmentEnabled());
    enrichmentEnabled.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.enrichment.enabled.tooltip"));

    JCheckBox enrichmentWhoisFallbackEnabled =
        new JCheckBox(MESSAGES.text("preferences.network.userLookups.enrichment.whoisFallback"));
    enrichmentWhoisFallbackEnabled.setSelected(current.userInfoEnrichmentWhoisFallbackEnabled());
    enrichmentWhoisFallbackEnabled.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.enrichment.whoisFallback.tooltip"));

    JCheckBox enrichmentPeriodicRefreshEnabled =
        new JCheckBox(MESSAGES.text("preferences.network.userLookups.enrichment.periodicRefresh"));
    enrichmentPeriodicRefreshEnabled.setSelected(
        current.userInfoEnrichmentPeriodicRefreshEnabled());
    enrichmentPeriodicRefreshEnabled.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.enrichment.periodicRefresh.tooltip"));

    JButton enrichmentHelp =
        PreferencesUiSupport.whyHelpButton(
            MESSAGES.text("preferences.network.userLookups.enrichment.help.title"),
            MESSAGES.text("preferences.network.userLookups.enrichment.help.message"));

    JButton whoisHelp =
        PreferencesUiSupport.whyHelpButton(
            MESSAGES.text("preferences.network.userLookups.enrichment.whoisHelp.title"),
            MESSAGES.text("preferences.network.userLookups.enrichment.whoisHelp.message"));

    JButton refreshHelp =
        PreferencesUiSupport.whyHelpButton(
            MESSAGES.text("preferences.network.userLookups.enrichment.refreshHelp.title"),
            MESSAGES.text("preferences.network.userLookups.enrichment.refreshHelp.message"));

    JTextArea enrichmentSummary = PreferencesUiSupport.subtleInfoText();
    enrichmentSummary.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));

    JSpinner enrichmentUserhostMinIntervalSeconds =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentUserhostMinIntervalSeconds(), 1, 300, 1, closeables);
    enrichmentUserhostMinIntervalSeconds.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.enrichment.userhost.minInterval.tooltip"));

    JSpinner enrichmentUserhostMaxPerMinute =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentUserhostMaxCommandsPerMinute(), 1, 60, 1, closeables);
    enrichmentUserhostMaxPerMinute.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.enrichment.userhost.maxPerMinute.tooltip"));

    JSpinner enrichmentUserhostNickCooldownMinutes =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentUserhostNickCooldownMinutes(), 1, 1440, 1, closeables);
    enrichmentUserhostNickCooldownMinutes.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.enrichment.userhost.nickCooldown.tooltip"));

    JSpinner enrichmentUserhostMaxNicksPerCommand =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentUserhostMaxNicksPerCommand(), 1, 5, 1, closeables);
    enrichmentUserhostMaxNicksPerCommand.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.enrichment.userhost.maxNicks.tooltip"));

    JSpinner enrichmentWhoisMinIntervalSeconds =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentWhoisMinIntervalSeconds(), 5, 600, 5, closeables);
    enrichmentWhoisMinIntervalSeconds.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.enrichment.whois.minInterval.tooltip"));

    JSpinner enrichmentWhoisNickCooldownMinutes =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentWhoisNickCooldownMinutes(), 1, 1440, 1, closeables);
    enrichmentWhoisNickCooldownMinutes.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.enrichment.whois.nickCooldown.tooltip"));

    JSpinner enrichmentPeriodicRefreshIntervalSeconds =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentPeriodicRefreshIntervalSeconds(), 30, 3600, 30, closeables);
    enrichmentPeriodicRefreshIntervalSeconds.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.enrichment.refresh.interval.tooltip"));

    JSpinner enrichmentPeriodicRefreshNicksPerTick =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentPeriodicRefreshNicksPerTick(), 1, 20, 1, closeables);
    enrichmentPeriodicRefreshNicksPerTick.setToolTipText(
        MESSAGES.text("preferences.network.userLookups.enrichment.refresh.nicksPerTick.tooltip"));

    JPanel enrichmentAdvanced =
        new JPanel(MigLayouts.twoColumnForm(12, MigLayouts.rowGaps(6, 6, 6, 10, 6, 6, 10, 6, 6)));
    enrichmentAdvanced.setOpaque(false);
    JLabel userhostHdr =
        new JLabel(MESSAGES.text("preferences.network.userLookups.enrichment.userhost.section"));
    userhostHdr.setFont(userhostHdr.getFont().deriveFont(Font.BOLD));
    enrichmentAdvanced.add(userhostHdr, MigConstraints.span2GrowXMinWidth0Wrap());
    enrichmentAdvanced.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.minInterval")));
    enrichmentAdvanced.add(enrichmentUserhostMinIntervalSeconds, MigConstraints.width(110));
    enrichmentAdvanced.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.maxCmd")));
    enrichmentAdvanced.add(enrichmentUserhostMaxPerMinute, MigConstraints.width(110));
    enrichmentAdvanced.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.nickCooldown")));
    enrichmentAdvanced.add(enrichmentUserhostNickCooldownMinutes, MigConstraints.width(110));
    enrichmentAdvanced.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.maxNicksShort")));
    enrichmentAdvanced.add(enrichmentUserhostMaxNicksPerCommand, MigConstraints.width(110));
    JLabel whoisHdr =
        new JLabel(MESSAGES.text("preferences.network.userLookups.enrichment.whois.section"));
    whoisHdr.setFont(whoisHdr.getFont().deriveFont(Font.BOLD));
    enrichmentAdvanced.add(whoisHdr, MigConstraints.span2GrowXMinWidth0Wrap());
    enrichmentAdvanced.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.minInterval")));
    enrichmentAdvanced.add(enrichmentWhoisMinIntervalSeconds, MigConstraints.width(110));
    enrichmentAdvanced.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.nickCooldown")));
    enrichmentAdvanced.add(enrichmentWhoisNickCooldownMinutes, MigConstraints.width(110));
    JLabel refreshHdr =
        new JLabel(MESSAGES.text("preferences.network.userLookups.enrichment.refresh.section"));
    refreshHdr.setFont(refreshHdr.getFont().deriveFont(Font.BOLD));
    enrichmentAdvanced.add(refreshHdr, MigConstraints.span2GrowXMinWidth0Wrap());
    enrichmentAdvanced.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.interval")));
    enrichmentAdvanced.add(enrichmentPeriodicRefreshIntervalSeconds, MigConstraints.width(110));
    enrichmentAdvanced.add(
        new JLabel(MESSAGES.text("preferences.network.userLookups.field.nicksPerTick")));
    enrichmentAdvanced.add(enrichmentPeriodicRefreshNicksPerTick, MigConstraints.width(110));

    Consumer<LookupRatePreset> applyLookupPreset =
        preset -> {
          if (preset == null || preset == LookupRatePreset.CUSTOM) return;

          switch (preset) {
            case CONSERVATIVE -> {
              userhostMinIntervalSeconds.setValue(10);
              userhostMaxPerMinute.setValue(2);
              userhostNickCooldownMinutes.setValue(60);
              userhostMaxNicksPerCommand.setValue(5);
              enrichmentUserhostMinIntervalSeconds.setValue(30);
              enrichmentUserhostMaxPerMinute.setValue(2);
              enrichmentUserhostNickCooldownMinutes.setValue(180);
              enrichmentUserhostMaxNicksPerCommand.setValue(5);
              enrichmentWhoisMinIntervalSeconds.setValue(120);
              enrichmentWhoisNickCooldownMinutes.setValue(240);
              enrichmentPeriodicRefreshIntervalSeconds.setValue(600);
              enrichmentPeriodicRefreshNicksPerTick.setValue(1);
            }
            case BALANCED -> {
              userhostMinIntervalSeconds.setValue(5);
              userhostMaxPerMinute.setValue(6);
              userhostNickCooldownMinutes.setValue(30);
              userhostMaxNicksPerCommand.setValue(5);
              enrichmentUserhostMinIntervalSeconds.setValue(15);
              enrichmentUserhostMaxPerMinute.setValue(4);
              enrichmentUserhostNickCooldownMinutes.setValue(60);
              enrichmentUserhostMaxNicksPerCommand.setValue(5);
              enrichmentWhoisMinIntervalSeconds.setValue(60);
              enrichmentWhoisNickCooldownMinutes.setValue(120);
              enrichmentPeriodicRefreshIntervalSeconds.setValue(300);
              enrichmentPeriodicRefreshNicksPerTick.setValue(2);
            }
            case RAPID -> {
              userhostMinIntervalSeconds.setValue(2);
              userhostMaxPerMinute.setValue(15);
              userhostNickCooldownMinutes.setValue(10);
              userhostMaxNicksPerCommand.setValue(5);
              enrichmentUserhostMinIntervalSeconds.setValue(5);
              enrichmentUserhostMaxPerMinute.setValue(10);
              enrichmentUserhostNickCooldownMinutes.setValue(15);
              enrichmentUserhostMaxNicksPerCommand.setValue(5);
              enrichmentWhoisMinIntervalSeconds.setValue(15);
              enrichmentWhoisNickCooldownMinutes.setValue(30);
              enrichmentPeriodicRefreshIntervalSeconds.setValue(60);
              enrichmentPeriodicRefreshNicksPerTick.setValue(3);
            }
            case CUSTOM -> {
              // No-op.
            }
          }
        };

    Runnable updateHostmaskSummary =
        () -> {
          if (!userhostEnabled.isSelected()) {
            hostmaskSummary.setText(
                MESSAGES.text("preferences.network.userLookups.summary.disabled"));
            return;
          }
          int minInterval = PreferencesUiSupport.spinnerInt(userhostMinIntervalSeconds);
          int maxPerMinute = PreferencesUiSupport.spinnerInt(userhostMaxPerMinute);
          int cooldownMinutes = PreferencesUiSupport.spinnerInt(userhostNickCooldownMinutes);
          int maxNicks = PreferencesUiSupport.spinnerInt(userhostMaxNicksPerCommand);
          hostmaskSummary.setText(
              MESSAGES.text(
                  "preferences.network.userLookups.summary.userhost",
                  maxPerMinute,
                  minInterval,
                  cooldownMinutes,
                  maxNicks));
        };

    Runnable updateEnrichmentSummary =
        () -> {
          if (!enrichmentEnabled.isSelected()) {
            enrichmentSummary.setText(
                MESSAGES.text("preferences.network.userLookups.summary.disabled"));
            return;
          }

          int minInterval = PreferencesUiSupport.spinnerInt(enrichmentUserhostMinIntervalSeconds);
          int maxPerMinute = PreferencesUiSupport.spinnerInt(enrichmentUserhostMaxPerMinute);
          int cooldownMinutes =
              PreferencesUiSupport.spinnerInt(enrichmentUserhostNickCooldownMinutes);
          int maxNicks = PreferencesUiSupport.spinnerInt(enrichmentUserhostMaxNicksPerCommand);

          String whoisSummary;
          if (enrichmentWhoisFallbackEnabled.isSelected()) {
            int whoisMinInterval =
                PreferencesUiSupport.spinnerInt(enrichmentWhoisMinIntervalSeconds);
            int whoisCooldown = PreferencesUiSupport.spinnerInt(enrichmentWhoisNickCooldownMinutes);
            whoisSummary =
                MESSAGES.text(
                    "preferences.network.userLookups.summary.whois",
                    whoisMinInterval,
                    whoisCooldown);
          } else {
            whoisSummary = MESSAGES.text("preferences.network.userLookups.summary.whoisOff");
          }

          String refreshSummary;
          if (enrichmentPeriodicRefreshEnabled.isSelected()) {
            int refreshInterval =
                PreferencesUiSupport.spinnerInt(enrichmentPeriodicRefreshIntervalSeconds);
            int refreshNicks =
                PreferencesUiSupport.spinnerInt(enrichmentPeriodicRefreshNicksPerTick);
            refreshSummary =
                MESSAGES.text(
                    "preferences.network.userLookups.summary.refresh",
                    refreshInterval,
                    refreshNicks);
          } else {
            refreshSummary = MESSAGES.text("preferences.network.userLookups.summary.refreshOff");
          }

          enrichmentSummary.setText(
              MESSAGES.text(
                  "preferences.network.userLookups.summary.enrichment",
                  maxPerMinute,
                  minInterval,
                  cooldownMinutes,
                  maxNicks,
                  whoisSummary,
                  refreshSummary));
        };

    Runnable updateAllSummaries =
        () -> {
          updateHostmaskSummary.run();
          updateEnrichmentSummary.run();
        };

    Runnable updateHostmaskState =
        () -> {
          boolean enabled = userhostEnabled.isSelected();
          LookupRatePreset preset =
              PreferencesUiSupport.selectedComboItem(
                  lookupPreset, LookupRatePreset.class, LookupRatePreset.CUSTOM);
          boolean custom = preset == LookupRatePreset.CUSTOM;

          boolean showAdvanced = enabled && custom;
          hostmaskAdvanced.setVisible(showAdvanced);

          userhostMinIntervalSeconds.setEnabled(showAdvanced);
          userhostMaxPerMinute.setEnabled(showAdvanced);
          userhostNickCooldownMinutes.setEnabled(showAdvanced);
          userhostMaxNicksPerCommand.setEnabled(showAdvanced);

          updateHostmaskSummary.run();
        };

    Runnable updateEnrichmentState =
        () -> {
          boolean enabled = enrichmentEnabled.isSelected();
          LookupRatePreset preset =
              PreferencesUiSupport.selectedComboItem(
                  lookupPreset, LookupRatePreset.class, LookupRatePreset.CUSTOM);
          boolean custom = preset == LookupRatePreset.CUSTOM;

          enrichmentWhoisFallbackEnabled.setEnabled(enabled);
          enrichmentPeriodicRefreshEnabled.setEnabled(enabled);

          boolean showAdvanced = enabled && custom;
          enrichmentAdvanced.setVisible(showAdvanced);

          enrichmentUserhostMinIntervalSeconds.setEnabled(showAdvanced);
          enrichmentUserhostMaxPerMinute.setEnabled(showAdvanced);
          enrichmentUserhostNickCooldownMinutes.setEnabled(showAdvanced);
          enrichmentUserhostMaxNicksPerCommand.setEnabled(showAdvanced);

          boolean whoisEnabled = showAdvanced && enrichmentWhoisFallbackEnabled.isSelected();
          enrichmentWhoisMinIntervalSeconds.setEnabled(whoisEnabled);
          enrichmentWhoisNickCooldownMinutes.setEnabled(whoisEnabled);

          boolean periodicEnabled = showAdvanced && enrichmentPeriodicRefreshEnabled.isSelected();
          enrichmentPeriodicRefreshIntervalSeconds.setEnabled(periodicEnabled);
          enrichmentPeriodicRefreshNicksPerTick.setEnabled(periodicEnabled);

          updateEnrichmentSummary.run();
        };

    userhostEnabled.addActionListener(
        e -> {
          updateHostmaskState.run();
          updateAllSummaries.run();
          hostmaskPanel.revalidate();
          hostmaskPanel.repaint();
          userLookupsPanel.revalidate();
          userLookupsPanel.repaint();
        });

    enrichmentEnabled.addActionListener(
        e -> {
          updateEnrichmentState.run();
          updateAllSummaries.run();
          enrichmentPanel.revalidate();
          enrichmentPanel.repaint();
          userLookupsPanel.revalidate();
          userLookupsPanel.repaint();
        });

    enrichmentWhoisFallbackEnabled.addActionListener(
        e -> {
          updateEnrichmentState.run();
          updateAllSummaries.run();
        });
    enrichmentPeriodicRefreshEnabled.addActionListener(
        e -> {
          updateEnrichmentState.run();
          updateAllSummaries.run();
        });

    lookupPreset.addActionListener(
        e -> {
          LookupRatePreset preset =
              PreferencesUiSupport.selectedComboItem(lookupPreset, LookupRatePreset.class, null);
          if (preset != null && preset != LookupRatePreset.CUSTOM) {
            applyLookupPreset.accept(preset);
          }
          updateLookupPresetHint.run();
          updateHostmaskState.run();
          updateEnrichmentState.run();
          updateAllSummaries.run();
          hostmaskPanel.revalidate();
          hostmaskPanel.repaint();
          enrichmentPanel.revalidate();
          enrichmentPanel.repaint();
          userLookupsPanel.revalidate();
          userLookupsPanel.repaint();
        });

    javax.swing.event.ChangeListener summaryChange = e -> updateAllSummaries.run();
    userhostMinIntervalSeconds.addChangeListener(summaryChange);
    userhostMaxPerMinute.addChangeListener(summaryChange);
    userhostNickCooldownMinutes.addChangeListener(summaryChange);
    userhostMaxNicksPerCommand.addChangeListener(summaryChange);
    enrichmentUserhostMinIntervalSeconds.addChangeListener(summaryChange);
    enrichmentUserhostMaxPerMinute.addChangeListener(summaryChange);
    enrichmentUserhostNickCooldownMinutes.addChangeListener(summaryChange);
    enrichmentUserhostMaxNicksPerCommand.addChangeListener(summaryChange);
    enrichmentWhoisMinIntervalSeconds.addChangeListener(summaryChange);
    enrichmentWhoisNickCooldownMinutes.addChangeListener(summaryChange);
    enrichmentPeriodicRefreshIntervalSeconds.addChangeListener(summaryChange);
    enrichmentPeriodicRefreshNicksPerTick.addChangeListener(summaryChange);

    JPanel enrichmentWhoisRow = new JPanel(MigLayouts.fillXGrowTrailing(6));
    enrichmentWhoisRow.setOpaque(false);
    enrichmentWhoisRow.add(enrichmentWhoisFallbackEnabled, MigConstraints.growX());
    enrichmentWhoisRow.add(whoisHelp, MigConstraints.alignXRight());

    JPanel enrichmentRefreshRow = new JPanel(MigLayouts.fillXGrowTrailing(6));
    enrichmentRefreshRow.setOpaque(false);
    enrichmentRefreshRow.add(enrichmentPeriodicRefreshEnabled, MigConstraints.growX());
    enrichmentRefreshRow.add(refreshHelp, MigConstraints.alignXRight());

    hostmaskPanel.add(userhostEnabled, MigConstraints.growX());
    hostmaskPanel.add(hostmaskHelp, MigConstraints.alignXRightWrap());
    hostmaskPanel.add(hostmaskSummary, MigConstraints.span2GrowXMinWidth0Wrap());
    hostmaskPanel.add(hostmaskAdvanced, MigConstraints.spanXGrowXHideModeWrap(2, 3));

    enrichmentPanel.add(enrichmentEnabled, MigConstraints.growX());
    enrichmentPanel.add(enrichmentHelp, MigConstraints.alignXRightWrap());
    enrichmentPanel.add(enrichmentSummary, MigConstraints.span2GrowXMinWidth0Wrap());
    enrichmentPanel.add(enrichmentWhoisRow, MigConstraints.spanXGrowXGapLeftWrap(2, 18));
    enrichmentPanel.add(enrichmentRefreshRow, MigConstraints.spanXGrowXGapLeftWrap(2, 18));
    enrichmentPanel.add(enrichmentAdvanced, MigConstraints.spanXGrowXHideModeWrap(2, 3));
    hostmaskAdvanced.setVisible(false);
    enrichmentAdvanced.setVisible(false);
    updateHostmaskState.run();
    updateEnrichmentState.run();

    JTabbedPane lookupsTabs = new JTabbedPane();
    JPanel lookupsOverview = new JPanel(MigLayouts.singleColumn(MigLayouts.rows(2, 10)));
    lookupsOverview.setOpaque(false);
    lookupsOverview.add(userLookupsIntro, MigConstraints.growXMinWidth0Wrap());
    lookupsOverview.add(lookupPresetPanel, MigConstraints.growXMinWidth0Wrap());

    lookupsTabs.addTab(
        MESSAGES.text("preferences.network.userLookups.tab.overview"),
        PreferencesUiSupport.padSubTab(lookupsOverview));
    lookupsTabs.addTab(
        MESSAGES.text("preferences.network.userLookups.tab.hostmask"),
        PreferencesUiSupport.padSubTab(hostmaskPanel));
    lookupsTabs.addTab(
        MESSAGES.text("preferences.network.userLookups.tab.enrichment"),
        PreferencesUiSupport.padSubTab(enrichmentPanel));

    userLookupsPanel.add(lookupsTabs, MigConstraints.growXMinWidth0Wrap());

    UserhostControls userhostControls =
        new UserhostControls(
            userhostEnabled,
            userhostMinIntervalSeconds,
            userhostMaxPerMinute,
            userhostNickCooldownMinutes,
            userhostMaxNicksPerCommand);
    UserInfoEnrichmentControls enrichmentControls =
        new UserInfoEnrichmentControls(
            enrichmentEnabled,
            enrichmentUserhostMinIntervalSeconds,
            enrichmentUserhostMaxPerMinute,
            enrichmentUserhostNickCooldownMinutes,
            enrichmentUserhostMaxNicksPerCommand,
            enrichmentWhoisFallbackEnabled,
            enrichmentWhoisMinIntervalSeconds,
            enrichmentWhoisNickCooldownMinutes,
            enrichmentPeriodicRefreshEnabled,
            enrichmentPeriodicRefreshIntervalSeconds,
            enrichmentPeriodicRefreshNicksPerTick);

    return new UserLookupsPanelControls(
        userhostControls, enrichmentControls, monitorIsonPollIntervalSeconds, userLookupsPanel);
  }

  public static UserLookupSettings readSettings(
      UserhostControls userhost,
      UserInfoEnrichmentControls enrichment,
      JSpinner monitorIsonPollIntervalSeconds) {
    return new UserLookupSettings(
        userhost.enabled.isSelected(),
        PreferencesUiSupport.spinnerInt(userhost.minIntervalSeconds),
        PreferencesUiSupport.spinnerInt(userhost.maxPerMinute),
        PreferencesUiSupport.spinnerInt(userhost.nickCooldownMinutes),
        PreferencesUiSupport.spinnerInt(userhost.maxNicksPerCommand),
        enrichment.enabled.isSelected(),
        PreferencesUiSupport.spinnerInt(enrichment.userhostMinIntervalSeconds),
        PreferencesUiSupport.spinnerInt(enrichment.userhostMaxPerMinute),
        PreferencesUiSupport.spinnerInt(enrichment.userhostNickCooldownMinutes),
        PreferencesUiSupport.spinnerInt(enrichment.userhostMaxNicksPerCommand),
        enrichment.whoisFallbackEnabled.isSelected(),
        PreferencesUiSupport.spinnerInt(enrichment.whoisMinIntervalSeconds),
        PreferencesUiSupport.spinnerInt(enrichment.whoisNickCooldownMinutes),
        enrichment.periodicRefreshEnabled.isSelected(),
        PreferencesUiSupport.spinnerInt(enrichment.periodicRefreshIntervalSeconds),
        PreferencesUiSupport.spinnerInt(enrichment.periodicRefreshNicksPerTick),
        PreferencesUiSupport.spinnerInt(monitorIsonPollIntervalSeconds));
  }

  public static void rememberSettings(
      RuntimeConfigStore runtimeConfig, UserLookupSettings settings) {
    runtimeConfig.rememberUserhostDiscoveryEnabled(settings.userhostEnabled());
    runtimeConfig.rememberUserhostMinIntervalSeconds(settings.userhostMinIntervalSeconds());
    runtimeConfig.rememberUserhostMaxCommandsPerMinute(settings.userhostMaxCommandsPerMinute());
    runtimeConfig.rememberUserhostNickCooldownMinutes(settings.userhostNickCooldownMinutes());
    runtimeConfig.rememberUserhostMaxNicksPerCommand(settings.userhostMaxNicksPerCommand());
    runtimeConfig.rememberUserInfoEnrichmentEnabled(settings.enrichmentEnabled());
    runtimeConfig.rememberUserInfoEnrichmentWhoisFallbackEnabled(
        settings.enrichmentWhoisFallbackEnabled());
    runtimeConfig.rememberUserInfoEnrichmentUserhostMinIntervalSeconds(
        settings.enrichmentUserhostMinIntervalSeconds());
    runtimeConfig.rememberUserInfoEnrichmentUserhostMaxCommandsPerMinute(
        settings.enrichmentUserhostMaxCommandsPerMinute());
    runtimeConfig.rememberUserInfoEnrichmentUserhostNickCooldownMinutes(
        settings.enrichmentUserhostNickCooldownMinutes());
    runtimeConfig.rememberUserInfoEnrichmentUserhostMaxNicksPerCommand(
        settings.enrichmentUserhostMaxNicksPerCommand());
    runtimeConfig.rememberUserInfoEnrichmentWhoisMinIntervalSeconds(
        settings.enrichmentWhoisMinIntervalSeconds());
    runtimeConfig.rememberUserInfoEnrichmentWhoisNickCooldownMinutes(
        settings.enrichmentWhoisNickCooldownMinutes());
    runtimeConfig.rememberUserInfoEnrichmentPeriodicRefreshEnabled(
        settings.enrichmentPeriodicRefreshEnabled());
    runtimeConfig.rememberUserInfoEnrichmentPeriodicRefreshIntervalSeconds(
        settings.enrichmentPeriodicRefreshIntervalSeconds());
    runtimeConfig.rememberUserInfoEnrichmentPeriodicRefreshNicksPerTick(
        settings.enrichmentPeriodicRefreshNicksPerTick());
    runtimeConfig.rememberMonitorIsonPollIntervalSeconds(settings.monitorIsonPollIntervalSeconds());
  }

  private enum LookupRatePreset {
    CONSERVATIVE("preferences.network.userLookups.preset.conservative"),
    BALANCED("preferences.network.userLookups.preset.balanced"),
    RAPID("preferences.network.userLookups.preset.rapid"),
    CUSTOM("preferences.network.userLookups.preset.custom");

    private final String messageKey;

    LookupRatePreset(String messageKey) {
      this.messageKey = messageKey;
    }

    @Override
    public String toString() {
      return MESSAGES.text(messageKey);
    }
  }

  private static LookupRatePreset detectLookupRatePreset(UiSettings settings) {
    if (matchesLookupRatePreset(settings, LookupRatePreset.BALANCED)) {
      return LookupRatePreset.BALANCED;
    }
    if (matchesLookupRatePreset(settings, LookupRatePreset.CONSERVATIVE)) {
      return LookupRatePreset.CONSERVATIVE;
    }
    if (matchesLookupRatePreset(settings, LookupRatePreset.RAPID)) {
      return LookupRatePreset.RAPID;
    }
    return LookupRatePreset.CUSTOM;
  }

  private static boolean matchesLookupRatePreset(UiSettings settings, LookupRatePreset preset) {
    return switch (preset) {
      case CONSERVATIVE ->
          settings.userhostMinIntervalSeconds() == 10
              && settings.userhostMaxCommandsPerMinute() == 2
              && settings.userhostNickCooldownMinutes() == 60
              && settings.userhostMaxNicksPerCommand() == 5
              && settings.userInfoEnrichmentUserhostMinIntervalSeconds() == 30
              && settings.userInfoEnrichmentUserhostMaxCommandsPerMinute() == 2
              && settings.userInfoEnrichmentUserhostNickCooldownMinutes() == 180
              && settings.userInfoEnrichmentUserhostMaxNicksPerCommand() == 5
              && settings.userInfoEnrichmentWhoisMinIntervalSeconds() == 120
              && settings.userInfoEnrichmentWhoisNickCooldownMinutes() == 240
              && settings.userInfoEnrichmentPeriodicRefreshIntervalSeconds() == 600
              && settings.userInfoEnrichmentPeriodicRefreshNicksPerTick() == 1;
      case BALANCED ->
          settings.userhostMinIntervalSeconds() == 5
              && settings.userhostMaxCommandsPerMinute() == 6
              && settings.userhostNickCooldownMinutes() == 30
              && settings.userhostMaxNicksPerCommand() == 5
              && settings.userInfoEnrichmentUserhostMinIntervalSeconds() == 15
              && settings.userInfoEnrichmentUserhostMaxCommandsPerMinute() == 4
              && settings.userInfoEnrichmentUserhostNickCooldownMinutes() == 60
              && settings.userInfoEnrichmentUserhostMaxNicksPerCommand() == 5
              && settings.userInfoEnrichmentWhoisMinIntervalSeconds() == 60
              && settings.userInfoEnrichmentWhoisNickCooldownMinutes() == 120
              && settings.userInfoEnrichmentPeriodicRefreshIntervalSeconds() == 300
              && settings.userInfoEnrichmentPeriodicRefreshNicksPerTick() == 2;
      case RAPID ->
          settings.userhostMinIntervalSeconds() == 2
              && settings.userhostMaxCommandsPerMinute() == 15
              && settings.userhostNickCooldownMinutes() == 10
              && settings.userhostMaxNicksPerCommand() == 5
              && settings.userInfoEnrichmentUserhostMinIntervalSeconds() == 5
              && settings.userInfoEnrichmentUserhostMaxCommandsPerMinute() == 10
              && settings.userInfoEnrichmentUserhostNickCooldownMinutes() == 15
              && settings.userInfoEnrichmentUserhostMaxNicksPerCommand() == 5
              && settings.userInfoEnrichmentWhoisMinIntervalSeconds() == 15
              && settings.userInfoEnrichmentWhoisNickCooldownMinutes() == 30
              && settings.userInfoEnrichmentPeriodicRefreshIntervalSeconds() == 60
              && settings.userInfoEnrichmentPeriodicRefreshNicksPerTick() == 3;
      case CUSTOM -> false;
    };
  }

  public record UserLookupSettings(
      boolean userhostEnabled,
      int userhostMinIntervalSeconds,
      int userhostMaxCommandsPerMinute,
      int userhostNickCooldownMinutes,
      int userhostMaxNicksPerCommand,
      boolean enrichmentEnabled,
      int enrichmentUserhostMinIntervalSeconds,
      int enrichmentUserhostMaxCommandsPerMinute,
      int enrichmentUserhostNickCooldownMinutes,
      int enrichmentUserhostMaxNicksPerCommand,
      boolean enrichmentWhoisFallbackEnabled,
      int enrichmentWhoisMinIntervalSeconds,
      int enrichmentWhoisNickCooldownMinutes,
      boolean enrichmentPeriodicRefreshEnabled,
      int enrichmentPeriodicRefreshIntervalSeconds,
      int enrichmentPeriodicRefreshNicksPerTick,
      int monitorIsonPollIntervalSeconds) {
    public UserLookupSettings {
      userhostMinIntervalSeconds =
          SettingsRangeSupport.normalizeUserhostMinIntervalSeconds(userhostMinIntervalSeconds);
      userhostMaxCommandsPerMinute =
          SettingsRangeSupport.normalizeUserhostMaxCommandsPerMinute(userhostMaxCommandsPerMinute);
      userhostNickCooldownMinutes =
          SettingsRangeSupport.normalizeUserhostNickCooldownMinutes(userhostNickCooldownMinutes);
      userhostMaxNicksPerCommand =
          SettingsRangeSupport.normalizeUserhostMaxNicksPerCommand(userhostMaxNicksPerCommand);

      enrichmentUserhostMinIntervalSeconds =
          SettingsRangeSupport.normalizeEnrichmentUserhostMinIntervalSeconds(
              enrichmentUserhostMinIntervalSeconds);
      enrichmentUserhostMaxCommandsPerMinute =
          SettingsRangeSupport.normalizeEnrichmentUserhostMaxCommandsPerMinute(
              enrichmentUserhostMaxCommandsPerMinute);
      enrichmentUserhostNickCooldownMinutes =
          SettingsRangeSupport.normalizeEnrichmentUserhostNickCooldownMinutes(
              enrichmentUserhostNickCooldownMinutes);
      enrichmentUserhostMaxNicksPerCommand =
          SettingsRangeSupport.normalizeUserhostMaxNicksPerCommand(
              enrichmentUserhostMaxNicksPerCommand);

      enrichmentWhoisMinIntervalSeconds =
          SettingsRangeSupport.normalizeEnrichmentWhoisMinIntervalSeconds(
              enrichmentWhoisMinIntervalSeconds);
      enrichmentWhoisNickCooldownMinutes =
          SettingsRangeSupport.normalizeEnrichmentWhoisNickCooldownMinutes(
              enrichmentWhoisNickCooldownMinutes);
      enrichmentPeriodicRefreshIntervalSeconds =
          SettingsRangeSupport.normalizeEnrichmentPeriodicRefreshIntervalSeconds(
              enrichmentPeriodicRefreshIntervalSeconds);
      enrichmentPeriodicRefreshNicksPerTick =
          SettingsRangeSupport.normalizeEnrichmentPeriodicRefreshNicksPerTick(
              enrichmentPeriodicRefreshNicksPerTick);

      enrichmentWhoisFallbackEnabled = enrichmentEnabled && enrichmentWhoisFallbackEnabled;
      enrichmentPeriodicRefreshEnabled = enrichmentEnabled && enrichmentPeriodicRefreshEnabled;

      monitorIsonPollIntervalSeconds =
          SettingsRangeSupport.normalizeMonitorIsonFallbackPollIntervalSeconds(
              monitorIsonPollIntervalSeconds);
    }
  }
}

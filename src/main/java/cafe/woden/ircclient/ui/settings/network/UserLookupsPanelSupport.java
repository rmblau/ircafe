package cafe.woden.ircclient.ui.settings.network;

import cafe.woden.ircclient.config.RuntimeConfigStore;
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
  private UserLookupsPanelSupport() {}

  static UserLookupsPanelControls buildControls(
      UiSettings current, List<AutoCloseable> closeables) {
    JPanel userLookupsPanel =
        new JPanel(MigLayouts.fillXWrapWithHideMode(12, 1, 3, MigLayoutConstraints.GROW_FILL, ""));
    userLookupsPanel.add(PreferencesUiSupport.tabTitle("User lookups"), MigConstraints.growXWrap());

    JPanel userLookupsIntro = new JPanel(MigLayouts.fillXGrowTrailing(6));
    userLookupsIntro.setOpaque(false);
    JTextArea userLookupsBlurb =
        PreferencesUiSupport.helpText(
            "Optional fallbacks for account/away/host info (USERHOST / WHOIS), with conservative rate limits.");
    JButton userLookupsHelp =
        PreferencesUiSupport.whyHelpButton(
            "Why do I need user lookups?",
            "Most modern IRC networks provide account and presence information via IRCv3 (e.g., account-tag, account-notify, away-notify, extended-join).\n\n"
                + "However, some networks (or some pieces of data) still require fallback lookups. IRCafe can optionally use USERHOST and (as a last resort) WHOIS to fill missing metadata.\n\n"
                + "If you're on an IRCv3-capable network and don't use hostmask-based ignore rules, you can usually leave these disabled.");
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
                case CONSERVATIVE -> "Lowest traffic. Best for huge channels or strict networks.";
                case BALANCED -> "Recommended default. Good fill-in speed with low risk.";
                case RAPID -> "Faster fill-in. More commands on the wire (use with caution).";
                case CUSTOM -> "Custom shows the tuning controls below.";
              };
          lookupPresetHint.setText(message);
        };
    updateLookupPresetHint.run();

    lookupPresetPanel.add(new JLabel("Rate limit preset:"));
    lookupPresetPanel.add(lookupPreset, MigConstraints.width(220));
    lookupPresetPanel.add(lookupPresetHint, MigConstraints.span2GrowXMinWidth0Wrap());

    JSpinner monitorIsonPollIntervalSeconds =
        PreferencesUiSupport.numberSpinner(
            current.monitorIsonFallbackPollIntervalSeconds(), 5, 600, 5, closeables);
    monitorIsonPollIntervalSeconds.setToolTipText(
        "Polling interval for ISON monitor fallback when IRC MONITOR is unavailable.");
    lookupPresetPanel.add(new JLabel("MONITOR fallback poll (sec):"));
    lookupPresetPanel.add(monitorIsonPollIntervalSeconds, MigConstraints.widthWrap(110));

    JPanel hostmaskPanel = new JPanel(MigLayouts.twoColumnFormWithHideMode(8, 12, 3, ""));
    hostmaskPanel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Hostmask discovery"),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)));
    hostmaskPanel.setOpaque(false);

    JCheckBox userhostEnabled =
        new JCheckBox("Fill missing hostmasks using USERHOST (rate-limited)");
    userhostEnabled.setSelected(current.userhostDiscoveryEnabled());
    userhostEnabled.setToolTipText(
        "When enabled, IRCafe may send USERHOST only when hostmask-based ignore rules exist and some nicks are missing hostmasks.");

    JButton hostmaskHelp =
        PreferencesUiSupport.whyHelpButton(
            "Why do I need hostmask discovery?",
            "Some ignore rules rely on hostmasks (nick!user@host).\n\n"
                + "On many networks, the full hostmask isn't included in NAMES and might not be available until additional lookups happen.\n\n"
                + "If you use hostmask-based ignore rules and some users show up without hostmasks, IRCafe can send rate-limited USERHOST commands to fill them in.\n\n"
                + "If you don't use hostmask-based ignores, you can usually leave this off.");

    JTextArea hostmaskSummary = PreferencesUiSupport.subtleInfoText();
    hostmaskSummary.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));

    JSpinner userhostMinIntervalSeconds =
        PreferencesUiSupport.numberSpinner(
            current.userhostMinIntervalSeconds(), 1, 60, 1, closeables);
    userhostMinIntervalSeconds.setToolTipText(
        "Minimum seconds between USERHOST commands per server.");

    JSpinner userhostMaxPerMinute =
        PreferencesUiSupport.numberSpinner(
            current.userhostMaxCommandsPerMinute(), 1, 60, 1, closeables);
    userhostMaxPerMinute.setToolTipText("Maximum USERHOST commands per minute per server.");

    JSpinner userhostNickCooldownMinutes =
        PreferencesUiSupport.numberSpinner(
            current.userhostNickCooldownMinutes(), 1, 240, 1, closeables);
    userhostNickCooldownMinutes.setToolTipText(
        "Cooldown in minutes before re-querying the same nick.");

    JSpinner userhostMaxNicksPerCommand =
        PreferencesUiSupport.numberSpinner(
            current.userhostMaxNicksPerCommand(), 1, 5, 1, closeables);
    userhostMaxNicksPerCommand.setToolTipText(
        "How many nicks to include per USERHOST command (servers typically allow up to 5).");

    JPanel hostmaskAdvanced = new JPanel(MigLayouts.twoColumnForm(12, MigLayouts.rows(4, 6)));
    hostmaskAdvanced.setOpaque(false);
    hostmaskAdvanced.add(new JLabel("Min interval (sec):"));
    hostmaskAdvanced.add(userhostMinIntervalSeconds, MigConstraints.width(110));
    hostmaskAdvanced.add(new JLabel("Max commands/min:"));
    hostmaskAdvanced.add(userhostMaxPerMinute, MigConstraints.width(110));
    hostmaskAdvanced.add(new JLabel("Nick cooldown (min):"));
    hostmaskAdvanced.add(userhostNickCooldownMinutes, MigConstraints.width(110));
    hostmaskAdvanced.add(new JLabel("Max nicks/command:"));
    hostmaskAdvanced.add(userhostMaxNicksPerCommand, MigConstraints.width(110));

    JPanel enrichmentPanel = new JPanel(MigLayouts.twoColumnFormWithHideMode(8, 12, 3, ""));
    enrichmentPanel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Roster enrichment (fallback)"),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)));
    enrichmentPanel.setOpaque(false);

    JCheckBox enrichmentEnabled =
        new JCheckBox("Best-effort roster enrichment using USERHOST (rate-limited)");
    enrichmentEnabled.setSelected(current.userInfoEnrichmentEnabled());
    enrichmentEnabled.setToolTipText(
        "When enabled, IRCafe may send USERHOST occasionally to enrich user info even when you don't have hostmask-based ignore rules.\n"
            + "This is a best-effort fallback for older networks.");

    JCheckBox enrichmentWhoisFallbackEnabled =
        new JCheckBox("Also use WHOIS fallback for account info (very slow)");
    enrichmentWhoisFallbackEnabled.setSelected(current.userInfoEnrichmentWhoisFallbackEnabled());
    enrichmentWhoisFallbackEnabled.setToolTipText(
        "When enabled, IRCafe may occasionally send WHOIS to learn account login state/name and away message.\n"
            + "This is slower and more likely to hit server rate limits. Recommended OFF by default.");

    JCheckBox enrichmentPeriodicRefreshEnabled =
        new JCheckBox("Periodic background refresh (slow scan)");
    enrichmentPeriodicRefreshEnabled.setSelected(
        current.userInfoEnrichmentPeriodicRefreshEnabled());
    enrichmentPeriodicRefreshEnabled.setToolTipText(
        "When enabled, IRCafe will periodically re-check a small number of nicks to detect changes.\n"
            + "Use conservative intervals to avoid extra network load.");

    JButton enrichmentHelp =
        PreferencesUiSupport.whyHelpButton(
            "Why do I need roster enrichment?",
            "This is a best-effort fallback for older networks or edge cases where IRCv3 metadata isn't available.\n\n"
                + "IRCafe can use rate-limited USERHOST to fill missing user info. Optionally it can also use WHOIS (much slower) to learn account/away details.\n\n"
                + "On modern IRCv3 networks, you typically don't need this. Leave it OFF unless you have a specific reason.");

    JButton whoisHelp =
        PreferencesUiSupport.whyHelpButton(
            "WHOIS fallback",
            "WHOIS is the slowest and noisiest fallback. It can provide account and away information when IRCv3 isn't available, but it is easy to hit server throttles.\n\n"
                + "Keep this OFF unless you're on a network that doesn't provide account info via IRCv3.");

    JButton refreshHelp =
        PreferencesUiSupport.whyHelpButton(
            "Periodic background refresh",
            "This periodically re-probes a small number of users to detect changes (e.g., account/away state) on networks that don't push updates.\n\n"
                + "It's a slow scan by design: use high intervals and small batch sizes to avoid extra network load.");

    JTextArea enrichmentSummary = PreferencesUiSupport.subtleInfoText();
    enrichmentSummary.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));

    JSpinner enrichmentUserhostMinIntervalSeconds =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentUserhostMinIntervalSeconds(), 1, 300, 1, closeables);
    enrichmentUserhostMinIntervalSeconds.setToolTipText(
        "Minimum seconds between USERHOST commands per server for enrichment.");

    JSpinner enrichmentUserhostMaxPerMinute =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentUserhostMaxCommandsPerMinute(), 1, 60, 1, closeables);
    enrichmentUserhostMaxPerMinute.setToolTipText(
        "Maximum USERHOST commands per minute per server for enrichment.");

    JSpinner enrichmentUserhostNickCooldownMinutes =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentUserhostNickCooldownMinutes(), 1, 1440, 1, closeables);
    enrichmentUserhostNickCooldownMinutes.setToolTipText(
        "Cooldown in minutes before re-querying the same nick via USERHOST (enrichment).\n"
            + "Higher values reduce network load.");

    JSpinner enrichmentUserhostMaxNicksPerCommand =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentUserhostMaxNicksPerCommand(), 1, 5, 1, closeables);
    enrichmentUserhostMaxNicksPerCommand.setToolTipText(
        "How many nicks to include per USERHOST command (servers typically allow up to 5).\n"
            + "This applies to enrichment mode, separate from hostmask discovery.");

    JSpinner enrichmentWhoisMinIntervalSeconds =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentWhoisMinIntervalSeconds(), 5, 600, 5, closeables);
    enrichmentWhoisMinIntervalSeconds.setToolTipText(
        "Minimum seconds between WHOIS commands per server (enrichment).\n"
            + "Keep this high to avoid throttling.");

    JSpinner enrichmentWhoisNickCooldownMinutes =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentWhoisNickCooldownMinutes(), 1, 1440, 1, closeables);
    enrichmentWhoisNickCooldownMinutes.setToolTipText(
        "Cooldown in minutes before re-WHOIS'ing the same nick.");

    JSpinner enrichmentPeriodicRefreshIntervalSeconds =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentPeriodicRefreshIntervalSeconds(), 30, 3600, 30, closeables);
    enrichmentPeriodicRefreshIntervalSeconds.setToolTipText(
        "How often to run a slow scan tick (seconds).\n"
            + "Higher values are safer. Example: 300 seconds (5 minutes).");

    JSpinner enrichmentPeriodicRefreshNicksPerTick =
        PreferencesUiSupport.numberSpinner(
            current.userInfoEnrichmentPeriodicRefreshNicksPerTick(), 1, 20, 1, closeables);
    enrichmentPeriodicRefreshNicksPerTick.setToolTipText(
        "How many nicks to probe per periodic tick.\nKeep this small (e.g., 1-3).");

    JPanel enrichmentAdvanced =
        new JPanel(MigLayouts.twoColumnForm(12, MigLayouts.rowGaps(6, 6, 6, 10, 6, 6, 10, 6, 6)));
    enrichmentAdvanced.setOpaque(false);
    JLabel userhostHdr = new JLabel("USERHOST tuning");
    userhostHdr.setFont(userhostHdr.getFont().deriveFont(Font.BOLD));
    enrichmentAdvanced.add(userhostHdr, MigConstraints.span2GrowXMinWidth0Wrap());
    enrichmentAdvanced.add(new JLabel("Min interval (sec):"));
    enrichmentAdvanced.add(enrichmentUserhostMinIntervalSeconds, MigConstraints.width(110));
    enrichmentAdvanced.add(new JLabel("Max cmd/min:"));
    enrichmentAdvanced.add(enrichmentUserhostMaxPerMinute, MigConstraints.width(110));
    enrichmentAdvanced.add(new JLabel("Nick cooldown (min):"));
    enrichmentAdvanced.add(enrichmentUserhostNickCooldownMinutes, MigConstraints.width(110));
    enrichmentAdvanced.add(new JLabel("Max nicks/cmd:"));
    enrichmentAdvanced.add(enrichmentUserhostMaxNicksPerCommand, MigConstraints.width(110));
    JLabel whoisHdr = new JLabel("WHOIS tuning");
    whoisHdr.setFont(whoisHdr.getFont().deriveFont(Font.BOLD));
    enrichmentAdvanced.add(whoisHdr, MigConstraints.span2GrowXMinWidth0Wrap());
    enrichmentAdvanced.add(new JLabel("Min interval (sec):"));
    enrichmentAdvanced.add(enrichmentWhoisMinIntervalSeconds, MigConstraints.width(110));
    enrichmentAdvanced.add(new JLabel("Nick cooldown (min):"));
    enrichmentAdvanced.add(enrichmentWhoisNickCooldownMinutes, MigConstraints.width(110));
    JLabel refreshHdr = new JLabel("Periodic refresh tuning");
    refreshHdr.setFont(refreshHdr.getFont().deriveFont(Font.BOLD));
    enrichmentAdvanced.add(refreshHdr, MigConstraints.span2GrowXMinWidth0Wrap());
    enrichmentAdvanced.add(new JLabel("Interval (sec):"));
    enrichmentAdvanced.add(enrichmentPeriodicRefreshIntervalSeconds, MigConstraints.width(110));
    enrichmentAdvanced.add(new JLabel("Nicks per tick:"));
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
            hostmaskSummary.setText("Disabled");
            return;
          }
          int minInterval = PreferencesUiSupport.spinnerInt(userhostMinIntervalSeconds);
          int maxPerMinute = PreferencesUiSupport.spinnerInt(userhostMaxPerMinute);
          int cooldownMinutes = PreferencesUiSupport.spinnerInt(userhostNickCooldownMinutes);
          int maxNicks = PreferencesUiSupport.spinnerInt(userhostMaxNicksPerCommand);
          hostmaskSummary.setText(
              String.format(
                  "USERHOST ≤%d/min • min %ds • cooldown %dm • up to %d nicks/cmd",
                  maxPerMinute, minInterval, cooldownMinutes, maxNicks));
        };

    Runnable updateEnrichmentSummary =
        () -> {
          if (!enrichmentEnabled.isSelected()) {
            enrichmentSummary.setText("Disabled");
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
                String.format("WHOIS min %ds, cooldown %dm", whoisMinInterval, whoisCooldown);
          } else {
            whoisSummary = "WHOIS off";
          }

          String refreshSummary;
          if (enrichmentPeriodicRefreshEnabled.isSelected()) {
            int refreshInterval =
                PreferencesUiSupport.spinnerInt(enrichmentPeriodicRefreshIntervalSeconds);
            int refreshNicks =
                PreferencesUiSupport.spinnerInt(enrichmentPeriodicRefreshNicksPerTick);
            refreshSummary = String.format("Refresh %ds ×%d", refreshInterval, refreshNicks);
          } else {
            refreshSummary = "Refresh off";
          }

          enrichmentSummary.setText(
              String.format(
                  "USERHOST ≤%d/min • min %ds • cooldown %dm • up to %d nicks/cmd\n%s • %s",
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

    lookupsTabs.addTab("Overview", PreferencesUiSupport.padSubTab(lookupsOverview));
    lookupsTabs.addTab("Hostmask discovery", PreferencesUiSupport.padSubTab(hostmaskPanel));
    lookupsTabs.addTab("Roster enrichment", PreferencesUiSupport.padSubTab(enrichmentPanel));

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
    CONSERVATIVE("Conservative"),
    BALANCED("Balanced"),
    RAPID("Rapid"),
    CUSTOM("Custom");

    private final String label;

    LookupRatePreset(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
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

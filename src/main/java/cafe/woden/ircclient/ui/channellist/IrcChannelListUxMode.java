package cafe.woden.ircclient.ui.channellist;

import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

final class IrcChannelListUxMode implements ChannelListUxMode {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();
  private static final String DEFAULT_HINT = MESSAGES.text("channelList.irc.defaultHint");
  private static final ActionPresentation PRESENTATION =
      new ActionPresentation(
          MESSAGES.text("channelList.irc.action.fullList.tooltip"),
          MESSAGES.text("channelList.irc.action.fullList.accessibleName"),
          MESSAGES.text("channelList.irc.action.alis.tooltip"),
          MESSAGES.text("channelList.irc.action.alis.accessibleName"),
          false,
          MESSAGES.text("channelList.action.nextPage.tooltip"),
          MESSAGES.text("channelList.action.nextPage.accessibleName"));

  @Override
  public String defaultHint() {
    return DEFAULT_HINT;
  }

  @Override
  public ActionPresentation actionPresentation() {
    return PRESENTATION;
  }

  @Override
  public void runPrimaryAction(Context context, String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return;
    if (!context.confirmFullListRequest()) return;
    context.rememberRequestType(sid, ChannelListRequestType.FULL_LIST);
    context.clearFilterText();
    context.emitRunListRequest();
  }

  @Override
  public void runSecondaryAction(Context context, String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return;

    JTextField queryField = new JTextField(28);
    JCheckBox includeTopic =
        new JCheckBox(MESSAGES.text("channelList.irc.alis.includeTopic"), true);
    JCheckBox minEnabled = new JCheckBox(MESSAGES.text("channelList.irc.alis.minUsers"));
    JSpinner minUsers = new JSpinner(new SpinnerNumberModel(10, 0, 1_000_000, 1));
    JCheckBox maxEnabled = new JCheckBox(MESSAGES.text("channelList.irc.alis.maxUsers"));
    JSpinner maxUsers = new JSpinner(new SpinnerNumberModel(500, 0, 1_000_000, 1));
    JCheckBox skipEnabled = new JCheckBox(MESSAGES.text("channelList.irc.alis.skip"));
    JSpinner skipCount = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
    JCheckBox showModes =
        new JCheckBox(MESSAGES.text("channelList.irc.alis.showModes"), false);
    JCheckBox showTopicSetter =
        new JCheckBox(MESSAGES.text("channelList.irc.alis.showTopicSetter"), false);
    JComboBox<String> registrationScope =
        new JComboBox<>(
            new String[] {
              MESSAGES.text("channelList.irc.alis.registration.any"),
              MESSAGES.text("channelList.irc.alis.registration.registeredOnly"),
              MESSAGES.text("channelList.irc.alis.registration.unregisteredOnly")
            });
    JPanel showFlagsPanel = new JPanel(MigLayouts.fillX(MigLayoutConstraints.LEADING_GROW, "[]"));
    showFlagsPanel.add(showModes);
    showFlagsPanel.add(showTopicSetter, MigConstraints.gapLeft(10));

    minUsers.setEnabled(false);
    maxUsers.setEnabled(false);
    skipCount.setEnabled(false);
    minEnabled.addActionListener(e -> minUsers.setEnabled(minEnabled.isSelected()));
    maxEnabled.addActionListener(e -> maxUsers.setEnabled(maxEnabled.isSelected()));
    skipEnabled.addActionListener(e -> skipCount.setEnabled(skipEnabled.isSelected()));

    JPanel form = new JPanel(MigLayouts.twoColumnForm(0, MigLayouts.rows(8, 6)));
    form.add(new JLabel(MESSAGES.text("channelList.irc.alis.field.query")));
    form.add(queryField, MigConstraints.growX());
    form.add(new JLabel(MESSAGES.text("channelList.irc.alis.field.topicFilter")));
    form.add(includeTopic, MigConstraints.growX());
    form.add(minEnabled);
    form.add(minUsers, MigConstraints.width(120));
    form.add(maxEnabled);
    form.add(maxUsers, MigConstraints.width(120));
    form.add(skipEnabled);
    form.add(skipCount, MigConstraints.width(120));
    form.add(new JLabel(MESSAGES.text("channelList.irc.alis.field.displayExtras")));
    form.add(showFlagsPanel, MigConstraints.growX());
    form.add(new JLabel(MESSAGES.text("channelList.irc.alis.field.registration")));
    form.add(registrationScope, MigConstraints.growX());

    int choice =
        JOptionPane.showConfirmDialog(
            context.ownerWindow(),
            form,
            MESSAGES.text("channelList.irc.alis.title"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
    if (choice != JOptionPane.OK_OPTION) return;

    String query = Objects.toString(queryField.getText(), "").trim();
    Integer minUsersValue =
        minEnabled.isSelected() ? ((Number) minUsers.getValue()).intValue() : null;
    Integer maxUsersValue =
        maxEnabled.isSelected() ? ((Number) maxUsers.getValue()).intValue() : null;
    Integer skipValue =
        skipEnabled.isSelected() ? ((Number) skipCount.getValue()).intValue() : null;
    if (minUsersValue != null && maxUsersValue != null && minUsersValue > maxUsersValue) {
      int t = minUsersValue;
      minUsersValue = maxUsersValue;
      maxUsersValue = t;
    }
    ChannelListPanel.AlisRegistrationFilter registrationFilter =
        switch (registrationScope.getSelectedIndex()) {
          case 1 -> ChannelListPanel.AlisRegistrationFilter.REGISTERED_ONLY;
          case 2 -> ChannelListPanel.AlisRegistrationFilter.UNREGISTERED_ONLY;
          default -> ChannelListPanel.AlisRegistrationFilter.ANY;
        };
    ChannelListPanel.AlisSearchOptions options =
        new ChannelListPanel.AlisSearchOptions(
            includeTopic.isSelected(),
            minUsersValue,
            maxUsersValue,
            skipValue,
            showModes.isSelected(),
            showTopicSetter.isSelected(),
            registrationFilter);
    String cmd = buildAlisCommand(query, options);

    context.rememberRequestType(sid, ChannelListRequestType.ALIS);
    context.beginList(sid, MESSAGES.text("channelList.irc.alis.loading"));
    context.emitRunCommand(cmd);
  }

  @Override
  public void runPagingAction(Context context, String serverId) {}

  @Override
  public void onBeginList(String serverId, String banner) {}

  @Override
  public void onEndList(String serverId, String summary) {}

  @Override
  public boolean isPagingActionEnabled(String serverId) {
    return false;
  }

  @Override
  public ChannelListRequestType inferRequestTypeFromBanner(String banner) {
    String text = Objects.toString(banner, "").trim().toLowerCase(Locale.ROOT);
    if (text.contains("alis")) return ChannelListRequestType.ALIS;
    return ChannelListRequestType.UNKNOWN;
  }

  static String buildAlisCommand(String query, ChannelListPanel.AlisSearchOptions options) {
    ChannelListPanel.AlisSearchOptions opts =
        options == null ? ChannelListPanel.AlisSearchOptions.defaults(false) : options;
    String q = Objects.toString(query, "").trim();
    StringBuilder raw = new StringBuilder("LIST ");
    raw.append(opts.includeTopic() ? "*" : (q.isEmpty() ? "*" : q));
    if (opts.includeTopic()) {
      raw.append(" -topic");
      raw.append(" ").append(q.isEmpty() ? "*" : q);
    }
    if (opts.minUsers() != null && opts.minUsers() >= 0) {
      raw.append(" -min ").append(opts.minUsers());
    }
    if (opts.maxUsers() != null && opts.maxUsers() >= 0) {
      raw.append(" -max ").append(opts.maxUsers());
    }
    if (opts.skipCount() != null && opts.skipCount() > 0) {
      raw.append(" -skip ").append(opts.skipCount());
    }

    StringBuilder showFlags = new StringBuilder();
    if (opts.showModes()) showFlags.append("m");
    if (opts.showTopicSetter()) showFlags.append("t");
    if (!showFlags.isEmpty()) {
      raw.append(" -show ").append(showFlags);
    }

    ChannelListPanel.AlisRegistrationFilter registration =
        opts.registrationFilter() == null
            ? ChannelListPanel.AlisRegistrationFilter.ANY
            : opts.registrationFilter();
    if (registration == ChannelListPanel.AlisRegistrationFilter.REGISTERED_ONLY) {
      raw.append(" -show r");
    } else if (registration == ChannelListPanel.AlisRegistrationFilter.UNREGISTERED_ONLY) {
      raw.append(" -show u");
    }

    return "/quote PRIVMSG ALIS :" + raw.toString().trim();
  }

  private static String normalizeServerId(String serverId) {
    return Objects.toString(serverId, "").trim();
  }
}

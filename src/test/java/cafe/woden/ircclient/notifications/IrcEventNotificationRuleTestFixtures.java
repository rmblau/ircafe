package cafe.woden.ircclient.notifications;

import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.model.IrcEventNotificationRule;

/** Test fixture builder for {@link IrcEventNotificationRule}. */
public final class IrcEventNotificationRuleTestFixtures {

  private IrcEventNotificationRuleTestFixtures() {}

  public static Builder rule() {
    return new Builder();
  }

  public static final class Builder {
    private boolean enabled = true;
    private IrcEventNotificationRule.EventType eventType = IrcEventNotificationRule.EventType.INVITE_RECEIVED;
    private IrcEventNotificationRule.SourceMode sourceMode = IrcEventNotificationRule.SourceMode.ANY;
    private String sourcePattern;
    private IrcEventNotificationRule.ChannelScope channelScope = IrcEventNotificationRule.ChannelScope.ALL;
    private String channelPatterns;
    private boolean toastEnabled = true;
    private IrcEventNotificationRule.FocusScope focusScope = IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY;
    private boolean statusBarEnabled = true;
    private boolean notificationsNodeEnabled = true;
    private boolean soundEnabled;
    private String soundId = BuiltInSound.NOTIF_1.name();
    private boolean soundUseCustom;
    private String soundCustomPath;
    private boolean scriptEnabled;
    private String scriptPath;
    private String scriptArgs;
    private String scriptWorkingDirectory;
    private IrcEventNotificationRule.CtcpMatchMode ctcpCommandMode = IrcEventNotificationRule.CtcpMatchMode.ANY;
    private String ctcpCommandPattern;
    private IrcEventNotificationRule.CtcpMatchMode ctcpValueMode = IrcEventNotificationRule.CtcpMatchMode.ANY;
    private String ctcpValuePattern;

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder eventType(IrcEventNotificationRule.EventType eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder sourceMode(IrcEventNotificationRule.SourceMode sourceMode) {
      this.sourceMode = sourceMode;
      return this;
    }

    public Builder sourcePattern(String sourcePattern) {
      this.sourcePattern = sourcePattern;
      return this;
    }

    public Builder channelScope(IrcEventNotificationRule.ChannelScope channelScope) {
      this.channelScope = channelScope;
      return this;
    }

    public Builder channelPatterns(String channelPatterns) {
      this.channelPatterns = channelPatterns;
      return this;
    }

    public Builder toastEnabled(boolean toastEnabled) {
      this.toastEnabled = toastEnabled;
      return this;
    }

    public Builder focusScope(IrcEventNotificationRule.FocusScope focusScope) {
      this.focusScope = focusScope;
      return this;
    }

    public Builder statusBarEnabled(boolean statusBarEnabled) {
      this.statusBarEnabled = statusBarEnabled;
      return this;
    }

    public Builder notificationsNodeEnabled(boolean notificationsNodeEnabled) {
      this.notificationsNodeEnabled = notificationsNodeEnabled;
      return this;
    }

    public Builder soundEnabled(boolean soundEnabled) {
      this.soundEnabled = soundEnabled;
      return this;
    }

    public Builder soundId(String soundId) {
      this.soundId = soundId;
      return this;
    }

    public Builder soundUseCustom(boolean soundUseCustom) {
      this.soundUseCustom = soundUseCustom;
      return this;
    }

    public Builder soundCustomPath(String soundCustomPath) {
      this.soundCustomPath = soundCustomPath;
      return this;
    }

    public Builder scriptEnabled(boolean scriptEnabled) {
      this.scriptEnabled = scriptEnabled;
      return this;
    }

    public Builder scriptPath(String scriptPath) {
      this.scriptPath = scriptPath;
      return this;
    }

    public Builder scriptArgs(String scriptArgs) {
      this.scriptArgs = scriptArgs;
      return this;
    }

    public Builder scriptWorkingDirectory(String scriptWorkingDirectory) {
      this.scriptWorkingDirectory = scriptWorkingDirectory;
      return this;
    }

    public Builder ctcpCommandMode(IrcEventNotificationRule.CtcpMatchMode ctcpCommandMode) {
      this.ctcpCommandMode = ctcpCommandMode;
      return this;
    }

    public Builder ctcpCommandPattern(String ctcpCommandPattern) {
      this.ctcpCommandPattern = ctcpCommandPattern;
      return this;
    }

    public Builder ctcpValueMode(IrcEventNotificationRule.CtcpMatchMode ctcpValueMode) {
      this.ctcpValueMode = ctcpValueMode;
      return this;
    }

    public Builder ctcpValuePattern(String ctcpValuePattern) {
      this.ctcpValuePattern = ctcpValuePattern;
      return this;
    }

    public IrcEventNotificationRule build() {
      return new IrcEventNotificationRule(
          enabled,
          eventType,
          sourceMode,
          sourcePattern,
          channelScope,
          channelPatterns,
          toastEnabled,
          focusScope,
          statusBarEnabled,
          notificationsNodeEnabled,
          soundEnabled,
          soundId,
          soundUseCustom,
          soundCustomPath,
          scriptEnabled,
          scriptPath,
          scriptArgs,
          scriptWorkingDirectory,
          ctcpCommandMode,
          ctcpCommandPattern,
          ctcpValueMode,
          ctcpValuePattern);
    }
  }
}

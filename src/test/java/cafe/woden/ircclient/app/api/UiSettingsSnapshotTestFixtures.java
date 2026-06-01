package cafe.woden.ircclient.app.api;

import cafe.woden.ircclient.config.api.NotificationRule;
import java.util.Arrays;
import java.util.List;

/** Test fixtures for creating {@link UiSettingsSnapshot} without repeating the full constructor. */
public final class UiSettingsSnapshotTestFixtures {

  private UiSettingsSnapshotTestFixtures() {}

  public static Builder builder() {
    return new Builder();
  }

  public static UiSettingsSnapshot defaults() {
    return builder().build();
  }

  public static UiSettingsSnapshot withNotificationRules(NotificationRule... notificationRules) {
    return builder().notificationRules(notificationRules).build();
  }

  public static final class Builder {
    private List<NotificationRule> notificationRules = List.of();
    private int notificationRuleCooldownSeconds = 15;
    private int monitorIsonFallbackPollIntervalSeconds = 30;
    private boolean ctcpRequestsInActiveTargetEnabled = true;
    private boolean typingIndicatorsReceiveEnabled = true;
    private boolean typingIndicatorsTreeEnabled = true;
    private boolean typingIndicatorsUsersListEnabled = true;
    private boolean typingIndicatorsTranscriptEnabled = true;

    private Builder() {}

    public Builder notificationRules(List<NotificationRule> notificationRules) {
      this.notificationRules =
          notificationRules == null ? List.of() : List.copyOf(notificationRules);
      return this;
    }

    public Builder notificationRules(NotificationRule... notificationRules) {
      this.notificationRules =
          notificationRules == null ? List.of() : Arrays.stream(notificationRules).toList();
      return this;
    }

    public Builder notificationRuleCooldownSeconds(int notificationRuleCooldownSeconds) {
      this.notificationRuleCooldownSeconds = notificationRuleCooldownSeconds;
      return this;
    }

    public Builder monitorIsonFallbackPollIntervalSeconds(
        int monitorIsonFallbackPollIntervalSeconds) {
      this.monitorIsonFallbackPollIntervalSeconds = monitorIsonFallbackPollIntervalSeconds;
      return this;
    }

    public Builder ctcpRequestsInActiveTargetEnabled(boolean ctcpRequestsInActiveTargetEnabled) {
      this.ctcpRequestsInActiveTargetEnabled = ctcpRequestsInActiveTargetEnabled;
      return this;
    }

    public Builder typingIndicatorsReceiveEnabled(boolean typingIndicatorsReceiveEnabled) {
      this.typingIndicatorsReceiveEnabled = typingIndicatorsReceiveEnabled;
      return this;
    }

    public Builder typingIndicatorsTreeEnabled(boolean typingIndicatorsTreeEnabled) {
      this.typingIndicatorsTreeEnabled = typingIndicatorsTreeEnabled;
      return this;
    }

    public Builder typingIndicatorsUsersListEnabled(boolean typingIndicatorsUsersListEnabled) {
      this.typingIndicatorsUsersListEnabled = typingIndicatorsUsersListEnabled;
      return this;
    }

    public Builder typingIndicatorsTranscriptEnabled(boolean typingIndicatorsTranscriptEnabled) {
      this.typingIndicatorsTranscriptEnabled = typingIndicatorsTranscriptEnabled;
      return this;
    }

    public UiSettingsSnapshot build() {
      return new UiSettingsSnapshot(
          notificationRules,
          notificationRuleCooldownSeconds,
          monitorIsonFallbackPollIntervalSeconds,
          ctcpRequestsInActiveTargetEnabled,
          typingIndicatorsReceiveEnabled,
          typingIndicatorsTreeEnabled,
          typingIndicatorsUsersListEnabled,
          typingIndicatorsTranscriptEnabled);
    }
  }
}

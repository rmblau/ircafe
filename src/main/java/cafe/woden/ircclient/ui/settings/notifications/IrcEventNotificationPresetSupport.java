package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.ui.localization.UiMessages;
import java.util.List;

final class IrcEventNotificationPresetSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private IrcEventNotificationPresetSupport() {}

  static List<IrcEventNotificationRule> buildPreset(Preset preset) {
    if (preset == null) return List.of();
    return IrcEventNotificationRule.preset(preset.name());
  }

  static BuiltInSound defaultBuiltInSoundForEvent(IrcEventNotificationRule.EventType eventType) {
    return IrcEventNotificationRule.defaultBuiltInSoundForEvent(eventType);
  }

  enum Preset {
    ESSENTIAL("preferences.notifications.ircEvents.preset.essential"),
    MODERATION("preferences.notifications.ircEvents.preset.moderation"),
    ALL_EVENTS("preferences.notifications.ircEvents.preset.allEvents");

    private final String labelKey;

    Preset(String labelKey) {
      this.labelKey = labelKey;
    }

    @Override
    public String toString() {
      return MESSAGES.text(labelKey);
    }
  }
}

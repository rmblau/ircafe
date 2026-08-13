package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IrcEventNotificationSoundSelectionPlannerTest {

  @Test
  void updatesBuiltInSoundWhenCurrentSoundStillMatchesPreviousEventDefault() {
    IrcEventNotificationSoundSelectionPlan plan =
        IrcEventNotificationSoundSelectionPlanner.planDefaultSoundForEventChange(
            "INVITE_RECEIVED", "PRIVATE_MESSAGE_RECEIVED", "CHANNEL_INVITE_1", false);

    assertTrue(plan.updateBuiltInSound());
    assertEquals("PM_RECEIVED_1", plan.soundId());
  }

  @Test
  void preservesManualBuiltInSoundSelection() {
    IrcEventNotificationSoundSelectionPlan plan =
        IrcEventNotificationSoundSelectionPlanner.planDefaultSoundForEventChange(
            "INVITE_RECEIVED", "PRIVATE_MESSAGE_RECEIVED", "NOTIF_3", false);

    assertFalse(plan.updateBuiltInSound());
  }

  @Test
  void preservesBuiltInSoundWhenCustomSoundIsSelected() {
    IrcEventNotificationSoundSelectionPlan plan =
        IrcEventNotificationSoundSelectionPlanner.planDefaultSoundForEventChange(
            "INVITE_RECEIVED", "PRIVATE_MESSAGE_RECEIVED", "CHANNEL_INVITE_1", true);

    assertFalse(plan.updateBuiltInSound());
  }

  @Test
  void normalizesEventAndSoundIds() {
    IrcEventNotificationSoundSelectionPlan plan =
        IrcEventNotificationSoundSelectionPlanner.planDefaultSoundForEventChange(
            " invite_received ", " private_message_received ", " channel_invite_1 ", false);

    assertTrue(plan.updateBuiltInSound());
    assertEquals("PM_RECEIVED_1", plan.soundId());
  }
}

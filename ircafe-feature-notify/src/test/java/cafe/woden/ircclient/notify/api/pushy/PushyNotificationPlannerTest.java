package cafe.woden.ircclient.notify.api.pushy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PushyNotificationPlannerTest {

  @Test
  void skipsEventsWhenSettingsAreDisabledOrUnconfigured() {
    PushyNotificationEvent event =
        new PushyNotificationEvent("MENTION", "libera", "#ircafe", "alice", false, "Mention", "hi");

    assertFalse(PushyNotificationPlanner.planEvent(null, event, 100L).sendable());
    assertFalse(
        PushyNotificationPlanner.planEvent(
                new PushyNotificationSettings(
                    true, "https://push.example/push", null, "device-token", null, "IRCafe", 5, 8),
                event,
                100L)
            .sendable());
    assertFalse(
        PushyNotificationPlanner.planEvent(
                new PushyNotificationSettings(
                    true, "https://push.example/push", "secret", null, null, "IRCafe", 5, 8),
                event,
                100L)
            .sendable());
  }

  @Test
  void plansEventPayloadWithDeviceTokenAndEncodedApiKey() {
    PushyNotificationSettings settings =
        new PushyNotificationSettings(
            true,
            "https://push.example/push",
            "secret key",
            "device-token",
            "topic-name",
            "IRCafe",
            5,
            8);
    PushyNotificationEvent event =
        new PushyNotificationEvent(
            "PRIVATE_MESSAGE_RECEIVED",
            "libera",
            "#ircafe",
            "alice",
            false,
            "Private message",
            "hello");

    PushyNotificationPlan plan = PushyNotificationPlanner.planEvent(settings, event, 12345L);

    assertTrue(plan.sendable());
    assertEquals("https://push.example/push?api_key=secret+key", plan.url());
    assertEquals(
        "{\"to\":\"device-token\",\"notification\":{\"title\":\"IRCafe - Private message\","
            + "\"body\":\"hello\"},\"data\":{\"eventType\":\"PRIVATE_MESSAGE_RECEIVED\","
            + "\"serverId\":\"libera\",\"channel\":\"#ircafe\",\"sourceNick\":\"alice\","
            + "\"sourceIsSelf\":\"false\",\"timestampMs\":\"12345\"}}",
        plan.payload());
  }

  @Test
  void usesTopicWhenDeviceTokenIsMissingAndEscapesPayloadValues() {
    PushyNotificationSettings settings =
        new PushyNotificationSettings(
            true, "https://push.example/push", "secret", null, "ops", "IRCafe", 5, 8);
    PushyNotificationEvent event =
        new PushyNotificationEvent(
            "NOTICE", "libera", "status", "bob", null, "IRCafe Alert", "line one\n\"quoted\"");

    PushyNotificationPlan plan = PushyNotificationPlanner.planEvent(settings, event, 7L);

    assertTrue(plan.sendable());
    assertTrue(plan.payload().contains("\"topic\":\"ops\""));
    assertTrue(plan.payload().contains("\"title\":\"IRCafe Alert\""));
    assertTrue(plan.payload().contains("\"body\":\"line one\\n\\\"quoted\\\"\""));
    assertTrue(plan.payload().contains("\"sourceIsSelf\":\"unknown\""));
  }

  @Test
  void plansTestFailuresWithUserFacingMessages() {
    assertEquals(
        PushyNotificationPlanner.DISABLED_MESSAGE,
        PushyNotificationPlanner.planTest(null, "Test", "Body", 1L).failureMessage());
    assertEquals(
        PushyNotificationPlanner.MISSING_CREDENTIALS_MESSAGE,
        PushyNotificationPlanner.planTest(
                new PushyNotificationSettings(true, null, null, "device", null, "IRCafe", 5, 8),
                "Test",
                "Body",
                1L)
            .failureMessage());
    assertEquals(
        PushyNotificationPlanner.MISSING_DESTINATION_MESSAGE,
        PushyNotificationPlanner.planTest(
                new PushyNotificationSettings(
                    true, "https://push.example/push", "secret", null, null, "IRCafe", 5, 8),
                "Test",
                "Body",
                1L)
            .failureMessage());
  }

  @Test
  void plansTestPayloadWithDefaultBody() {
    PushyNotificationPlan plan =
        PushyNotificationPlanner.planTest(
            new PushyNotificationSettings(
                true, "https://push.example/push", "secret", "device", null, "IRCafe", 5, 8),
            " ",
            " ",
            42L);

    assertTrue(plan.sendable());
    assertTrue(plan.payload().contains("\"title\":\"IRCafe - IRC Event\""));
    assertTrue(plan.payload().contains("\"body\":\"Pushy integration test from IRCafe.\""));
    assertTrue(plan.payload().contains("\"eventType\":\"PRIVATE_MESSAGE_RECEIVED\""));
    assertTrue(plan.payload().contains("\"timestampMs\":\"42\""));
  }
}

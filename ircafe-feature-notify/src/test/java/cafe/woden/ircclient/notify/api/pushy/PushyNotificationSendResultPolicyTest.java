package cafe.woden.ircclient.notify.api.pushy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PushyNotificationSendResultPolicyTest {

  @Test
  void mapsSuccessfulHttpStatusToSuccessMessage() {
    PushyNotificationSendResult result =
        PushyNotificationSendResultPolicy.fromHttpResponse(202, "accepted");

    assertTrue(result.success());
    assertEquals("Push sent (HTTP 202).", result.message());
    assertEquals("", result.diagnosticBody());
  }

  @Test
  void mapsFailedHttpStatusAndTrimsDiagnosticBody() {
    String body = "x".repeat(260);

    PushyNotificationSendResult result =
        PushyNotificationSendResultPolicy.fromHttpResponse(401, body);

    assertFalse(result.success());
    assertEquals("Pushy request failed (401).", result.message());
    assertEquals(243, result.diagnosticBody().length());
    assertTrue(result.diagnosticBody().endsWith("..."));
  }

  @Test
  void mapsExceptionMessageOrClassNameToFailureMessage() {
    PushyNotificationSendResult withMessage =
        PushyNotificationSendResultPolicy.fromException(new IllegalStateException("boom"));
    PushyNotificationSendResult withoutMessage =
        PushyNotificationSendResultPolicy.fromException(new IllegalStateException());

    assertFalse(withMessage.success());
    assertEquals("boom", withMessage.message());
    assertEquals("IllegalStateException", withoutMessage.message());
  }
}

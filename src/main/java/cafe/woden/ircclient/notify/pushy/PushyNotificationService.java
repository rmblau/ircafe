package cafe.woden.ircclient.notify.pushy;

import cafe.woden.ircclient.config.execution.ExecutorConfig;
import cafe.woden.ircclient.config.properties.PushyProperties;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.net.HttpHeaderNames;
import cafe.woden.ircclient.notify.api.PushyNotificationPort;
import cafe.woden.ircclient.notify.api.pushy.PushyNotificationEvent;
import cafe.woden.ircclient.notify.api.pushy.PushyNotificationPlan;
import cafe.woden.ircclient.notify.api.pushy.PushyNotificationPlanner;
import cafe.woden.ircclient.notify.api.pushy.PushyNotificationSendResult;
import cafe.woden.ircclient.notify.api.pushy.PushyNotificationSendResultPolicy;
import cafe.woden.ircclient.notify.api.pushy.PushyNotificationSettings;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Optional Pushy integration for IRC event notifications.
 *
 * <p>This is intentionally best-effort and never throws to callers.
 */
@Component
@Lazy
@ApplicationLayer
public class PushyNotificationService implements PushyNotificationPort {

  private static final Logger log = LoggerFactory.getLogger(PushyNotificationService.class);

  private final PushySettingsBus settingsBus;
  private final ExecutorService executor;

  public PushyNotificationService(
      PushySettingsBus settingsBus,
      @Qualifier(ExecutorConfig.PUSHY_NOTIFICATION_EXECUTOR) ExecutorService executor) {
    this.settingsBus = settingsBus;
    this.executor = executor;
  }

  public boolean notifyEvent(
      IrcEventNotificationRule.EventType eventType,
      String serverId,
      String channel,
      String sourceNick,
      Boolean sourceIsSelf,
      String title,
      String body) {
    PushyProperties properties = currentProperties();
    PushyNotificationSettings settings = toFeatureSettings(properties);
    PushyNotificationEvent event =
        new PushyNotificationEvent(
            eventType != null ? eventType.name() : null,
            serverId,
            channel,
            sourceNick,
            sourceIsSelf,
            title,
            body);
    PushyNotificationPlan plan =
        PushyNotificationPlanner.planEvent(settings, event, System.currentTimeMillis());
    if (!plan.sendable()) return false;

    executor.execute(() -> sendPush(settings, plan.url(), plan.payload()));
    return true;
  }

  public PushResult sendTestNotification(PushyProperties settings, String title, String body) {
    PushyProperties properties = settings != null ? settings : currentProperties();
    PushyNotificationSettings featureSettings = toFeatureSettings(properties);
    PushyNotificationPlan plan =
        PushyNotificationPlanner.planTest(featureSettings, title, body, System.currentTimeMillis());
    if (!plan.sendable()) {
      String failure = plan.failureMessage();
      if (failure.isBlank()) failure = "Pushy request is incomplete.";
      return PushResult.failed(failure);
    }

    return sendPush(featureSettings, plan.url(), plan.payload());
  }

  private PushResult sendPush(PushyNotificationSettings settings, String url, String payload) {
    try {
      HttpClient client =
          HttpClient.newBuilder()
              .connectTimeout(Duration.ofSeconds(settings.connectTimeoutSeconds()))
              .build();
      HttpRequest request =
          HttpRequest.newBuilder(URI.create(url))
              .timeout(Duration.ofSeconds(settings.readTimeoutSeconds()))
              .header(HttpHeaderNames.CONTENT_TYPE, "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
              .build();

      HttpResponse<String> response =
          client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      int status = response.statusCode();
      PushyNotificationSendResult result =
          PushyNotificationSendResultPolicy.fromHttpResponse(status, response.body());
      if (!result.success()) {
        log.warn(
            "[ircafe] Pushy request failed: status={} body={}", status, result.diagnosticBody());
      }
      return toPushResult(result);
    } catch (Exception e) {
      log.debug("[ircafe] Pushy request failed", e);
      return toPushResult(PushyNotificationSendResultPolicy.fromException(e));
    }
  }

  private PushyProperties currentProperties() {
    return settingsBus != null ? settingsBus.get() : null;
  }

  private static PushyNotificationSettings toFeatureSettings(PushyProperties properties) {
    if (properties == null) {
      return PushyNotificationSettings.disabled();
    }
    PushyProperties p = properties;
    return PushyNotificationSettings.fromRuntime(
        p.enabled(),
        p.endpoint(),
        p.apiKey(),
        p.deviceToken(),
        p.topic(),
        p.titlePrefix(),
        p.connectTimeoutSeconds(),
        p.readTimeoutSeconds());
  }

  private static PushResult toPushResult(PushyNotificationSendResult result) {
    if (result == null) {
      return PushResult.failed("Pushy request failed.");
    }
    return result.success()
        ? PushResult.success(result.message())
        : PushResult.failed(result.message());
  }
}

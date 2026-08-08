package cafe.woden.ircclient.notifications;

import cafe.woden.ircclient.app.api.IrcEventNotifierPort;
import cafe.woden.ircclient.app.api.TrayNotificationsPort;
import cafe.woden.ircclient.config.execution.ExecutorConfig;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.notifications.api.IrcEventNotificationRuleAdapters;
import cafe.woden.ircclient.notify.api.PushyNotificationPort;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationActionPlan;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationActionPlanner;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationDispatchContext;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationDispatchPreflightPlan;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationDispatchPreflightPlanner;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationMatchPolicy;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEvaluation;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEvaluator;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationScriptAction;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationScriptPlan;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationScriptPlanner;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationTrayAction;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Evaluates configured IRC-event notification rules and dispatches matching notification actions.
 */
@Component
@Lazy
@SecondaryAdapter
@ApplicationLayer
@RequiredArgsConstructor
public class IrcEventNotificationService implements IrcEventNotifierPort {

  private static final Logger log = LoggerFactory.getLogger(IrcEventNotificationService.class);
  private static final long SCRIPT_TIMEOUT_SECONDS = 8L;

  private final IrcEventNotificationRulesBus rulesBus;
  private final TrayNotificationsPort trayNotificationService;
  private final NotificationStore notificationStore;
  private final PushyNotificationPort pushyNotificationService;

  @Qualifier(ExecutorConfig.IRC_EVENT_SCRIPT_EXECUTOR)
  private final ExecutorService scriptExecutor;

  /** Returns true if at least one rule matched and actions were evaluated. */
  @Override
  public boolean notifyConfigured(
      IrcEventNotificationRule.EventType eventType,
      String serverId,
      String channel,
      String sourceNick,
      Boolean sourceIsSelf,
      String title,
      String body,
      String activeServerId,
      String activeTarget) {
    return notifyConfigured(
        eventType,
        serverId,
        channel,
        sourceNick,
        sourceIsSelf,
        title,
        body,
        activeServerId,
        activeTarget,
        null,
        null);
  }

  @Override
  public boolean notifyConfigured(
      IrcEventNotificationRule.EventType eventType,
      String serverId,
      String channel,
      String sourceNick,
      Boolean sourceIsSelf,
      String title,
      String body,
      String activeServerId,
      String activeTarget,
      String ctcpCommand,
      String ctcpValue) {
    List<IrcEventNotificationRule> rules = rulesBus != null ? rulesBus.get() : List.of();
    IrcEventNotificationDispatchPreflightPlan preflight =
        IrcEventNotificationDispatchPreflightPlanner.plan(
            eventType != null ? eventType.name() : null, rules != null ? rules.size() : 0);
    if (!preflight.shouldEvaluate()) return false;

    IrcEventNotificationRuleEvaluation evaluation =
        IrcEventNotificationRuleEvaluator.evaluate(
            IrcEventNotificationRuleAdapters.toMatchRules(rules),
            preflight.eventTypeName(),
            eventType.toString(),
            serverId,
            channel,
            sourceNick,
            sourceIsSelf,
            title,
            body,
            activeServerId,
            activeTarget,
            ctcpCommand,
            ctcpValue);
    if (!evaluation.valid() || !evaluation.anyMatched()) return false;

    IrcEventNotificationDispatchContext context = evaluation.context();
    for (Integer matchedIndex : evaluation.matchedRuleIndexes()) {
      if (!preflight.matchedRuleIndexValid(matchedIndex)) continue;
      IrcEventNotificationRule matched = rules.get(matchedIndex);
      if (matched == null) continue;
      dispatchMatchedRule(
          matched,
          eventType,
          context.serverId(),
          context.target(),
          context.sourceNick(),
          sourceIsSelf,
          context.title(),
          context.body(),
          ctcpCommand,
          ctcpValue);
    }

    return true;
  }

  private void dispatchMatchedRule(
      IrcEventNotificationRule matched,
      IrcEventNotificationRule.EventType eventType,
      String sid,
      String target,
      String source,
      Boolean sourceIsSelf,
      String title,
      String body,
      String ctcpCommand,
      String ctcpValue) {
    if (matched == null) return;

    IrcEventNotificationActionPlan actionPlan =
        IrcEventNotificationActionPlanner.plan(
            IrcEventNotificationRuleAdapters.toActionRule(matched));

    if (actionPlan.recordNotification() && notificationStore != null) {
      notificationStore.recordIrcEvent(sid, target, source, title, body);
    }

    IrcEventNotificationTrayAction trayAction = actionPlan.trayAction();
    if (trayAction.enabled() && trayNotificationService != null) {
      trayNotificationService.notifyCustom(
          sid,
          target,
          title,
          body,
          trayAction.showToast(),
          trayAction.showStatusBar(),
          IrcEventNotificationRuleAdapters.toFocusScope(
              trayAction.focusScope(), matched.focusScope()),
          trayAction.playSound(),
          trayAction.soundId(),
          trayAction.soundUseCustom(),
          trayAction.soundCustomPath());
    }

    IrcEventNotificationScriptAction scriptAction = actionPlan.scriptAction();
    if (scriptAction.enabled()) {
      dispatchScript(
          scriptAction,
          eventType,
          sid,
          target,
          source,
          sourceIsSelf,
          title,
          body,
          ctcpCommand,
          ctcpValue);
    }

    if (actionPlan.sendPush() && pushyNotificationService != null) {
      try {
        pushyNotificationService.notifyEvent(
            eventType, sid, target, source, sourceIsSelf, title, body);
      } catch (Exception ignored) {
      }
    }
  }

  @Override
  public boolean hasEnabledRuleFor(IrcEventNotificationRule.EventType eventType) {
    List<IrcEventNotificationRule> rules = rulesBus != null ? rulesBus.get() : List.of();
    IrcEventNotificationDispatchPreflightPlan preflight =
        IrcEventNotificationDispatchPreflightPlanner.plan(
            eventType != null ? eventType.name() : null, rules != null ? rules.size() : 0);
    if (!preflight.shouldEvaluate()) return false;
    return IrcEventNotificationMatchPolicy.hasEnabledRuleFor(
        IrcEventNotificationRuleAdapters.toMatchRules(rules), preflight.eventTypeName());
  }

  private void dispatchScript(
      IrcEventNotificationScriptAction scriptAction,
      IrcEventNotificationRule.EventType eventType,
      String serverId,
      String channel,
      String sourceNick,
      Boolean sourceIsSelf,
      String title,
      String body,
      String ctcpCommand,
      String ctcpValue) {
    if (scriptAction == null || !scriptAction.enabled()) return;

    scriptExecutor.execute(
        () ->
            runScript(
                scriptAction.scriptPath(),
                scriptAction.scriptArgs(),
                scriptAction.workingDirectory(),
                eventType,
                serverId,
                channel,
                sourceNick,
                sourceIsSelf,
                title,
                body,
                ctcpCommand,
                ctcpValue));
  }

  private void runScript(
      String scriptPath,
      String scriptArgs,
      String scriptWorkingDirectory,
      IrcEventNotificationRule.EventType eventType,
      String serverId,
      String channel,
      String sourceNick,
      Boolean sourceIsSelf,
      String title,
      String body,
      String ctcpCommand,
      String ctcpValue) {
    try {
      IrcEventNotificationScriptPlan plan =
          IrcEventNotificationScriptPlanner.plan(
              scriptPath,
              scriptArgs,
              scriptWorkingDirectory,
              eventType != null ? eventType.name() : "",
              serverId,
              channel,
              sourceNick,
              sourceIsSelf,
              title,
              body,
              ctcpCommand,
              ctcpValue,
              System.currentTimeMillis());
      if (plan.command().isEmpty()) return;

      ProcessBuilder pb = new ProcessBuilder(plan.command());
      pb.redirectInput(ProcessBuilder.Redirect.PIPE);
      pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
      pb.redirectError(ProcessBuilder.Redirect.DISCARD);

      if (plan.workingDirectory() != null && !plan.workingDirectory().isBlank()) {
        File cwd = new File(plan.workingDirectory());
        if (!cwd.isDirectory()) {
          log.warn(
              "[ircafe] Event notification script working directory does not exist: {}",
              plan.workingDirectory());
          return;
        }
        pb.directory(cwd);
      }

      java.util.Map<String, String> env = pb.environment();
      env.putAll(plan.environment());

      Process p = pb.start();
      boolean exited = p.waitFor(SCRIPT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
      if (!exited) {
        p.destroyForcibly();
        log.warn(
            "[ircafe] Event notification script timed out after {}s: {}",
            SCRIPT_TIMEOUT_SECONDS,
            scriptPath);
      }
    } catch (Exception ex) {
      log.warn("[ircafe] Could not run event notification script: {}", scriptPath, ex);
    }
  }
}

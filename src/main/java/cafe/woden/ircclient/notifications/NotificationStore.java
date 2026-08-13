package cafe.woden.ircclient.notifications;

import cafe.woden.ircclient.app.api.UiSettingsPort;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.notifications.api.HighlightEvent;
import cafe.woden.ircclient.notifications.api.IrcEventRuleEvent;
import cafe.woden.ircclient.notifications.api.NotificationChange;
import cafe.woden.ircclient.notifications.api.NotificationEvent;
import cafe.woden.ircclient.notifications.api.NotificationStorePort;
import cafe.woden.ircclient.notifications.api.RuleMatchEvent;
import cafe.woden.ircclient.notify.api.store.NotificationRuleCooldownPolicy;
import cafe.woden.ircclient.notify.api.store.NotificationRuleMatchCooldown;
import cafe.woden.ircclient.notify.api.store.NotificationStoreEventBucketPolicy;
import cafe.woden.ircclient.notify.api.store.NotificationStoreEventPolicy;
import cafe.woden.ircclient.notify.api.store.NotificationStoreEventValues;
import cafe.woden.ircclient.notify.api.store.NotificationStoreOperationPlan;
import cafe.woden.ircclient.notify.api.store.NotificationStoreOperationPlanner;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.PublishProcessor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** In-memory store of per-server "highlight" notifications. */
@Component
@ApplicationLayer
public class NotificationStore implements NotificationStorePort {

  /** Hard cap to prevent unbounded memory growth. */
  public static final int DEFAULT_MAX_EVENTS_PER_SERVER = 2000;

  /** Default cooldown to avoid spamming rule-match notifications. */
  public static final int DEFAULT_RULE_MATCH_COOLDOWN_SECONDS =
      NotificationRuleCooldownPolicy.DEFAULT_COOLDOWN_SECONDS;

  private final int maxEventsPerServer;
  private final UiSettingsPort uiSettingsPort;
  private final NotificationRuleMatchCooldown ruleMatchCooldown =
      new NotificationRuleMatchCooldown();

  private final ConcurrentHashMap<String, List<HighlightEvent>> eventsByServer =
      new ConcurrentHashMap<>();

  private final ConcurrentHashMap<String, List<RuleMatchEvent>> ruleEventsByServer =
      new ConcurrentHashMap<>();

  private final ConcurrentHashMap<String, List<IrcEventRuleEvent>> ircEventRuleEventsByServer =
      new ConcurrentHashMap<>();

  private final FlowableProcessor<NotificationChange> changes =
      PublishProcessor.<NotificationChange>create().toSerialized();

  public NotificationStore() {
    this(null, DEFAULT_MAX_EVENTS_PER_SERVER);
  }

  public NotificationStore(int maxEventsPerServer) {
    this(null, maxEventsPerServer);
  }

  @Autowired
  public NotificationStore(UiSettingsPort uiSettingsPort) {
    this(uiSettingsPort, DEFAULT_MAX_EVENTS_PER_SERVER);
  }

  public NotificationStore(UiSettingsPort uiSettingsPort, int maxEventsPerServer) {
    this.uiSettingsPort = uiSettingsPort;
    this.maxEventsPerServer =
        NotificationStoreEventBucketPolicy.normalizeMaxEventsPerServer(maxEventsPerServer);
  }

  /** Emits a signal whenever notifications change for a server. */
  @Override
  public Flowable<NotificationChange> changes() {
    return changes.onBackpressureBuffer();
  }

  /** Record a new highlight event. */
  @Override
  public void recordHighlight(TargetRef channelTarget, String fromNick) {
    recordHighlight(channelTarget, fromNick, "", "");
  }

  /** Record a new highlight event with optional message snippet context. */
  @Override
  public void recordHighlight(TargetRef channelTarget, String fromNick, String snippet) {
    recordHighlight(channelTarget, fromNick, snippet, "");
  }

  /** Record a new highlight event with optional snippet context and backing message id. */
  @Override
  public void recordHighlight(
      TargetRef channelTarget, String fromNick, String snippet, String messageId) {
    if (channelTarget == null) return;
    if (channelTarget.isUiOnly()) return;
    if (!channelTarget.isChannel()) return;

    NotificationStoreEventValues event =
        NotificationStoreEventPolicy.highlight(
            channelTarget.serverId(), channelTarget.target(), fromNick, snippet, messageId);
    if (!event.valid()) return;
    Instant now = Instant.now();

    List<HighlightEvent> list =
        eventsByServer.computeIfAbsent(
            event.serverId(), k -> Collections.synchronizedList(new ArrayList<>()));

    synchronized (list) {
      NotificationStoreEventBucketPolicy.appendCapped(
          list,
          new HighlightEvent(
              event.serverId(),
              event.channel(),
              event.fromNick(),
              event.snippet(),
              now,
              event.messageId()),
          maxEventsPerServer);
    }

    changes.onNext(new NotificationChange(event.serverId()));
  }

  /** Record a new rule match event. */
  @Override
  public void recordRuleMatch(
      TargetRef channelTarget, String fromNick, String ruleLabel, String snippet) {
    recordRuleMatch(channelTarget, fromNick, ruleLabel, snippet, "");
  }

  /** Record a new rule match event with an optional backing message id. */
  @Override
  public void recordRuleMatch(
      TargetRef channelTarget,
      String fromNick,
      String ruleLabel,
      String snippet,
      String messageId) {
    if (channelTarget == null) return;
    if (channelTarget.isUiOnly()) return;
    if (!channelTarget.isChannel()) return;

    NotificationStoreEventValues event =
        NotificationStoreEventPolicy.ruleMatch(
            channelTarget.serverId(),
            channelTarget.target(),
            fromNick,
            ruleLabel,
            snippet,
            messageId);
    if (!event.valid()) return;
    Instant now = Instant.now();

    if (!ruleMatchCooldown.allow(
        event.serverId(), event.channel(), event.label(), currentRuleMatchCooldownSeconds(), now)) {
      return;
    }

    List<RuleMatchEvent> list =
        ruleEventsByServer.computeIfAbsent(
            event.serverId(), k -> Collections.synchronizedList(new ArrayList<>()));

    synchronized (list) {
      NotificationStoreEventBucketPolicy.appendCapped(
          list,
          new RuleMatchEvent(
              event.serverId(),
              event.channel(),
              event.fromNick(),
              event.label(),
              event.snippet(),
              now,
              event.messageId()),
          maxEventsPerServer);
    }

    changes.onNext(new NotificationChange(event.serverId()));
  }

  /** Record a configured IRC event notification for the Notifications node. */
  @Override
  public void recordIrcEvent(
      String serverId, String target, String fromNick, String title, String body) {
    recordIrcEvent(serverId, target, fromNick, title, body, "");
  }

  /** Record a configured IRC event notification for the Notifications node. */
  @Override
  public void recordIrcEvent(
      String serverId,
      String target,
      String fromNick,
      String title,
      String body,
      String messageId) {
    NotificationStoreEventValues event =
        NotificationStoreEventPolicy.ircEvent(serverId, target, fromNick, title, body, messageId);
    if (!event.valid()) return;
    Instant now = Instant.now();

    List<IrcEventRuleEvent> list =
        ircEventRuleEventsByServer.computeIfAbsent(
            event.serverId(), k -> Collections.synchronizedList(new ArrayList<>()));

    synchronized (list) {
      NotificationStoreEventBucketPolicy.appendCapped(
          list,
          new IrcEventRuleEvent(
              event.serverId(),
              event.channel(),
              event.fromNick(),
              event.label(),
              event.snippet(),
              now,
              event.messageId()),
          maxEventsPerServer);
    }

    changes.onNext(new NotificationChange(event.serverId()));
  }

  /** Returns a defensive copy of all highlight events for a server, oldest to newest. */
  @Override
  public List<HighlightEvent> listAll(String serverId) {
    NotificationStoreOperationPlan plan = NotificationStoreOperationPlanner.server(serverId);
    if (!plan.valid()) return List.of();
    List<HighlightEvent> list = eventsByServer.get(plan.serverId());
    if (list == null) return List.of();
    synchronized (list) {
      return NotificationStoreEventBucketPolicy.copyAll(list);
    }
  }

  /** Returns a defensive copy of all rule-match events for a server, oldest to newest. */
  @Override
  public List<RuleMatchEvent> listAllRuleMatches(String serverId) {
    NotificationStoreOperationPlan plan = NotificationStoreOperationPlanner.server(serverId);
    if (!plan.valid()) return List.of();
    List<RuleMatchEvent> list = ruleEventsByServer.get(plan.serverId());
    if (list == null) return List.of();
    synchronized (list) {
      return NotificationStoreEventBucketPolicy.copyAll(list);
    }
  }

  /**
   * Returns a defensive copy of all configured IRC event notifications for a server, oldest to
   * newest.
   */
  @Override
  public List<IrcEventRuleEvent> listAllIrcEventRules(String serverId) {
    NotificationStoreOperationPlan plan = NotificationStoreOperationPlanner.server(serverId);
    if (!plan.valid()) return List.of();
    List<IrcEventRuleEvent> list = ircEventRuleEventsByServer.get(plan.serverId());
    if (list == null) return List.of();
    synchronized (list) {
      return NotificationStoreEventBucketPolicy.copyAll(list);
    }
  }

  /** Returns up to {@code max} most recent highlight events for a server (newest last). */
  @Override
  public List<HighlightEvent> listRecent(String serverId, int max) {
    NotificationStoreOperationPlan plan = NotificationStoreOperationPlanner.recent(serverId, max);
    if (!plan.valid()) return List.of();
    List<HighlightEvent> list = eventsByServer.get(plan.serverId());
    if (list == null) return List.of();
    synchronized (list) {
      return NotificationStoreEventBucketPolicy.copyRecent(list, plan.max());
    }
  }

  @Override
  public int count(String serverId) {
    NotificationStoreOperationPlan plan = NotificationStoreOperationPlanner.server(serverId);
    if (!plan.valid()) return 0;
    int total = 0;

    List<HighlightEvent> highlights = eventsByServer.get(plan.serverId());
    if (highlights != null) {
      synchronized (highlights) {
        total += NotificationStoreEventBucketPolicy.count(highlights);
      }
    }

    List<RuleMatchEvent> rules = ruleEventsByServer.get(plan.serverId());
    if (rules != null) {
      synchronized (rules) {
        total += NotificationStoreEventBucketPolicy.count(rules);
      }
    }

    List<IrcEventRuleEvent> ircEvents = ircEventRuleEventsByServer.get(plan.serverId());
    if (ircEvents != null) {
      synchronized (ircEvents) {
        total += NotificationStoreEventBucketPolicy.count(ircEvents);
      }
    }

    return total;
  }

  /** Clears all highlight events for a specific channel on a server. */
  @Override
  public void clearChannel(TargetRef channelTarget) {
    NotificationStoreOperationPlan plan =
        NotificationStoreOperationPlanner.channel(
            channelTarget != null ? channelTarget.serverId() : null,
            channelTarget != null ? channelTarget.target() : null,
            channelTarget != null,
            channelTarget != null && channelTarget.isUiOnly(),
            channelTarget != null && channelTarget.isChannel());
    if (!plan.valid()) return;

    boolean changed = false;

    List<HighlightEvent> list = eventsByServer.get(plan.serverId());
    if (list != null) {
      synchronized (list) {
        changed =
            NotificationStoreEventBucketPolicy.removeMatchingChannel(
                    list, plan.channel(), HighlightEvent::channel)
                > 0;
      }
    }
    List<RuleMatchEvent> rules = ruleEventsByServer.get(plan.serverId());
    if (rules != null) {
      synchronized (rules) {
        changed |=
            NotificationStoreEventBucketPolicy.removeMatchingChannel(
                    rules, plan.channel(), RuleMatchEvent::channel)
                > 0;
      }
    }

    List<IrcEventRuleEvent> ircEvents = ircEventRuleEventsByServer.get(plan.serverId());
    if (ircEvents != null) {
      synchronized (ircEvents) {
        changed |=
            NotificationStoreEventBucketPolicy.removeMatchingChannel(
                    ircEvents, plan.channel(), IrcEventRuleEvent::channel)
                > 0;
      }
    }
    ruleMatchCooldown.clearChannel(plan.serverId(), plan.channel());

    if (changed) {
      changes.onNext(new NotificationChange(plan.serverId()));
    }
  }

  /** Clears all highlight events for a server. */
  @Override
  public void clearServer(String serverId) {
    NotificationStoreOperationPlan plan = NotificationStoreOperationPlanner.server(serverId);
    if (!plan.valid()) return;
    List<HighlightEvent> list = eventsByServer.get(plan.serverId());
    if (list != null) {
      synchronized (list) {
        NotificationStoreEventBucketPolicy.clear(list);
      }
    }

    List<RuleMatchEvent> rules = ruleEventsByServer.get(plan.serverId());
    if (rules != null) {
      synchronized (rules) {
        NotificationStoreEventBucketPolicy.clear(rules);
      }
    }

    List<IrcEventRuleEvent> ircEvents = ircEventRuleEventsByServer.get(plan.serverId());
    if (ircEvents != null) {
      synchronized (ircEvents) {
        NotificationStoreEventBucketPolicy.clear(ircEvents);
      }
    }

    ruleMatchCooldown.clearServer(plan.serverId());
    changes.onNext(new NotificationChange(plan.serverId()));
  }

  @Override
  public int clearSelected(String serverId, List<? extends NotificationEvent> selectedEvents) {
    NotificationStoreOperationPlan plan =
        NotificationStoreOperationPlanner.selected(
            serverId, selectedEvents != null ? selectedEvents.size() : 0);
    if (!plan.valid()) return 0;

    int removed = 0;

    List<HighlightEvent> highlights = eventsByServer.get(plan.serverId());
    if (highlights != null) {
      synchronized (highlights) {
        removed +=
            NotificationStoreEventBucketPolicy.removeSelectedByIdentity(highlights, selectedEvents);
      }
    }

    List<RuleMatchEvent> rules = ruleEventsByServer.get(plan.serverId());
    if (rules != null) {
      synchronized (rules) {
        removed +=
            NotificationStoreEventBucketPolicy.removeSelectedByIdentity(rules, selectedEvents);
      }
    }

    List<IrcEventRuleEvent> ircEvents = ircEventRuleEventsByServer.get(plan.serverId());
    if (ircEvents != null) {
      synchronized (ircEvents) {
        removed +=
            NotificationStoreEventBucketPolicy.removeSelectedByIdentity(ircEvents, selectedEvents);
      }
    }

    clearRuleMatchCooldownsForSelectedRules(plan.serverId(), selectedEvents);
    if (removed > 0) {
      changes.onNext(new NotificationChange(plan.serverId()));
    }
    return removed;
  }

  private int currentRuleMatchCooldownSeconds() {
    try {
      if (uiSettingsPort == null) return DEFAULT_RULE_MATCH_COOLDOWN_SECONDS;
      int v = uiSettingsPort.get().notificationRuleCooldownSeconds();
      return NotificationRuleCooldownPolicy.normalizeCooldownSeconds(v);
    } catch (Exception ignored) {
      return DEFAULT_RULE_MATCH_COOLDOWN_SECONDS;
    }
  }

  private void clearRuleMatchCooldownsForSelectedRules(
      String serverId, List<? extends NotificationEvent> selectedEvents) {
    NotificationStoreOperationPlan plan =
        NotificationStoreOperationPlanner.selected(
            serverId, selectedEvents != null ? selectedEvents.size() : 0);
    if (!plan.valid()) return;

    List<NotificationStoreEventValues> selectedRuleMatches = new ArrayList<>();
    for (NotificationEvent event : selectedEvents) {
      if (!(event instanceof RuleMatchEvent ruleMatch)) continue;
      selectedRuleMatches.add(
          NotificationStoreEventPolicy.ruleMatch(
              ruleMatch.serverId(),
              ruleMatch.channel(),
              ruleMatch.fromNick(),
              ruleMatch.ruleLabel(),
              ruleMatch.snippet(),
              ruleMatch.messageId()));
    }

    ruleMatchCooldown.clearSelectedRuleMatches(plan.serverId(), selectedRuleMatches);
  }
}

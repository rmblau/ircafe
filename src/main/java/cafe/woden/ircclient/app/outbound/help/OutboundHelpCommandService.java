package cafe.woden.ircclient.app.outbound.help;

import cafe.woden.ircclient.app.api.UiPort;
import cafe.woden.ircclient.app.commands.SlashCommandPresentationCatalog;
import cafe.woden.ircclient.app.core.TargetCoordinator;
import cafe.woden.ircclient.app.outbound.help.spi.OutboundHelpContributor;
import cafe.woden.ircclient.app.outbound.help.spi.OutboundHelpSink;
import cafe.woden.ircclient.app.outbound.help.spi.OutboundHelpTargetView;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.model.TargetRef;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Handles outbound /help command flow. */
@Component
@ApplicationLayer
public final class OutboundHelpCommandService {

  private final UiPort ui;
  private final TargetCoordinator targetCoordinator;
  private final SlashCommandPresentationCatalog slashCommandPresentationCatalog;
  private final List<OutboundHelpContributor> contributors;
  private final Map<String, List<HelpTopicHandler>> helpTopicHandlers;

  @Autowired
  public OutboundHelpCommandService(
      UiPort ui,
      TargetCoordinator targetCoordinator,
      List<OutboundHelpContributor> contributors,
      SlashCommandPresentationCatalog slashCommandPresentationCatalog,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(
        ui,
        targetCoordinator,
        OutboundHelpPluginProviders.outboundHelpContributors(
            contributors,
            OutboundHelpPluginProviders.resolveInstalledPlugins(installedPluginsProvider)),
        slashCommandPresentationCatalog);
  }

  public OutboundHelpCommandService(
      UiPort ui,
      TargetCoordinator targetCoordinator,
      List<OutboundHelpContributor> contributors,
      SlashCommandPresentationCatalog slashCommandPresentationCatalog) {
    this.ui = Objects.requireNonNull(ui, "ui");
    this.targetCoordinator = Objects.requireNonNull(targetCoordinator, "targetCoordinator");
    this.contributors = OutboundHelpPluginProviders.outboundHelpContributors(contributors, null);
    this.slashCommandPresentationCatalog =
        Objects.requireNonNull(slashCommandPresentationCatalog, "slashCommandPresentationCatalog");
    this.helpTopicHandlers = buildHelpTopicHandlers();
  }

  OutboundHelpCommandService(
      UiPort ui,
      TargetCoordinator targetCoordinator,
      List<OutboundHelpContributor> contributors,
      SlashCommandPresentationCatalog slashCommandPresentationCatalog,
      InstalledPluginsPort installedPlugins) {
    this(
        ui,
        targetCoordinator,
        OutboundHelpPluginProviders.outboundHelpContributors(contributors, installedPlugins),
        slashCommandPresentationCatalog);
  }

  public void handleHelp(String topic) {
    TargetRef at = targetCoordinator.getActiveTarget();
    TargetRef out = (at != null) ? at : targetCoordinator.safeStatusTarget();
    String t = normalizeHelpTopic(topic);
    if (!t.isEmpty()) {
      List<HelpTopicHandler> handlers = helpTopicHandlers.get(t);
      if (handlers != null && !handlers.isEmpty()) {
        handlers.forEach(handler -> handler.handle(out));
        return;
      }
      ui.appendStatus(out, "(help)", "No dedicated help for '" + t + "'. Showing common commands.");
    }

    slashCommandPresentationCatalog.appendGeneralHelp(out, this::appendStaticHelpLine);
    OutboundHelpSink help = helpSink(out);
    for (OutboundHelpContributor contributor : contributors) {
      contributor.appendGeneralHelp(help);
    }
  }

  private Map<String, List<HelpTopicHandler>> buildHelpTopicHandlers() {
    LinkedHashMap<String, List<HelpTopicHandler>> handlers = new LinkedHashMap<>();
    for (OutboundHelpContributor contributor : contributors) {
      registerHelpTopicHandlers(handlers, contributor.topicHelpHandlers());
    }
    registerTargetHelpTopicHandlers(
        handlers, slashCommandPresentationCatalog.topicHelpHandlers(this::appendStaticHelpLine));
    LinkedHashMap<String, List<HelpTopicHandler>> immutable = new LinkedHashMap<>();
    handlers.forEach((topic, topicHandlers) -> immutable.put(topic, List.copyOf(topicHandlers)));
    return Map.copyOf(immutable);
  }

  private void appendStaticHelpLine(TargetRef out, String line) {
    if (out == null || line == null || line.isBlank()) return;
    ui.appendStatus(out, "(help)", line);
  }

  private OutboundHelpSink helpSink(TargetRef out) {
    return new ServiceOutboundHelpSink(out);
  }

  private void registerHelpTopicHandlers(
      Map<String, List<HelpTopicHandler>> handlers,
      Map<String, Consumer<OutboundHelpSink>> topicHandlers) {
    if (handlers == null || topicHandlers == null || topicHandlers.isEmpty()) return;
    for (Map.Entry<String, Consumer<OutboundHelpSink>> entry : topicHandlers.entrySet()) {
      String topic = normalizeHelpTopic(entry.getKey());
      Consumer<OutboundHelpSink> consumer = entry.getValue();
      if (!topic.isEmpty() && consumer != null) {
        handlers
            .computeIfAbsent(topic, ignored -> new ArrayList<>())
            .add(out -> consumer.accept(helpSink(out)));
      }
    }
  }

  private static void registerTargetHelpTopicHandlers(
      Map<String, List<HelpTopicHandler>> handlers,
      Map<String, Consumer<TargetRef>> topicHandlers) {
    if (handlers == null || topicHandlers == null || topicHandlers.isEmpty()) return;
    for (Map.Entry<String, Consumer<TargetRef>> entry : topicHandlers.entrySet()) {
      String topic = normalizeHelpTopic(entry.getKey());
      Consumer<TargetRef> consumer = entry.getValue();
      if (!topic.isEmpty() && consumer != null) {
        List<HelpTopicHandler> topicHandlersForKey =
            handlers.computeIfAbsent(topic, ignored -> new ArrayList<>());
        topicHandlersForKey.add(consumer::accept);
      }
    }
  }

  private final class ServiceOutboundHelpSink implements OutboundHelpSink {
    private final TargetRef out;

    private ServiceOutboundHelpSink(TargetRef out) {
      this.out = out;
    }

    @Override
    public OutboundHelpTargetView target() {
      if (out == null) {
        return new OutboundHelpTargetView("", "");
      }
      return new OutboundHelpTargetView(out.serverId(), out.target());
    }

    @Override
    public void appendLine(String line) {
      appendStaticHelpLine(out, line);
    }
  }

  @FunctionalInterface
  private interface HelpTopicHandler {
    void handle(TargetRef out);
  }

  private static String normalizeHelpTopic(String raw) {
    String s = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    if (s.startsWith("/")) s = s.substring(1).trim();
    return s;
  }
}

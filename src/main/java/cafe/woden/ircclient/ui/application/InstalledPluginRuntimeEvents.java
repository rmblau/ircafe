package cafe.woden.ircclient.ui.application;

import cafe.woden.ircclient.config.api.InstalledPluginProblem;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.diagnostics.RuntimeDiagnosticEvent;
import cafe.woden.ircclient.plugin.spi.InstalledPluginDescriptor;
import cafe.woden.ircclient.ui.localization.UiMessages;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Builds exportable runtime-event rows for the installed plugin diagnostics panel. */
public final class InstalledPluginRuntimeEvents {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private InstalledPluginRuntimeEvents() {}

  public static List<RuntimeDiagnosticEvent> build(InstalledPluginsPort installedPluginsPort) {
    return build(installedPluginsPort, Clock.systemUTC());
  }

  static List<RuntimeDiagnosticEvent> build(
      InstalledPluginsPort installedPluginsPort, Clock clock) {
    Instant recordedAt = clock == null ? Instant.now() : clock.instant();
    String pluginDirectory = pluginDirectory(installedPluginsPort);
    if (installedPluginsPort == null) {
      return List.of(
          new RuntimeDiagnosticEvent(
              recordedAt,
              "INFO",
              message("chatDock.plugins.category"),
              message("chatDock.plugins.unavailable.summary"),
              pluginDirectory.isBlank()
                  ? ""
                  : message("chatDock.plugins.directory", pluginDirectory)));
    }

    List<InstalledPluginDescriptor> installedPlugins = installedPluginsPort.installedPlugins();
    List<InstalledPluginProblem> pluginProblems = installedPluginsPort.pluginProblems();
    boolean hasInstalledPlugins = installedPlugins != null && !installedPlugins.isEmpty();
    boolean hasPluginProblems = pluginProblems != null && !pluginProblems.isEmpty();
    if (!hasInstalledPlugins && !hasPluginProblems) {
      return List.of(
          new RuntimeDiagnosticEvent(
              recordedAt,
              "INFO",
              message("chatDock.plugins.category"),
              message("chatDock.plugins.none.summary"),
              pluginDirectory.isBlank()
                  ? ""
                  : message("chatDock.plugins.directory", pluginDirectory)));
    }

    ArrayList<RuntimeDiagnosticEvent> rows =
        new ArrayList<>(
            (hasInstalledPlugins ? installedPlugins.size() : 0)
                + (hasPluginProblems ? pluginProblems.size() : 0));
    if (hasInstalledPlugins) {
      for (InstalledPluginDescriptor descriptor : installedPlugins) {
        appendPluginDescriptorRow(rows, recordedAt, pluginDirectory, descriptor);
      }
    }
    if (hasPluginProblems) {
      for (InstalledPluginProblem problem : pluginProblems) {
        appendPluginProblemRow(rows, recordedAt, pluginDirectory, problem);
      }
    }
    return List.copyOf(rows);
  }

  private static void appendPluginDescriptorRow(
      List<RuntimeDiagnosticEvent> rows,
      Instant recordedAt,
      String pluginDirectory,
      InstalledPluginDescriptor descriptor) {
    if (descriptor == null) return;
    String pluginId = Objects.toString(descriptor.pluginId(), "").trim();
    String pluginVersion = Objects.toString(descriptor.pluginVersion(), "").trim();
    String versionLabel =
        pluginVersion.isBlank() ? message("chatDock.plugins.unknown") : pluginVersion;
    String sourceJar = Objects.toString(descriptor.sourceJar(), "").trim();
    StringBuilder details = new StringBuilder();
    details
        .append(
            message(
                "chatDock.plugins.detail.pluginId",
                pluginId.isBlank() ? message("chatDock.plugins.unknown.parenthesized") : pluginId))
        .append('\n')
        .append(message("chatDock.plugins.detail.version", versionLabel))
        .append('\n')
        .append(message("chatDock.plugins.detail.apiVersion", descriptor.pluginApiVersion()));
    if (!sourceJar.isBlank()) {
      details.append('\n').append(message("chatDock.plugins.detail.sourceJar", sourceJar));
    }
    if (!pluginDirectory.isBlank()) {
      details
          .append('\n')
          .append(message("chatDock.plugins.detail.pluginDirectory", pluginDirectory));
    }
    rows.add(
        new RuntimeDiagnosticEvent(
            recordedAt,
            "INFO",
            message("chatDock.plugins.plugin.category"),
            (pluginId.isBlank() ? message("chatDock.plugins.unknown.parenthesized") : pluginId)
                + " "
                + versionLabel,
            details.toString()));
  }

  private static void appendPluginProblemRow(
      List<RuntimeDiagnosticEvent> rows,
      Instant recordedAt,
      String pluginDirectory,
      InstalledPluginProblem problem) {
    if (problem == null) return;
    StringBuilder details = new StringBuilder(Objects.toString(problem.details(), ""));
    if (!pluginDirectory.isBlank()
        && !details.toString().contains(message("chatDock.plugins.directory.prefix"))
        && !details
            .toString()
            .contains(message("chatDock.plugins.detail.pluginDirectory.prefix"))) {
      if (!details.isEmpty()) {
        details.append('\n');
      }
      details.append(message("chatDock.plugins.directory", pluginDirectory));
    }
    rows.add(
        new RuntimeDiagnosticEvent(
            recordedAt,
            problem.level(),
            message("chatDock.plugins.problem.category"),
            problem.summary(),
            details.toString()));
  }

  private static String pluginDirectory(InstalledPluginsPort installedPluginsPort) {
    return installedPluginsPort != null && installedPluginsPort.pluginDirectory() != null
        ? installedPluginsPort.pluginDirectory().toString()
        : "";
  }

  private static String message(String key, Object... args) {
    return MESSAGES.text(key, args);
  }
}

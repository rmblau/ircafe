package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.irc.port.IrcCurrentNickPort;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.model.UserCommandAlias;
import cafe.woden.ircclient.util.AppVersion;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Expands user-defined slash-command aliases before regular command parsing. */
@Component
@ApplicationLayer
@RequiredArgsConstructor
public class UserCommandAliasEngine {

  private static final DateTimeFormatter HEXCHAT_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH)
          .withZone(ZoneId.systemDefault());

  private final UserCommandAliasExpansionService expansionService =
      new UserCommandAliasExpansionService();

  @NonNull private final UserCommandAliasesBus aliasesBus;

  @NonNull
  @Qualifier("ircCurrentNickPort")
  private final IrcCurrentNickPort currentNickPort;

  public ExpansionResult expand(String raw, TargetRef contextTarget) {
    UserCommandAliasExpansionResult result =
        expansionService.expand(
            raw, aliasDefinitions(aliasesBus.get()), buildContext(contextTarget));
    return new ExpansionResult(result.lines(), result.warnings());
  }

  private static List<UserCommandAliasDefinition> aliasDefinitions(List<UserCommandAlias> aliases) {
    if (aliases == null || aliases.isEmpty()) {
      return List.of();
    }
    return aliases.stream()
        .filter(Objects::nonNull)
        .map(
            alias ->
                new UserCommandAliasDefinition(alias.enabled(), alias.name(), alias.template()))
        .toList();
  }

  private UserCommandAliasExpansionContext buildContext(TargetRef targetRef) {
    if (targetRef == null) {
      return new UserCommandAliasExpansionContext(
          "", "", "", "", hexChatTimeText(), hexChatVersionText(), hexChatMachineText());
    }

    String serverId = norm(targetRef.serverId());
    String target = norm(targetRef.target());
    String channel = targetRef.isChannel() ? target : "";

    String nick = "";
    if (!serverId.isBlank()) {
      Optional<String> currentNick = currentNickPort.currentNick(serverId);
      nick = currentNick.map(UserCommandAliasEngine::norm).orElse("");
    }

    return new UserCommandAliasExpansionContext(
        serverId,
        target,
        channel,
        nick,
        hexChatTimeText(),
        hexChatVersionText(),
        hexChatMachineText());
  }

  private static String hexChatTimeText() {
    return HEXCHAT_TIME_FORMATTER.format(Instant.now());
  }

  private static String hexChatVersionText() {
    String version = AppVersion.appNameWithVersion();
    if (version == null || version.isBlank()) return AppVersion.APP_NAME;
    return version;
  }

  private static String hexChatMachineText() {
    String name = norm(System.getProperty("os.name"));
    String version = norm(System.getProperty("os.version"));
    String arch = norm(System.getProperty("os.arch"));

    StringBuilder out = new StringBuilder(48);
    if (!name.isBlank()) out.append(name);
    if (!version.isBlank()) {
      if (out.length() > 0) out.append(' ');
      out.append(version);
    }
    if (!arch.isBlank()) {
      if (out.length() > 0) {
        out.append(" (").append(arch).append(')');
      } else {
        out.append(arch);
      }
    }
    if (out.length() == 0) return "Unknown OS";
    return out.toString();
  }

  static List<String> splitExpandedCommands(String expanded) {
    return UserCommandAliasExpansionService.splitExpandedCommands(expanded);
  }

  private static String norm(String value) {
    return Objects.toString(value, "").trim();
  }

  public record ExpansionResult(List<String> lines, List<String> warnings) {}
}

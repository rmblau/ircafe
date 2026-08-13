package cafe.woden.ircclient.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jmolecules.ddd.annotation.ValueObject;

/** Rule for event-driven desktop notifications (kick/ban/invite/mode changes, etc). */
@ValueObject
public record IrcEventNotificationRule(
    boolean enabled,
    EventType eventType,
    SourceMode sourceMode,
    String sourcePattern,
    ChannelScope channelScope,
    String channelPatterns,
    boolean toastEnabled,
    FocusScope focusScope,
    boolean statusBarEnabled,
    boolean notificationsNodeEnabled,
    boolean soundEnabled,
    String soundId,
    boolean soundUseCustom,
    String soundCustomPath,
    boolean scriptEnabled,
    String scriptPath,
    String scriptArgs,
    String scriptWorkingDirectory,
    CtcpMatchMode ctcpCommandMode,
    String ctcpCommandPattern,
    CtcpMatchMode ctcpValueMode,
    String ctcpValuePattern) {
  private static final List<EventType> STATUS_BAR_ANY_COMPANION_EVENTS =
      List.of(EventType.KICKED, EventType.BANNED, EventType.KLINED);

  public enum EventType {
    KICKED("Kicked"),
    YOU_KICKED("You Were Kicked"),
    BANNED("Banned"),
    YOU_BANNED("You Were Banned"),
    VOICED("Voiced"),
    DEVOICED("De-Voiced"),
    OPPED("Opped"),
    DEOPPED("De-Opped"),
    HALF_OPPED("Half-Opped"),
    DEHALF_OPPED("De-Half-Opped"),
    YOU_OPPED("You Were Opped"),
    YOU_DEOPPED("You Were De-Opped"),
    YOU_VOICED("You Were Voiced"),
    YOU_DEVOICED("You Were De-Voiced"),
    YOU_HALF_OPPED("You Were Half-Opped"),
    YOU_DEHALF_OPPED("You Were De-Half-Opped"),
    PRIVATE_MESSAGE_RECEIVED("Private Message Received"),
    CTCP_RECEIVED("CTCP Request Received"),
    NOTICE_RECEIVED("Notice Received"),
    WALLOPS_RECEIVED("WALLOPS Received"),
    INVITE_RECEIVED("Invite Received"),
    USER_JOINED("User Joined Channel"),
    USER_PARTED("User Parted Channel"),
    USER_QUIT("User Quit"),
    USER_NICK_CHANGED("User Nick Changed"),
    TOPIC_CHANGED("Topic Changed"),
    NETSPLIT_DETECTED("Netsplit Detected"),
    KLINED("User K-Lined / Restricted"),
    YOU_KLINED("You Were K-Lined / Restricted");

    private final String label;

    EventType(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  public enum SourceMode {
    ANY("Any"),
    SELF("Self"),
    OTHERS("Someone else"),
    NICK_LIST("Specific nicks"),
    GLOB("Nick glob"),
    REGEX("Nick regex");

    private final String label;

    SourceMode(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  public enum ChannelScope {
    ALL("All channels"),
    ACTIVE_TARGET_ONLY("Active channel only"),
    ONLY("Only matching"),
    ALL_EXCEPT("All except matching");

    private final String label;

    ChannelScope(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  public enum FocusScope {
    ANY("Any"),
    FOREGROUND_ONLY("Foreground Only"),
    BACKGROUND_ONLY("Background Only");

    private final String label;

    FocusScope(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  public enum CtcpMatchMode {
    ANY("Any"),
    LIKE("Like"),
    GLOB("Glob"),
    REGEX("Regex");

    private final String label;

    CtcpMatchMode(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  public IrcEventNotificationRule {
    if (eventType == null) eventType = EventType.INVITE_RECEIVED;

    if (sourceMode == null) sourceMode = SourceMode.ANY;
    sourcePattern = normalizeToNull(sourcePattern);
    if (sourceMode == SourceMode.ANY
        || sourceMode == SourceMode.SELF
        || sourceMode == SourceMode.OTHERS) {
      sourcePattern = null;
    }

    if (channelScope == null) channelScope = ChannelScope.ALL;
    channelPatterns = normalizeToNull(channelPatterns);
    if (channelScope == ChannelScope.ALL || channelScope == ChannelScope.ACTIVE_TARGET_ONLY) {
      channelPatterns = null;
    }

    if (focusScope == null) focusScope = FocusScope.BACKGROUND_ONLY;

    if (soundId == null || soundId.isBlank())
      soundId = defaultBuiltInSoundForEvent(eventType).name();
    if (soundCustomPath != null && soundCustomPath.isBlank()) soundCustomPath = null;
    if (soundUseCustom && soundCustomPath == null) soundUseCustom = false;

    scriptPath = normalizeToNull(scriptPath);
    if (scriptEnabled && scriptPath == null) scriptEnabled = false;
    scriptArgs = normalizeToNull(scriptArgs);
    scriptWorkingDirectory = normalizeToNull(scriptWorkingDirectory);

    if (ctcpCommandMode == null) ctcpCommandMode = CtcpMatchMode.ANY;
    if (ctcpValueMode == null) ctcpValueMode = CtcpMatchMode.ANY;
    ctcpCommandPattern = normalizeToNull(ctcpCommandPattern);
    ctcpValuePattern = normalizeToNull(ctcpValuePattern);
    if (ctcpCommandMode == CtcpMatchMode.ANY) ctcpCommandPattern = null;
    if (ctcpValueMode == CtcpMatchMode.ANY) ctcpValuePattern = null;
    if (eventType != EventType.CTCP_RECEIVED) {
      ctcpCommandMode = CtcpMatchMode.ANY;
      ctcpCommandPattern = null;
      ctcpValueMode = CtcpMatchMode.ANY;
      ctcpValuePattern = null;
    }
  }

  public IrcEventNotificationRule(
      boolean enabled,
      EventType eventType,
      SourceMode sourceMode,
      String sourcePattern,
      ChannelScope channelScope,
      String channelPatterns,
      boolean toastEnabled,
      FocusScope focusScope,
      boolean statusBarEnabled,
      boolean notificationsNodeEnabled,
      boolean soundEnabled,
      String soundId,
      boolean soundUseCustom,
      String soundCustomPath,
      boolean scriptEnabled,
      String scriptPath,
      String scriptArgs,
      String scriptWorkingDirectory) {
    this(
        enabled,
        eventType,
        sourceMode,
        sourcePattern,
        channelScope,
        channelPatterns,
        toastEnabled,
        focusScope,
        statusBarEnabled,
        notificationsNodeEnabled,
        soundEnabled,
        soundId,
        soundUseCustom,
        soundCustomPath,
        scriptEnabled,
        scriptPath,
        scriptArgs,
        scriptWorkingDirectory,
        CtcpMatchMode.ANY,
        null,
        CtcpMatchMode.ANY,
        null);
  }

  public boolean matches(EventType type, String sourceNick, Boolean sourceIsSelf, String channel) {
    return matches(type, sourceNick, sourceIsSelf, channel, false, null, null, null);
  }

  public boolean matches(
      EventType type,
      String sourceNick,
      Boolean sourceIsSelf,
      String channel,
      boolean activeTargetOnSameServer,
      String activeTarget) {
    return matches(
        type,
        sourceNick,
        sourceIsSelf,
        channel,
        activeTargetOnSameServer,
        activeTarget,
        null,
        null);
  }

  public boolean matches(
      EventType type,
      String sourceNick,
      Boolean sourceIsSelf,
      String channel,
      boolean activeTargetOnSameServer,
      String activeTarget,
      String ctcpCommand,
      String ctcpValue) {
    if (!enabled || type == null || eventType != type) return false;
    if (!matchesSource(sourceNick, sourceIsSelf)) return false;
    if (!matchesCtcp(ctcpCommand, ctcpValue)) return false;
    return matchesChannel(channel, activeTargetOnSameServer, activeTarget);
  }

  /** Backward-compatible overload used by older callers. */
  public boolean matches(EventType type, Boolean sourceIsSelf, String channel) {
    return matches(type, null, sourceIsSelf, channel);
  }

  public boolean matchesCtcp(String command, String value) {
    if (eventType != EventType.CTCP_RECEIVED) return true;
    if (!matchesCtcpMode(ctcpCommandMode, ctcpCommandPattern, command)) {
      return false;
    }
    return matchesCtcpMode(ctcpValueMode, ctcpValuePattern, value);
  }

  public boolean matchesSource(String sourceNick, Boolean sourceIsSelf) {
    return switch (sourceMode) {
      case ANY -> true;
      case SELF -> Boolean.TRUE.equals(sourceIsSelf);
      case OTHERS -> Boolean.FALSE.equals(sourceIsSelf);
      case NICK_LIST -> matchesNickList(sourcePattern, sourceNick);
      case GLOB -> {
        String nick = normalizeToNull(sourceNick);
        yield nick != null && matchesAnyMask(parseMaskList(sourcePattern), nick);
      }
      case REGEX -> regexMatch(sourcePattern, sourceNick);
    };
  }

  public boolean matchesChannel(String channel) {
    return matchesChannel(channel, false, null);
  }

  public boolean matchesChannel(
      String channel, boolean activeTargetOnSameServer, String activeTarget) {
    String ch = normalizeToNull(channel);
    String active = normalizeToNull(activeTarget);
    List<String> masks = parseMaskList(channelPatterns);

    return switch (channelScope) {
      case ALL -> true;
      case ACTIVE_TARGET_ONLY ->
          activeTargetOnSameServer && ch != null && active != null && ch.equalsIgnoreCase(active);
      case ONLY -> ch != null && !masks.isEmpty() && matchesAnyMask(masks, ch);
      case ALL_EXCEPT -> ch == null || masks.isEmpty() || !matchesAnyMask(masks, ch);
    };
  }

  public static List<IrcEventNotificationRule> defaults() {
    List<IrcEventNotificationRule> out = new ArrayList<>();
    for (EventType eventType : EventType.values()) {
      out.add(
          defaultRule(
              defaultEnabledForEvent(eventType), eventType, defaultSourceModeForEvent(eventType)));
    }

    for (EventType eventType : STATUS_BAR_ANY_COMPANION_EVENTS) {
      out.add(statusBarAnyCompanionRule(eventType));
    }
    return List.copyOf(out);
  }

  public static List<IrcEventNotificationRule> preset(String preset) {
    return switch (normalizeName(preset)) {
      case "ESSENTIAL" ->
          List.of(
              eventDefaultRule(EventType.PRIVATE_MESSAGE_RECEIVED, SourceMode.OTHERS, false),
              eventDefaultRule(EventType.INVITE_RECEIVED, SourceMode.OTHERS, false),
              eventDefaultRule(EventType.YOU_KICKED, SourceMode.ANY, false),
              eventDefaultRule(EventType.YOU_BANNED, SourceMode.ANY, false),
              eventDefaultRule(EventType.YOU_KLINED, SourceMode.ANY, false));
      case "MODERATION" ->
          List.of(
              eventDefaultRule(EventType.KICKED, SourceMode.OTHERS, false),
              eventDefaultRule(EventType.BANNED, SourceMode.OTHERS, false),
              eventDefaultRule(EventType.OPPED, SourceMode.OTHERS, false),
              eventDefaultRule(EventType.DEOPPED, SourceMode.OTHERS, false),
              eventDefaultRule(EventType.VOICED, SourceMode.OTHERS, false),
              eventDefaultRule(EventType.DEVOICED, SourceMode.OTHERS, false),
              eventDefaultRule(EventType.HALF_OPPED, SourceMode.OTHERS, false),
              eventDefaultRule(EventType.DEHALF_OPPED, SourceMode.OTHERS, false),
              eventDefaultRule(EventType.INVITE_RECEIVED, SourceMode.ANY, false));
      case "ALL_EVENTS" -> allEventsPreset();
      default -> List.of();
    };
  }

  public static BuiltInSound defaultBuiltInSoundForEvent(EventType eventType) {
    return BuiltInSound.fromId(defaultBuiltInSoundIdForEvent(eventType));
  }

  private static IrcEventNotificationRule defaultRule(
      boolean enabled, EventType eventType, SourceMode sourceMode) {
    return new IrcEventNotificationRule(
        enabled,
        eventType,
        sourceMode,
        null,
        ChannelScope.ALL,
        null,
        true,
        FocusScope.BACKGROUND_ONLY,
        true,
        true,
        false,
        defaultBuiltInSoundIdForEvent(eventType),
        false,
        null,
        false,
        null,
        null,
        null,
        CtcpMatchMode.ANY,
        null,
        CtcpMatchMode.ANY,
        null);
  }

  private static IrcEventNotificationRule statusBarAnyCompanionRule(EventType eventType) {
    return new IrcEventNotificationRule(
        true,
        eventType,
        defaultSourceModeForEvent(eventType),
        null,
        ChannelScope.ALL,
        null,
        false,
        FocusScope.ANY,
        true,
        true,
        false,
        defaultBuiltInSoundIdForEvent(eventType),
        false,
        null,
        false,
        null,
        null,
        null,
        CtcpMatchMode.ANY,
        null,
        CtcpMatchMode.ANY,
        null);
  }

  private static IrcEventNotificationRule eventDefaultRule(
      EventType eventType, SourceMode sourceMode, boolean soundEnabled) {
    return new IrcEventNotificationRule(
        true,
        eventType,
        sourceMode,
        null,
        ChannelScope.ALL,
        null,
        true,
        FocusScope.BACKGROUND_ONLY,
        true,
        true,
        soundEnabled,
        defaultBuiltInSoundIdForEvent(eventType),
        false,
        null,
        false,
        null,
        null,
        null,
        CtcpMatchMode.ANY,
        null,
        CtcpMatchMode.ANY,
        null);
  }

  private static List<IrcEventNotificationRule> allEventsPreset() {
    List<IrcEventNotificationRule> out = new ArrayList<>();
    for (EventType eventType : EventType.values()) {
      out.add(eventDefaultRule(eventType, SourceMode.ANY, false));
    }
    return List.copyOf(out);
  }

  private static String defaultBuiltInSoundIdForEvent(EventType eventType) {
    if (eventType == null) return "NOTIF_1";
    return switch (eventTypeOrDefault(enumName(eventType))) {
      case KICKED -> "SOMEBODY_GOT_KICKED";
      case YOU_KICKED -> "YOU_KICKED_1";
      case BANNED -> "SOMEBODY_BANNED";
      case YOU_BANNED -> "YOU_BANNED_1";
      case VOICED -> "SOMEBODY_GAVE_SOMEBODY_VOICE";
      case DEVOICED -> "SOMEONE_ELSE_TOOK_VOICE";
      case OPPED -> "SOMEBODY_OPPED";
      case DEOPPED -> "SOMEBODY_DEOPPED";
      case HALF_OPPED -> "SOMEBODY_HALF_OPPED";
      case DEHALF_OPPED -> "SOMEBODY_LOST_HALFOPS";
      case YOU_HALF_OPPED -> "YOU_HALF_OPS";
      case YOU_DEHALF_OPPED -> "YOU_HALF_OPS_REMOVED";
      case YOU_OPPED -> "YOU_OPS_1";
      case YOU_DEOPPED -> "YOU_DEOPPED";
      case YOU_VOICED -> "YOU_VOICE_1";
      case YOU_DEVOICED -> "YOU_LOST_VOICE_1";
      case PRIVATE_MESSAGE_RECEIVED -> "PM_RECEIVED_1";
      case CTCP_RECEIVED -> "SOMEBODY_SENT_CTCP_1";
      case NOTICE_RECEIVED -> "NOTICE_RECEIVED_1";
      case WALLOPS_RECEIVED -> "WALLOPS_1";
      case INVITE_RECEIVED -> "CHANNEL_INVITE_1";
      case USER_JOINED -> "USER_JOINED";
      case USER_PARTED -> "USER_LEFT_CHANNEL";
      case USER_QUIT -> "USER_DISCONNECTED_SERVER";
      case USER_NICK_CHANGED -> "SOMEBODY_NICK_CHANGED";
      case TOPIC_CHANGED -> "TOPIC_CHANGED_1";
      case NETSPLIT_DETECTED -> "NETSPLIT_1";
      case KLINED -> "USER_KLINED_1";
      case YOU_KLINED -> "YOU_KLINED";
    };
  }

  private static boolean defaultEnabledForEvent(EventType eventType) {
    return switch (eventTypeOrDefault(enumName(eventType))) {
      case PRIVATE_MESSAGE_RECEIVED, INVITE_RECEIVED, YOU_KICKED, YOU_BANNED, YOU_KLINED -> true;
      default -> false;
    };
  }

  private static SourceMode defaultSourceModeForEvent(EventType eventType) {
    return switch (eventTypeOrDefault(enumName(eventType))) {
      case KICKED,
          BANNED,
          VOICED,
          DEVOICED,
          OPPED,
          DEOPPED,
          HALF_OPPED,
          DEHALF_OPPED,
          PRIVATE_MESSAGE_RECEIVED,
          CTCP_RECEIVED,
          NOTICE_RECEIVED,
          WALLOPS_RECEIVED,
          INVITE_RECEIVED,
          USER_JOINED,
          USER_PARTED,
          USER_QUIT,
          USER_NICK_CHANGED,
          NETSPLIT_DETECTED,
          KLINED ->
          SourceMode.OTHERS;
      default -> SourceMode.ANY;
    };
  }

  private static boolean matchesNickList(String rawList, String nick) {
    String normalizedNick = normalizeToNull(nick);
    if (normalizedNick == null) return false;

    for (String token : parseTokenList(rawList)) {
      if (token.equalsIgnoreCase(normalizedNick)) return true;
    }
    return false;
  }

  private static boolean matchesCtcpMode(CtcpMatchMode mode, String pattern, String value) {
    CtcpMatchMode effective = mode != null ? mode : CtcpMatchMode.ANY;
    if (effective == CtcpMatchMode.ANY) return true;

    String p = normalizeToNull(pattern);
    String v = normalizeToNull(value);
    if (p == null || v == null) return false;

    return switch (effective) {
      case LIKE -> p.equalsIgnoreCase(v);
      case GLOB -> globMatch(p.toLowerCase(Locale.ROOT), v.toLowerCase(Locale.ROOT));
      case REGEX -> regexMatch(p, v);
      case ANY -> true;
    };
  }

  private static boolean regexMatch(String regex, String value) {
    String r = normalizeToNull(regex);
    String v = normalizeToNull(value);
    if (r == null || v == null) return false;

    try {
      return Pattern.compile(r, Pattern.CASE_INSENSITIVE).matcher(v).matches();
    } catch (PatternSyntaxException ignored) {
      return false;
    }
  }

  private static List<String> parseTokenList(String raw) {
    String s = normalizeToNull(raw);
    if (s == null) return List.of();

    String[] tokens = s.split("[,\\s]+");
    List<String> out = new ArrayList<>();
    for (String token : tokens) {
      String t = normalizeToNull(token);
      if (t == null) continue;
      out.add(t);
    }
    return out;
  }

  private static List<String> parseMaskList(String raw) {
    List<String> tokens = parseTokenList(raw);
    if (tokens.isEmpty()) return List.of();

    List<String> out = new ArrayList<>(tokens.size());
    for (String token : tokens) {
      out.add(token.toLowerCase(Locale.ROOT));
    }
    return out;
  }

  private static boolean matchesAnyMask(List<String> masks, String value) {
    if (masks == null || masks.isEmpty()) return false;
    String v = normalizeToNull(value);
    if (v == null) return false;
    String normalized = v.toLowerCase(Locale.ROOT);

    for (String mask : masks) {
      if (globMatch(mask, normalized)) return true;
    }
    return false;
  }

  private static boolean globMatch(String mask, String value) {
    if (mask == null || mask.isEmpty()) return false;
    StringBuilder regex = new StringBuilder("^");
    for (int i = 0; i < mask.length(); i++) {
      char c = mask.charAt(i);
      if (c == '*') {
        regex.append(".*");
        continue;
      }
      if (c == '?') {
        regex.append('.');
        continue;
      }
      if ("\\.[]{}()+-^$|".indexOf(c) >= 0) {
        regex.append('\\');
      }
      regex.append(c);
    }
    regex.append('$');
    return Pattern.compile(regex.toString()).matcher(value).matches();
  }

  private static EventType eventTypeOrDefault(String value) {
    if (value == null || value.isBlank()) return EventType.INVITE_RECEIVED;
    try {
      return EventType.valueOf(value.trim());
    } catch (Exception ignored) {
      return EventType.INVITE_RECEIVED;
    }
  }

  private static SourceMode sourceModeOrDefault(String value) {
    if (value == null || value.isBlank()) return SourceMode.ANY;
    try {
      return SourceMode.valueOf(value.trim());
    } catch (Exception ignored) {
      return SourceMode.ANY;
    }
  }

  private static ChannelScope channelScopeOrDefault(String value) {
    if (value == null || value.isBlank()) return ChannelScope.ALL;
    try {
      return ChannelScope.valueOf(value.trim());
    } catch (Exception ignored) {
      return ChannelScope.ALL;
    }
  }

  private static FocusScope focusScopeOrDefault(String value) {
    if (value == null || value.isBlank()) return FocusScope.BACKGROUND_ONLY;
    try {
      return FocusScope.valueOf(value.trim());
    } catch (Exception ignored) {
      return FocusScope.BACKGROUND_ONLY;
    }
  }

  private static CtcpMatchMode ctcpMatchModeOrDefault(String value) {
    if (value == null || value.isBlank()) return CtcpMatchMode.ANY;
    try {
      return CtcpMatchMode.valueOf(value.trim());
    } catch (Exception ignored) {
      return CtcpMatchMode.ANY;
    }
  }

  private static String enumName(Enum<?> value) {
    return value == null ? null : value.name();
  }

  private static String normalizeName(String raw) {
    return Objects.toString(raw, "").trim().toUpperCase(Locale.ROOT);
  }

  private static String normalizeToNull(String raw) {
    String s = Objects.toString(raw, "").trim();
    return s.isEmpty() ? null : s;
  }
}

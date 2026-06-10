package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.config.properties.UiProperties;
import cafe.woden.ircclient.ui.SwingEdt;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsDocumentListener;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettings;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeIdUtils;
import cafe.woden.ircclient.ui.settings.theme.ThemeManager;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettingsBus;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.awt.Component;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JTextField;

public final class AppearanceLivePreviewSession implements AutoCloseable {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();
  private static final String FLAT_ONLY_TOOLTIP =
      MESSAGES.text("preferences.appearance.tooltip.flatLafOnly");
  private static final String DENSITY_UNSUPPORTED_TOOLTIP =
      MESSAGES.text("preferences.appearance.tooltip.densitySupportedThemesOnly");
  private static final ThemeAccentSettings DEFAULT_ACCENT_SETTINGS =
      new ThemeAccentSettings(
          UiProperties.DEFAULT_ACCENT_COLOR, UiProperties.DEFAULT_ACCENT_STRENGTH);
  private static final ThemeTweakSettings DEFAULT_TWEAK_SETTINGS =
      new ThemeTweakSettings(ThemeTweakSettings.ThemeDensity.AUTO, 10);
  private static final ChatThemeSettings DEFAULT_CHAT_THEME_SETTINGS =
      new ChatThemeSettings(
          ChatThemeSettings.Preset.DEFAULT, null, null, null, 35, null, null, null, null, null);

  private final ThemeControls theme;
  private final AccentControls accent;
  private final ChatThemeControls chatTheme;
  private final FontControls fonts;
  private final TweakControls tweaks;
  private final UiSettingsBus settingsBus;
  private final ThemeManager themeManager;
  private final ThemeAccentSettingsBus accentSettingsBus;
  private final ThemeTweakSettingsBus tweakSettingsBus;
  private final ChatThemeSettingsBus chatThemeSettingsBus;

  private final AtomicReference<String> committedThemeId;
  private final AtomicReference<String> lastPreviewThemeId;
  private final AtomicReference<UiSettings> committedUiSettings;
  private final AtomicReference<ThemeAccentSettings> committedAccentSettings;
  private final AtomicReference<ThemeTweakSettings> committedTweakSettings;
  private final AtomicReference<ChatThemeSettings> committedChatThemeSettings;
  private final AtomicBoolean suppressLivePreview = new AtomicBoolean(false);

  private final OptionalHexPreviewState lastValidAccentHex;
  private final OptionalHexPreviewState lastValidChatTimestampHex;
  private final OptionalHexPreviewState lastValidChatSystemHex;
  private final OptionalHexPreviewState lastValidChatMentionHex;
  private final OptionalHexPreviewState lastValidChatMessageHex;
  private final OptionalHexPreviewState lastValidChatNoticeHex;
  private final OptionalHexPreviewState lastValidChatActionHex;
  private final OptionalHexPreviewState lastValidChatErrorHex;
  private final OptionalHexPreviewState lastValidChatPresenceHex;

  private final RxDebouncedEdtTrigger lafPreviewDebounce;
  private final RxDebouncedEdtTrigger chatPreviewDebounce;
  private final RxDebouncedEdtTrigger fontPreviewDebounce;

  AppearanceLivePreviewSession(
      UiSettings current,
      ThemeAccentSettings initialAccent,
      ThemeTweakSettings initialTweaks,
      ChatThemeSettings initialChatTheme,
      ThemeControls theme,
      AccentControls accent,
      ChatThemeControls chatTheme,
      FontControls fonts,
      TweakControls tweaks,
      UiSettingsBus settingsBus,
      ThemeManager themeManager,
      ThemeAccentSettingsBus accentSettingsBus,
      ThemeTweakSettingsBus tweakSettingsBus,
      ChatThemeSettingsBus chatThemeSettingsBus) {
    this.theme = Objects.requireNonNull(theme, "theme");
    this.accent = Objects.requireNonNull(accent, "accent");
    this.chatTheme = Objects.requireNonNull(chatTheme, "chatTheme");
    this.fonts = Objects.requireNonNull(fonts, "fonts");
    this.tweaks = Objects.requireNonNull(tweaks, "tweaks");
    this.settingsBus = settingsBus;
    this.themeManager = themeManager;
    this.accentSettingsBus = accentSettingsBus;
    this.tweakSettingsBus = tweakSettingsBus;
    this.chatThemeSettingsBus = chatThemeSettingsBus;

    ThemeAccentSettings committedAccent = fallbackAccent(initialAccent);
    ThemeTweakSettings committedTweaks = fallbackTweaks(initialTweaks);
    ChatThemeSettings committedChatTheme = fallbackChatTheme(initialChatTheme);
    String committedTheme = ThemeIdUtils.normalizeThemeId(current != null ? current.theme() : null);

    this.committedThemeId = new AtomicReference<>(committedTheme);
    this.lastPreviewThemeId = new AtomicReference<>(committedTheme);
    this.committedUiSettings = new AtomicReference<>(current);
    this.committedAccentSettings = new AtomicReference<>(committedAccent);
    this.committedTweakSettings = new AtomicReference<>(committedTweaks);
    this.committedChatThemeSettings = new AtomicReference<>(committedChatTheme);

    this.lastValidAccentHex = new OptionalHexPreviewState(committedAccent.accentColor());
    this.lastValidChatTimestampHex =
        new OptionalHexPreviewState(committedChatTheme.timestampColor());
    this.lastValidChatSystemHex = new OptionalHexPreviewState(committedChatTheme.systemColor());
    this.lastValidChatMentionHex = new OptionalHexPreviewState(committedChatTheme.mentionBgColor());
    this.lastValidChatMessageHex = new OptionalHexPreviewState(committedChatTheme.messageColor());
    this.lastValidChatNoticeHex = new OptionalHexPreviewState(committedChatTheme.noticeColor());
    this.lastValidChatActionHex = new OptionalHexPreviewState(committedChatTheme.actionColor());
    this.lastValidChatErrorHex = new OptionalHexPreviewState(committedChatTheme.errorColor());
    this.lastValidChatPresenceHex = new OptionalHexPreviewState(committedChatTheme.presenceColor());

    this.lafPreviewDebounce = new RxDebouncedEdtTrigger(140, this::applyLafPreview);
    this.chatPreviewDebounce = new RxDebouncedEdtTrigger(120, this::applyChatPreview);
    this.fontPreviewDebounce = new RxDebouncedEdtTrigger(120, this::applyFontPreview);
  }

  void attachListeners() {
    updateTweakCapabilityUi();

    final boolean[] ignoreThemeComboEvents = new boolean[] {true};
    theme.combo.addActionListener(
        e -> {
          if (ignoreThemeComboEvents[0]) return;
          updateTweakCapabilityUi();
          scheduleLafPreview();
        });
    ignoreThemeComboEvents[0] = false;

    accent.enabled.addActionListener(e -> scheduleLafPreview());
    accent.preset.addActionListener(e -> scheduleLafPreview());
    accent.strength.addChangeListener(e -> scheduleLafPreview());
    lastValidAccentHex.attachTo(accent.hex, this::scheduleLafPreview);

    tweaks.density.addActionListener(e -> scheduleLafPreview());
    tweaks.cornerRadius.addChangeListener(e -> scheduleLafPreview());
    tweaks.uiFontOverrideEnabled.addActionListener(
        e -> {
          tweaks.applyUiFontEnabledState.run();
          scheduleLafPreview();
        });
    tweaks.uiFontFamily.addActionListener(e -> scheduleLafPreview());
    tweaks.uiFontFamily.addItemListener(
        e -> {
          if (e != null && e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            scheduleLafPreview();
          }
        });
    Component uiFontFamilyEditor =
        tweaks.uiFontFamily.getEditor() != null
            ? tweaks.uiFontFamily.getEditor().getEditorComponent()
            : null;
    if (uiFontFamilyEditor instanceof JTextField tf) {
      tf.getDocument().addDocumentListener(new SettingsDocumentListener(this::scheduleLafPreview));
    }
    tweaks.uiFontSize.addChangeListener(e -> scheduleLafPreview());

    chatTheme.preset.addActionListener(e -> scheduleChatPreview());
    chatTheme.mentionStrength.addChangeListener(e -> scheduleChatPreview());
    lastValidChatTimestampHex.attachTo(chatTheme.timestamp.hex, this::scheduleChatPreview);
    lastValidChatSystemHex.attachTo(chatTheme.system.hex, this::scheduleChatPreview);
    lastValidChatMentionHex.attachTo(chatTheme.mention.hex, this::scheduleChatPreview);
    lastValidChatMessageHex.attachTo(chatTheme.message.hex, this::scheduleChatPreview);
    lastValidChatNoticeHex.attachTo(chatTheme.notice.hex, this::scheduleChatPreview);
    lastValidChatActionHex.attachTo(chatTheme.action.hex, this::scheduleChatPreview);
    lastValidChatErrorHex.attachTo(chatTheme.error.hex, this::scheduleChatPreview);
    lastValidChatPresenceHex.attachTo(chatTheme.presence.hex, this::scheduleChatPreview);

    fonts.fontFamily.addActionListener(e -> scheduleFontPreview());
    fonts.fontFamily.addItemListener(
        e -> {
          if (e != null && e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            scheduleFontPreview();
          }
        });
    Component fontFamilyEditor =
        fonts.fontFamily.getEditor() != null
            ? fonts.fontFamily.getEditor().getEditorComponent()
            : null;
    if (fontFamilyEditor instanceof JTextField tf) {
      tf.getDocument().addDocumentListener(new SettingsDocumentListener(this::scheduleFontPreview));
    }
    fonts.fontSize.addChangeListener(e -> scheduleFontPreview());
  }

  public void commit(
      UiSettings next,
      ThemeAccentSettings nextAccent,
      ThemeTweakSettings nextTweaks,
      ChatThemeSettings nextChatTheme) {
    committedThemeId.set(ThemeIdUtils.normalizeThemeId(next != null ? next.theme() : null));
    committedUiSettings.set(next);
    ThemeAccentSettings committedAccent = fallbackAccent(nextAccent);
    ThemeTweakSettings committedTweaks = fallbackTweaks(nextTweaks);
    ChatThemeSettings committedChatTheme = fallbackChatTheme(nextChatTheme);
    committedAccentSettings.set(committedAccent);
    committedTweakSettings.set(committedTweaks);
    committedChatThemeSettings.set(committedChatTheme);
    lastValidAccentHex.set(committedAccent.accentColor());
    lastValidChatTimestampHex.set(committedChatTheme.timestampColor());
    lastValidChatSystemHex.set(committedChatTheme.systemColor());
    lastValidChatMentionHex.set(committedChatTheme.mentionBgColor());
    lastValidChatMessageHex.set(committedChatTheme.messageColor());
    lastValidChatNoticeHex.set(committedChatTheme.noticeColor());
    lastValidChatActionHex.set(committedChatTheme.actionColor());
    lastValidChatErrorHex.set(committedChatTheme.errorColor());
    lastValidChatPresenceHex.set(committedChatTheme.presenceColor());
  }

  public void restoreCommittedAppearance() {
    if (themeManager == null) return;
    suppressLivePreview.set(true);
    try {
      lafPreviewDebounce.cancelPending();
      chatPreviewDebounce.cancelPending();
      fontPreviewDebounce.cancelPending();

      UiSettings committedUi = committedUiSettings.get();
      UiSettings liveUi = settingsBus != null ? settingsBus.get() : null;
      ThemeAccentSettings targetAccent = fallbackAccent(committedAccentSettings.get());
      ThemeAccentSettings liveAccent = accentSettingsBus != null ? accentSettingsBus.get() : null;
      ThemeTweakSettings targetTweaks = fallbackTweaks(committedTweakSettings.get());
      ThemeTweakSettings liveTweaks = tweakSettingsBus != null ? tweakSettingsBus.get() : null;
      ChatThemeSettings targetChatTheme = fallbackChatTheme(committedChatThemeSettings.get());
      ChatThemeSettings liveChatTheme =
          chatThemeSettingsBus != null ? chatThemeSettingsBus.get() : null;
      String committedTheme = committedThemeId.get();
      String liveTheme = ThemeIdUtils.normalizeThemeId(liveUi != null ? liveUi.theme() : null);

      RollbackPlan rollbackPlan =
          planRollback(
              committedTheme,
              liveTheme,
              committedUi,
              liveUi,
              accentSettingsBus != null,
              targetAccent,
              liveAccent,
              tweakSettingsBus != null,
              targetTweaks,
              liveTweaks,
              chatThemeSettingsBus != null,
              targetChatTheme,
              liveChatTheme);

      if (!rollbackPlan.hasAnyWork()) {
        lastPreviewThemeId.set(committedTheme);
        return;
      }
      if (rollbackPlan.restoreUiSettings() && committedUi != null) {
        settingsBus.set(committedUi);
      }
      if (rollbackPlan.restoreAccentSettings() && accentSettingsBus != null) {
        accentSettingsBus.set(targetAccent);
      }
      if (rollbackPlan.restoreTweakSettings() && tweakSettingsBus != null) {
        tweakSettingsBus.set(targetTweaks);
      }
      if (rollbackPlan.restoreChatThemeSettings() && chatThemeSettingsBus != null) {
        chatThemeSettingsBus.set(targetChatTheme);
      }

      if (rollbackPlan.applyTheme()) {
        themeManager.applyTheme(committedTheme);
        lastPreviewThemeId.set(committedTheme);
      } else if (rollbackPlan.applyAppearance()) {
        themeManager.applyAppearance(false);
        lastPreviewThemeId.set(committedTheme);
      } else if (rollbackPlan.refreshChatStyles()) {
        themeManager.refreshChatStyles();
      }
    } finally {
      suppressLivePreview.set(false);
    }
  }

  static RollbackPlan planRollback(
      String committedThemeId,
      String liveThemeId,
      UiSettings committedUi,
      UiSettings liveUi,
      boolean accentBusAvailable,
      ThemeAccentSettings committedAccent,
      ThemeAccentSettings liveAccent,
      boolean tweakBusAvailable,
      ThemeTweakSettings committedTweaks,
      ThemeTweakSettings liveTweaks,
      boolean chatThemeBusAvailable,
      ChatThemeSettings committedChatTheme,
      ChatThemeSettings liveChatTheme) {
    boolean themeChanged = !ThemeIdUtils.sameTheme(committedThemeId, liveThemeId);
    boolean uiChanged = committedUi != null && !Objects.equals(committedUi, liveUi);
    boolean accentChanged = accentBusAvailable && !Objects.equals(committedAccent, liveAccent);
    boolean tweakChanged = tweakBusAvailable && !Objects.equals(committedTweaks, liveTweaks);
    boolean chatThemeChanged =
        chatThemeBusAvailable && !Objects.equals(committedChatTheme, liveChatTheme);

    boolean applyTheme = themeChanged;
    boolean applyAppearance = !applyTheme && (uiChanged || accentChanged || tweakChanged);
    boolean refreshChatStyles = !applyTheme && !applyAppearance && chatThemeChanged;
    return new RollbackPlan(
        uiChanged,
        accentChanged,
        tweakChanged,
        chatThemeChanged,
        applyTheme,
        applyAppearance,
        refreshChatStyles);
  }

  @Override
  public void close() {
    lafPreviewDebounce.close();
    chatPreviewDebounce.close();
    fontPreviewDebounce.close();
  }

  private void applyLafPreview() {
    if (suppressLivePreview.get()) return;
    if (themeManager == null) return;

    String selectedTheme =
        ThemeIdUtils.normalizeThemeId(String.valueOf(theme.combo.getSelectedItem()));
    if (selectedTheme.isBlank()) return;

    if (tweakSettingsBus != null) {
      tweakSettingsBus.set(readTweakSettings());
    }

    if (accentSettingsBus != null) {
      String hex = accent.enabled.isSelected() ? lastValidAccentHex.resolve(accent.hex) : null;
      accentSettingsBus.set(new ThemeAccentSettings(hex, accent.strength.getValue()));
    }

    if (!Objects.equals(selectedTheme, lastPreviewThemeId.get())) {
      themeManager.applyTheme(selectedTheme);
      lastPreviewThemeId.set(selectedTheme);
    } else {
      themeManager.applyAppearance(true);
    }

    try {
      accent.updateChip.run();
    } catch (Exception ignored) {
    }
  }

  private void applyChatPreview() {
    if (suppressLivePreview.get()) return;
    if (themeManager == null) return;
    if (chatThemeSettingsBus == null) return;

    chatThemeSettingsBus.set(readChatThemeSettings());
    themeManager.refreshChatStyles();
  }

  private void applyFontPreview() {
    if (suppressLivePreview.get()) return;
    UiSettings base = settingsBus != null ? settingsBus.get() : null;
    if (base == null) return;

    String family = PreferencesUiSupport.selectedComboText(fonts.fontFamily);
    if (family.isBlank()) family = "Monospaced";
    int size =
        SettingsRangeSupport.normalizeFontSize(PreferencesUiSupport.spinnerInt(fonts.fontSize));

    settingsBus.set(base.withChatFontFamily(family).withChatFontSize(size));
  }

  private void scheduleLafPreview() {
    if (suppressLivePreview.get()) return;
    lafPreviewDebounce.trigger();
  }

  private void scheduleChatPreview() {
    if (suppressLivePreview.get()) return;
    chatPreviewDebounce.trigger();
  }

  private void scheduleFontPreview() {
    if (suppressLivePreview.get()) return;
    fontPreviewDebounce.trigger();
  }

  private void updateTweakCapabilityUi() {
    Object selectedTheme = theme.combo.getSelectedItem();
    String selectedThemeId = selectedTheme != null ? selectedTheme.toString() : "";
    boolean densityTweakCapable = ThemeIdUtils.isLikelyDensityTweakTarget(selectedThemeId);
    boolean cornerTweakCapable = ThemeIdUtils.isLikelyFlatTarget(selectedThemeId);
    tweaks.density.setEnabled(densityTweakCapable);
    tweaks.cornerRadius.setEnabled(cornerTweakCapable);
    tweaks.density.setToolTipText(
        densityTweakCapable ? AppearanceTooltips.DENSITY : DENSITY_UNSUPPORTED_TOOLTIP);
    tweaks.cornerRadius.setToolTipText(
        cornerTweakCapable ? AppearanceTooltips.CORNER_RADIUS : FLAT_ONLY_TOOLTIP);
  }

  private ThemeTweakSettings readTweakSettings() {
    return AppearanceControlsSupport.readTweakSettings(tweaks);
  }

  private ChatThemeSettings readChatThemeSettings() {
    ChatThemeSettings.Preset preset =
        PreferencesUiSupport.selectedComboItem(
            chatTheme.preset, ChatThemeSettings.Preset.class, ChatThemeSettings.Preset.DEFAULT);
    return new ChatThemeSettings(
        preset,
        lastValidChatTimestampHex.resolve(chatTheme.timestamp.hex),
        lastValidChatSystemHex.resolve(chatTheme.system.hex),
        lastValidChatMentionHex.resolve(chatTheme.mention.hex),
        chatTheme.mentionStrength.getValue(),
        lastValidChatMessageHex.resolve(chatTheme.message.hex),
        lastValidChatNoticeHex.resolve(chatTheme.notice.hex),
        lastValidChatActionHex.resolve(chatTheme.action.hex),
        lastValidChatErrorHex.resolve(chatTheme.error.hex),
        lastValidChatPresenceHex.resolve(chatTheme.presence.hex));
  }

  private static ThemeAccentSettings fallbackAccent(ThemeAccentSettings settings) {
    return settings != null ? settings : DEFAULT_ACCENT_SETTINGS;
  }

  private static ThemeTweakSettings fallbackTweaks(ThemeTweakSettings settings) {
    return settings != null ? settings : DEFAULT_TWEAK_SETTINGS;
  }

  private static ChatThemeSettings fallbackChatTheme(ChatThemeSettings settings) {
    return settings != null ? settings : DEFAULT_CHAT_THEME_SETTINGS;
  }

  static record RollbackPlan(
      boolean restoreUiSettings,
      boolean restoreAccentSettings,
      boolean restoreTweakSettings,
      boolean restoreChatThemeSettings,
      boolean applyTheme,
      boolean applyAppearance,
      boolean refreshChatStyles) {
    boolean hasAnyWork() {
      return restoreUiSettings
          || restoreAccentSettings
          || restoreTweakSettings
          || restoreChatThemeSettings
          || applyTheme
          || applyAppearance
          || refreshChatStyles;
    }
  }

  private static final class RxDebouncedEdtTrigger implements AutoCloseable {
    private final AtomicLong sequence = new AtomicLong(0L);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Subject<Long> signals = PublishSubject.<Long>create().toSerialized();
    private final Disposable subscription;

    private RxDebouncedEdtTrigger(long debounceMs, Runnable action) {
      Runnable safeAction = action == null ? () -> {} : action;
      this.subscription =
          signals
              .debounce(Math.max(0L, debounceMs), TimeUnit.MILLISECONDS)
              .observeOn(SwingEdt.scheduler())
              .subscribe(
                  seq -> {
                    if (closed.get()) return;
                    if (seq.longValue() != sequence.get()) return;
                    try {
                      safeAction.run();
                    } catch (Exception ignored) {
                    }
                  },
                  err -> {});
    }

    void trigger() {
      if (closed.get()) return;
      signals.onNext(sequence.incrementAndGet());
    }

    void cancelPending() {
      sequence.incrementAndGet();
    }

    @Override
    public void close() {
      if (!closed.compareAndSet(false, true)) return;
      sequence.incrementAndGet();
      try {
        subscription.dispose();
      } catch (Exception ignored) {
      }
      try {
        signals.onComplete();
      } catch (Exception ignored) {
      }
    }
  }
}

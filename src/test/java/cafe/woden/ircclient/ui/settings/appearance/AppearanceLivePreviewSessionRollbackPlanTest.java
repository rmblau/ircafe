package cafe.woden.ircclient.ui.settings.appearance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettings;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettingsTestFixtures;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeAppearanceSettingsTestFixtures;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettings;
import org.junit.jupiter.api.Test;

class AppearanceLivePreviewSessionRollbackPlanTest {

  @Test
  void noChangesProducesNoRollbackWork() {
    UiSettings ui = mock(UiSettings.class);
    ThemeAccentSettings accent = ThemeAppearanceSettingsTestFixtures.accent("#336699", 42);
    ThemeTweakSettings tweaks =
        ThemeAppearanceSettingsTestFixtures.tweakBuilder()
            .density(ThemeTweakSettings.ThemeDensity.COZY)
            .cornerRadius(8)
            .uiFontOverrideEnabled(true)
            .build();
    ChatThemeSettings chat = ChatThemeSettingsTestFixtures.defaults();

    AppearanceLivePreviewSession.RollbackPlan plan =
        AppearanceLivePreviewSession.planRollback(
            "darcula", "Darcula", ui, ui, true, accent, accent, true, tweaks, tweaks, true, chat,
            chat);

    assertFalse(plan.hasAnyWork());
    assertFalse(plan.applyTheme());
    assertFalse(plan.applyAppearance());
    assertFalse(plan.refreshChatStyles());
  }

  @Test
  void themeChangePrefersThemeApplyOverAppearanceOrChatRefresh() {
    UiSettings committedUi = mock(UiSettings.class);
    UiSettings liveUi = mock(UiSettings.class);
    ThemeAccentSettings committedAccent = ThemeAppearanceSettingsTestFixtures.accent("#336699", 42);
    ThemeAccentSettings liveAccent = ThemeAppearanceSettingsTestFixtures.accent("#6699CC", 65);
    ThemeTweakSettings committedTweaks = ThemeAppearanceSettingsTestFixtures.tweakDefaults();
    ThemeTweakSettings liveTweaks =
        ThemeAppearanceSettingsTestFixtures.tweakBuilder()
            .density(ThemeTweakSettings.ThemeDensity.SPACIOUS)
            .cornerRadius(14)
            .uiFontOverrideEnabled(true)
            .uiFontSize(16)
            .build();
    ChatThemeSettings committedChat = ChatThemeSettingsTestFixtures.defaults();
    ChatThemeSettings liveChat =
        ChatThemeSettingsTestFixtures.builder()
            .preset(ChatThemeSettings.Preset.ACCENTED)
            .mentionStrength(60)
            .build();

    AppearanceLivePreviewSession.RollbackPlan plan =
        AppearanceLivePreviewSession.planRollback(
            "darcula",
            "light",
            committedUi,
            liveUi,
            true,
            committedAccent,
            liveAccent,
            true,
            committedTweaks,
            liveTweaks,
            true,
            committedChat,
            liveChat);

    assertTrue(plan.applyTheme());
    assertFalse(plan.applyAppearance());
    assertFalse(plan.refreshChatStyles());
  }

  @Test
  void accentOrTweakChangesRequestAppearanceApply() {
    UiSettings ui = mock(UiSettings.class);
    ThemeAccentSettings committedAccent = ThemeAppearanceSettingsTestFixtures.accent("#336699", 42);
    ThemeAccentSettings liveAccent = ThemeAppearanceSettingsTestFixtures.accent("#336699", 60);
    ThemeTweakSettings tweaks = ThemeAppearanceSettingsTestFixtures.tweakDefaults();
    ChatThemeSettings chat = ChatThemeSettingsTestFixtures.defaults();

    AppearanceLivePreviewSession.RollbackPlan plan =
        AppearanceLivePreviewSession.planRollback(
            "darcula",
            "darcula",
            ui,
            ui,
            true,
            committedAccent,
            liveAccent,
            true,
            tweaks,
            tweaks,
            true,
            chat,
            chat);

    assertTrue(plan.restoreAccentSettings());
    assertTrue(plan.applyAppearance());
    assertFalse(plan.applyTheme());
    assertFalse(plan.refreshChatStyles());
  }

  @Test
  void chatThemeOnlyChangesRequestChatRefresh() {
    UiSettings ui = mock(UiSettings.class);
    ThemeAccentSettings accent = ThemeAppearanceSettingsTestFixtures.accent("#336699", 42);
    ThemeTweakSettings tweaks = ThemeAppearanceSettingsTestFixtures.tweakDefaults();
    ChatThemeSettings committedChat = ChatThemeSettingsTestFixtures.defaults();
    ChatThemeSettings liveChat =
        ChatThemeSettingsTestFixtures.builder().timestampColor("#AAAAAA").build();

    AppearanceLivePreviewSession.RollbackPlan plan =
        AppearanceLivePreviewSession.planRollback(
            "darcula",
            "darcula",
            ui,
            ui,
            true,
            accent,
            accent,
            true,
            tweaks,
            tweaks,
            true,
            committedChat,
            liveChat);

    assertTrue(plan.restoreChatThemeSettings());
    assertFalse(plan.applyTheme());
    assertFalse(plan.applyAppearance());
    assertTrue(plan.refreshChatStyles());
  }

  @Test
  void busAvailabilityGuardsRestoreWork() {
    UiSettings ui = mock(UiSettings.class);
    ThemeAccentSettings committedAccent = ThemeAppearanceSettingsTestFixtures.accent("#336699", 42);
    ThemeAccentSettings liveAccent = ThemeAppearanceSettingsTestFixtures.accent("#336699", 60);
    ThemeTweakSettings tweaks = ThemeAppearanceSettingsTestFixtures.tweakDefaults();
    ChatThemeSettings chat = ChatThemeSettingsTestFixtures.defaults();

    AppearanceLivePreviewSession.RollbackPlan plan =
        AppearanceLivePreviewSession.planRollback(
            "darcula",
            "darcula",
            ui,
            ui,
            false,
            committedAccent,
            liveAccent,
            true,
            tweaks,
            tweaks,
            true,
            chat,
            chat);

    assertFalse(plan.restoreAccentSettings());
    assertFalse(plan.applyTheme());
    assertFalse(plan.applyAppearance());
    assertFalse(plan.refreshChatStyles());
    assertFalse(plan.hasAnyWork());
  }
}

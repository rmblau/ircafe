package cafe.woden.ircclient.app.api;

import cafe.woden.ircclient.app.outbound.backend.spi.BackendEditorProfile;
import java.util.List;
import java.util.Objects;

/** Built-in server-editor metadata for the core IRC, Quassel, and Matrix backends. */
public final class BuiltInBackendEditorProfiles {

  private BuiltInBackendEditorProfiles() {}

  public static List<BackendEditorProfileSpec> all() {
    return cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendEditorProfiles.all().stream()
        .map(BuiltInBackendEditorProfiles::toAppProfile)
        .toList();
  }

  public static BackendEditorProfileSpec irc() {
    return toAppProfile(
        cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendEditorProfiles.irc());
  }

  public static BackendEditorProfileSpec quasselCore() {
    return toAppProfile(
        cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendEditorProfiles.quasselCore());
  }

  public static BackendEditorProfileSpec matrix() {
    return toAppProfile(
        cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendEditorProfiles.matrix());
  }

  private static BackendEditorProfileSpec toAppProfile(BackendEditorProfile profile) {
    Objects.requireNonNull(profile, "profile");
    return new BackendEditorProfileSpec(
        profile.backendId(),
        profile.displayName(),
        toAppUiMode(profile.uiMode()),
        profile.defaultPlainPort(),
        profile.defaultTlsPort(),
        profile.directAuthEnabled(),
        profile.matrixAuthSupported(),
        profile.requiresNick(),
        profile.usesNickAsDefaultLogin(),
        profile.supportsQuasselCoreCommands(),
        profile.defaultLoginFallback(),
        profile.hostLabel(),
        profile.serverPasswordLabel(),
        profile.nickLabel(),
        profile.loginLabel(),
        profile.realNameLabel(),
        profile.tlsToggleLabel(),
        profile.connectionHint(),
        profile.authDisabledHint(),
        profile.serverPasswordPlaceholder(),
        profile.hostPlaceholder(),
        profile.loginPlaceholder(),
        profile.nickPlaceholder(),
        profile.realNamePlaceholder());
  }

  private static BackendUiMode toAppUiMode(
      cafe.woden.ircclient.app.outbound.backend.spi.BackendUiMode uiMode) {
    return switch (Objects.requireNonNull(uiMode, "uiMode")) {
      case IRC -> BackendUiMode.IRC;
      case MATRIX -> BackendUiMode.MATRIX;
    };
  }
}

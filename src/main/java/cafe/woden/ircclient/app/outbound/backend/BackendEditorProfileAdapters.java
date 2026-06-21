package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.api.BackendEditorProfileSpec;
import cafe.woden.ircclient.app.outbound.backend.spi.BackendEditorProfile;
import cafe.woden.ircclient.app.outbound.backend.spi.BackendUiMode;
import java.util.Objects;

/** Bridges plugin-facing backend editor profile metadata to the app-owned UI model. */
final class BackendEditorProfileAdapters {

  private BackendEditorProfileAdapters() {}

  static BackendEditorProfile toPluginProfile(BackendEditorProfileSpec profileSpec) {
    Objects.requireNonNull(profileSpec, "profileSpec");
    return new BackendEditorProfile(
        profileSpec.backendId(),
        profileSpec.displayName(),
        toPluginUiMode(profileSpec.uiMode()),
        profileSpec.defaultPlainPort(),
        profileSpec.defaultTlsPort(),
        profileSpec.directAuthEnabled(),
        profileSpec.matrixAuthSupported(),
        profileSpec.requiresNick(),
        profileSpec.usesNickAsDefaultLogin(),
        profileSpec.supportsQuasselCoreCommands(),
        profileSpec.defaultLoginFallback(),
        profileSpec.hostLabel(),
        profileSpec.serverPasswordLabel(),
        profileSpec.nickLabel(),
        profileSpec.loginLabel(),
        profileSpec.realNameLabel(),
        profileSpec.tlsToggleLabel(),
        profileSpec.connectionHint(),
        profileSpec.authDisabledHint(),
        profileSpec.serverPasswordPlaceholder(),
        profileSpec.hostPlaceholder(),
        profileSpec.loginPlaceholder(),
        profileSpec.nickPlaceholder(),
        profileSpec.realNamePlaceholder());
  }

  static BackendEditorProfileSpec toAppProfile(BackendEditorProfile profile) {
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

  private static BackendUiMode toPluginUiMode(cafe.woden.ircclient.app.api.BackendUiMode uiMode) {
    return switch (Objects.requireNonNull(uiMode, "uiMode")) {
      case IRC -> BackendUiMode.IRC;
      case MATRIX -> BackendUiMode.MATRIX;
    };
  }

  private static cafe.woden.ircclient.app.api.BackendUiMode toAppUiMode(BackendUiMode uiMode) {
    return switch (Objects.requireNonNull(uiMode, "uiMode")) {
      case IRC -> cafe.woden.ircclient.app.api.BackendUiMode.IRC;
      case MATRIX -> cafe.woden.ircclient.app.api.BackendUiMode.MATRIX;
    };
  }
}

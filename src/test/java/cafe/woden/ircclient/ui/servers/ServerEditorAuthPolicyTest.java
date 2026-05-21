package cafe.woden.ircclient.ui.servers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.IrcPropertiesTestFixtures;
import org.junit.jupiter.api.Test;

class ServerEditorAuthPolicyTest {

  private static final ServerEditorBackendProfiles BACKEND_PROFILES =
      ServerEditorBackendProfiles.builtIns();

  @Test
  void effectiveAuthModeDisablesTraditionalAuthForMatrixAndQuasselProfiles() {
    assertEquals(
        ServerEditorAuthMode.DISABLED,
        ServerEditorAuthPolicy.effectiveAuthMode(
            BACKEND_PROFILES.profileForBackendId("matrix"), ServerEditorAuthMode.SASL));
    assertEquals(
        ServerEditorAuthMode.DISABLED,
        ServerEditorAuthPolicy.effectiveAuthMode(
            BACKEND_PROFILES.profileForBackendId("quassel-core"), ServerEditorAuthMode.NICKSERV));
  }

  @Test
  void seedMatrixAuthModePrefersPasswordWhenSeedUsesMatrixPasswordMechanism() {
    IrcProperties.Server seed =
        IrcPropertiesTestFixtures.serverBuilder("matrix")
            .host("https://matrix.example.org")
            .port(443)
            .backend(IrcProperties.Server.Backend.MATRIX)
            .nick("")
            .login("")
            .realName("")
            .sasl(
                new IrcProperties.Server.Sasl(
                    true,
                    "alice",
                    "secret",
                    ServerEditorAuthPolicy.MATRIX_PASSWORD_AUTH_MECHANISM,
                    true))
            .build();

    assertEquals(
        ServerEditorMatrixAuthMode.USERNAME_PASSWORD,
        ServerEditorAuthPolicy.seedMatrixAuthMode(
            BACKEND_PROFILES.profileForBackendId("matrix"), seed));
  }

  @Test
  void resolveLoginPrefersMatrixUsernameThenNickThenFallback() {
    ServerEditorBackendProfile matrix = BACKEND_PROFILES.profileForBackendId("matrix");
    ServerEditorBackendProfile irc = BACKEND_PROFILES.profileForBackendId("irc");

    assertEquals(
        "alice",
        ServerEditorAuthPolicy.resolveLogin(
            matrix, "", "nick", "alice", ServerEditorMatrixAuthMode.USERNAME_PASSWORD));
    assertEquals(
        "nick",
        ServerEditorAuthPolicy.resolveLogin(
            irc, "", "nick", "", ServerEditorMatrixAuthMode.ACCESS_TOKEN));
    assertEquals(
        "quassel-user",
        ServerEditorAuthPolicy.resolveLogin(
            BACKEND_PROFILES.profileForBackendId("quassel-core"),
            "",
            "",
            "",
            ServerEditorMatrixAuthMode.ACCESS_TOKEN));
  }

  @Test
  void buildSaslUsesMatrixPasswordMechanismAndClearsServerPassword() {
    ServerEditorAuthPolicy.SaslBuildResult sasl =
        ServerEditorAuthPolicy.buildSasl(
            BACKEND_PROFILES.profileForBackendId("matrix"),
            ServerEditorAuthMode.DISABLED,
            ServerEditorMatrixAuthMode.USERNAME_PASSWORD,
            "matrix-secret",
            "alice",
            "",
            "",
            "",
            true);

    assertEquals("", sasl.serverPassword());
    assertEquals(ServerEditorAuthMode.DISABLED, sasl.authMode());
    assertEquals(ServerEditorAuthPolicy.MATRIX_PASSWORD_AUTH_MECHANISM, sasl.sasl().mechanism());
    assertEquals("alice", sasl.sasl().username());
  }

  @Test
  void buildNickservRequiresPasswordWhenEnabled() {
    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                ServerEditorAuthPolicy.buildNickserv(
                    ServerEditorAuthMode.NICKSERV, "NickServ", "", true));

    assertEquals("NickServ password is required when NickServ is enabled", error.getMessage());
    assertFalse(
        ServerEditorAuthPolicy.buildNickserv(ServerEditorAuthMode.DISABLED, "NickServ", "", true)
            .enabled());
  }

  @Test
  void saslValidationUsesSharedMechanismRules() {
    ServerEditorAuthPolicy.SaslValidation external =
        ServerEditorAuthPolicy.saslValidation(ServerEditorAuthMode.SASL, "EXTERNAL", "", "");
    ServerEditorAuthPolicy.SaslValidation scram =
        ServerEditorAuthPolicy.saslValidation(ServerEditorAuthMode.SASL, "SCRAM-SHA-256", "", "");
    ServerEditorAuthPolicy.SaslValidation autoNoSecret =
        ServerEditorAuthPolicy.saslValidation(ServerEditorAuthMode.SASL, "AUTO", "", "");

    assertTrue(external.applicable());
    assertFalse(external.userBad());
    assertFalse(external.secretBad());
    assertTrue(scram.applicable());
    assertTrue(scram.userBad());
    assertTrue(scram.secretBad());
    assertTrue(autoNoSecret.applicable());
    assertFalse(autoNoSecret.userBad());
    assertFalse(autoNoSecret.secretBad());
  }

  @Test
  void matrixValidationTracksCredentialAndUsernameRequirements() {
    ServerEditorAuthPolicy.MatrixValidation accessToken =
        ServerEditorAuthPolicy.matrixValidation(
            BACKEND_PROFILES.profileForBackendId("matrix"),
            ServerEditorMatrixAuthMode.ACCESS_TOKEN,
            "",
            "");
    ServerEditorAuthPolicy.MatrixValidation passwordMode =
        ServerEditorAuthPolicy.matrixValidation(
            BACKEND_PROFILES.profileForBackendId("matrix"),
            ServerEditorMatrixAuthMode.USERNAME_PASSWORD,
            "secret",
            "");

    assertTrue(accessToken.applicable());
    assertTrue(accessToken.credentialBad());
    assertFalse(accessToken.usernameBad());
    assertTrue(passwordMode.applicable());
    assertFalse(passwordMode.credentialBad());
    assertTrue(passwordMode.usernameBad());
  }

  @Test
  void nickservValidationOnlyAppliesWhenNickservIsSelected() {
    ServerEditorAuthPolicy.NickservValidation enabled =
        ServerEditorAuthPolicy.nickservValidation(ServerEditorAuthMode.NICKSERV, "");
    ServerEditorAuthPolicy.NickservValidation disabled =
        ServerEditorAuthPolicy.nickservValidation(ServerEditorAuthMode.DISABLED, "");

    assertTrue(enabled.applicable());
    assertTrue(enabled.passwordBad());
    assertFalse(disabled.applicable());
    assertFalse(disabled.passwordBad());
  }

  @Test
  void seedAuthModePrefersSaslThenNickserv() {
    assertEquals(
        ServerEditorAuthMode.SASL,
        ServerEditorAuthPolicy.seedAuthMode(
            IrcPropertiesTestFixtures.serverBuilder("libera")
                .host("irc.libera.chat")
                .nick("tester")
                .login("tester")
                .realName("Tester")
                .sasl(new IrcProperties.Server.Sasl(true, "tester", "secret", "PLAIN", true))
                .nickserv(new IrcProperties.Server.Nickserv(false, "", "NickServ", true))
                .build()));
    assertEquals(
        ServerEditorAuthMode.NICKSERV,
        ServerEditorAuthPolicy.seedAuthMode(
            IrcPropertiesTestFixtures.serverBuilder("libera")
                .host("irc.libera.chat")
                .nick("tester")
                .login("tester")
                .realName("Tester")
                .sasl(new IrcProperties.Server.Sasl(false, "", "", "PLAIN", true))
                .nickserv(new IrcProperties.Server.Nickserv(true, "secret", "NickServ", true))
                .build()));
    assertEquals(ServerEditorAuthMode.DISABLED, ServerEditorAuthPolicy.seedAuthMode(null));
  }
}

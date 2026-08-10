package cafe.woden.ircclient.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class BuiltInProviderSubprojectBoundaryTest {

  private static final Pattern IMPORT_PATTERN =
      Pattern.compile("(?m)^\\s*import\\s+(?:static\\s+)?([\\w.]+)\\s*;");
  private static final Pattern BUILT_IN_PROVIDER_INCLUDE_PATTERN =
      Pattern.compile("(?m)^\\s*include\\s+['\"](ircafe-builtins-[\\w-]+)['\"]\\s*$");

  @Test
  void builtInProviderSubprojectsStayWithinDeclaredRuntimeBoundaries() throws IOException {
    Set<String> violations = new TreeSet<>();

    for (Path sourceRoot : builtInProviderSourceRoots()) {
      try (Stream<Path> files = Files.walk(sourceRoot)) {
        for (Path file :
            files.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
          Matcher matcher = IMPORT_PATTERN.matcher(Files.readString(file));
          while (matcher.find()) {
            String dependency = matcher.group(1);
            if (!isBuiltInProviderDependency(sourceRoot, dependency)) {
              violations.add(file + " -> " + dependency);
            }
          }
        }
      }
    }

    assertTrue(
        violations.isEmpty(),
        () ->
            "Built-in provider subprojects must stay limited to plugin API contracts, "
                + "their explicitly paired runtime feature, AutoService registration, and JDK "
                + "types. Violations:\n  "
                + String.join("\n  ", violations));
  }

  @Test
  void appIncludesBuiltInProviderJarsOnRuntimeClasspath() throws IOException {
    String settings = Files.readString(Path.of("settings.gradle"));
    String build = Files.readString(Path.of("build.gradle"));

    for (String projectName : builtInProviderProjectNames(settings)) {
      assertBuiltInProviderJarIncluded(build, projectName);
    }
  }

  @Test
  void appDoesNotCompileAgainstServiceLoaderOnlyProviders() throws IOException {
    String settings = Files.readString(Path.of("settings.gradle"));
    String build = Files.readString(Path.of("build.gradle"));

    for (String projectName : builtInProviderProjectNames(settings)) {
      assertServiceLoaderOnlyProvider(build, projectName);
    }
  }

  @Test
  void builtInProviderSubprojectsApplySharedBuildConvention() throws IOException {
    for (Path projectDir : builtInProviderProjectDirs()) {
      Path buildFile = projectDir.resolve("build.gradle");
      String build = Files.readString(buildFile);
      assertTrue(
          build.contains(
              "apply from: rootProject.file('gradle/builtins-provider-conventions.gradle')"),
          buildFile + " should apply the shared built-in provider Gradle convention");
    }
  }

  @Test
  void sharedJavaBuildConventionsOwnTransitiveCyclonedxJarDependencies() throws IOException {
    String moduleConvention =
        Files.readString(Path.of("gradle/java-library-subproject-conventions.gradle"));
    String featureConvention = Files.readString(Path.of("gradle/ircv3-feature-conventions.gradle"));
    String builtInConvention =
        Files.readString(Path.of("gradle/builtins-provider-conventions.gradle"));

    assertTrue(
        moduleConvention.contains("collectProjectDependencies")
            && moduleConvention.contains("cyclonedxDirectBom")
            && moduleConvention.contains("tasks.named('jar')"),
        "the shared Java library subproject convention should wire transitive project JAR producers");
    assertTrue(
        featureConvention.contains("java-library-subproject-conventions.gradle"),
        "the feature convention should reuse the shared Java library subproject setup");
    assertTrue(
        builtInConvention.contains("java-library-subproject-conventions.gradle"),
        "the built-in provider convention should reuse the shared Java library subproject setup");

    for (Path projectDir : builtInProviderProjectDirs()) {
      String build = Files.readString(projectDir.resolve("build.gradle"));
      assertTrue(
          !build.contains("cyclonedxDirectBom") && !build.contains("cyclonedxBom"),
          projectDir + " should not repeat CycloneDX producer wiring locally");
    }
  }

  @Test
  void builtInIrcv3MessageTagsAndMessageIdProvidersOwnFocusedRuntimeParsing() throws IOException {
    assertRuntimeMessageTagParserProvider(
        "ircafe-builtins-ircv3-message-tags",
        "ircafe-feature-ircv3-message-tags",
        "Ircv3MessageTagsExtensionProvider.java");
    assertRuntimeInboundTagProvider(
        "ircafe-builtins-ircv3-message-id",
        "ircafe-feature-ircv3-message-id",
        "Ircv3MessageIdRuntimeProvider.java",
        "Ircv3MessageIdTagPolicy",
        List.of("ircafe-feature-ircv3-message-tags"),
        false);
  }

  @Test
  void builtInIrcv3MutationProvidersOwnRuntimeRendering() throws IOException {
    Path backendProject = Path.of("ircafe-builtins-backend");
    String backendBuild = Files.readString(backendProject.resolve("build.gradle"));
    assertTrue(
        !backendBuild.contains("ircafe-feature-ircv3-"),
        "the backend metadata provider should not compile against IRCv3 runtime features");
    assertTrue(
        !Files.exists(
            backendProject.resolve(
                "src/main/java/cafe/woden/ircclient/app/outbound/backend/"
                    + "Ircv3MessageMutationCommandBuilders.java")),
        "the static backend mutation-builder adapter should stay removed");

    assertRuntimeMutationProvider(
        "ircafe-builtins-ircv3-reply",
        "ircafe-feature-ircv3-reply",
        "Ircv3ReplyExtensionProvider.java",
        "Ircv3ReplyCommandBuilder");
    assertRuntimeMutationProvider(
        "ircafe-builtins-ircv3-reactions",
        "ircafe-feature-ircv3-reactions",
        "Ircv3ReactionsExtensionProvider.java",
        "Ircv3ReactionCommandBuilder");
    assertRuntimeMutationProvider(
        "ircafe-builtins-ircv3-message-edit",
        "ircafe-feature-ircv3-message-edit",
        "Ircv3MessageEditExtensionProvider.java",
        "Ircv3MessageEditCommandBuilder");
    assertRuntimeMutationProvider(
        "ircafe-builtins-ircv3-message-redaction",
        "ircafe-feature-ircv3-message-redaction",
        "Ircv3MessageRedactionExtensionProvider.java",
        "Ircv3MessageRedactionCommandBuilder");
  }

  @Test
  void builtInIrcv3OutboundCommandProvidersOwnRuntimeRendering() throws IOException {
    assertRuntimeOutboundProvider(
        "ircafe-builtins-ircv3-typing",
        "ircafe-feature-ircv3-typing",
        "Ircv3TypingExtensionProvider.java",
        "Ircv3TypingCommandBuilder",
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeOutboundProvider(
        "ircafe-builtins-ircv3-read-marker",
        "ircafe-feature-ircv3-read-marker",
        "Ircv3ReadMarkerExtensionProvider.java",
        "Ircv3ReadMarkerCommandBuilder",
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeOutboundProvider(
        "ircafe-builtins-ircv3-chat-history",
        "ircafe-feature-ircv3-chat-history",
        "Ircv3ChatHistoryExtensionProvider.java",
        "Ircv3ChatHistoryCommandBuilder",
        List.of());
    assertRuntimeOutboundProvider(
        "ircafe-builtins-ircv3-multiline",
        "ircafe-feature-ircv3-multiline",
        "Ircv3MultilineExtensionProvider.java",
        "Ircv3MultilineCommandPlanner",
        List.of("ircafe-feature-ircv3-common"));
    assertRuntimeOutboundProvider(
        "ircafe-builtins-ircv3-znc-playback",
        "ircafe-feature-ircv3-znc-playback",
        "Ircv3ZncPlaybackExtensionProvider.java",
        "Ircv3ZncPlaybackRequestPlanner",
        List.of());
    assertRuntimeOutboundProvider(
        "ircafe-builtins-ircv3-labeled-response",
        "ircafe-feature-ircv3-labeled-response",
        "Ircv3LabeledResponseExtensionProvider.java",
        "Ircv3LabeledResponseRawLinePreparer",
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeOutboundProvider(
        "ircafe-builtins-ircv3-monitor",
        "ircafe-feature-ircv3-monitor",
        "Ircv3MonitorExtensionProvider.java",
        "Ircv3MonitorCommandPlanner",
        List.of("ircafe-feature-ircv3-common"));
  }

  @Test
  void builtInIrcv3StsProviderOwnsRuntimeLearningPolicy() throws IOException {
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-sts",
        "ircafe-feature-ircv3-sts",
        "Ircv3StsExtensionProvider.java",
        List.of("Ircv3StsPolicyParser", "Ircv3StsPolicyLearningPlanner"),
        List.of());
  }

  @Test
  void builtInIrcv3InboundTagProvidersOwnRuntimeInterpretation() throws IOException {
    assertRuntimeInboundTagProvider(
        "ircafe-builtins-ircv3-channel-context",
        "ircafe-feature-ircv3-channel-context",
        "Ircv3ChannelContextExtensionProvider.java",
        "Ircv3ChannelContextPolicy",
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeInboundTagProvider(
        "ircafe-builtins-ircv3-reply",
        "ircafe-feature-ircv3-reply",
        "Ircv3ReplyExtensionProvider.java",
        "Ircv3ReplyTagSignal",
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeInboundTagProvider(
        "ircafe-builtins-ircv3-reactions",
        "ircafe-feature-ircv3-reactions",
        "Ircv3ReactionsExtensionProvider.java",
        "Ircv3ReactionTagSignal",
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeInboundTagProvider(
        "ircafe-builtins-ircv3-message-redaction",
        "ircafe-feature-ircv3-message-redaction",
        "Ircv3MessageRedactionExtensionProvider.java",
        "Ircv3MessageRedactionTagSignal",
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeInboundTagProvider(
        "ircafe-builtins-ircv3-typing",
        "ircafe-feature-ircv3-typing",
        "Ircv3TypingExtensionProvider.java",
        "Ircv3TypingTagSignal",
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeInboundTagProvider(
        "ircafe-builtins-ircv3-read-marker",
        "ircafe-feature-ircv3-read-marker",
        "Ircv3ReadMarkerExtensionProvider.java",
        "Ircv3ReadMarkerTagSignal",
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeInboundTagProvider(
        "ircafe-builtins-ircv3-message-edit",
        "ircafe-feature-ircv3-message-edit",
        "Ircv3MessageEditExtensionProvider.java",
        "Ircv3MessageEditTagSignal",
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeInboundTagProvider(
        "ircafe-builtins-ircv3-server-time",
        "ircafe-feature-ircv3-server-time",
        "Ircv3ServerTimeExtensionProvider.java",
        "Ircv3ServerTime",
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeInboundTagProvider(
        "ircafe-builtins-ircv3-account-tag",
        "ircafe-feature-ircv3-account-tag",
        "Ircv3AccountTagExtensionProvider.java",
        "Ircv3AccountTagSignal",
        List.of());
    assertRuntimeInboundTagProvider(
        "ircafe-builtins-ircv3-echo-message",
        "ircafe-feature-ircv3-echo-message",
        "Ircv3EchoMessageExtensionProvider.java",
        "Ircv3EchoMessageTargetHintPlanner",
        List.of(
            "ircafe-feature-ircv3-common",
            "ircafe-feature-ircv3-channel-context",
            "ircafe-feature-ircv3-message-tags"));
    assertRuntimeInboundTagProvider(
        "ircafe-builtins-ircv3-batch",
        "ircafe-feature-ircv3-message-tags",
        "Ircv3BatchExtensionProvider.java",
        "Ircv3BatchTag",
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-batch"));
    assertRuntimeInboundTagProvider(
        "ircafe-builtins-ircv3-labeled-response",
        "ircafe-feature-ircv3-labeled-response",
        "Ircv3LabeledResponseExtensionProvider.java",
        "Ircv3LabeledResponseTagSignal",
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
  }

  @Test
  void builtInIrcv3InboundCommandProvidersOwnRuntimeInterpretation() throws IOException {
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-away-notify",
        "ircafe-feature-ircv3-away-notify",
        "Ircv3AwayNotifyExtensionProvider.java",
        List.of("Ircv3AwayLineParser", "Ircv3AwayNotifySignalParser"),
        List.of());
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-account-notify",
        "ircafe-feature-ircv3-account-notify",
        "Ircv3AccountNotifyExtensionProvider.java",
        List.of("Ircv3AccountNotifySignalParser"),
        List.of());
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-extended-join",
        "ircafe-feature-ircv3-extended-join",
        "Ircv3ExtendedJoinExtensionProvider.java",
        List.of("Ircv3ExtendedJoinSignalParser"),
        List.of());
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-chghost",
        "ircafe-feature-ircv3-chghost",
        "Ircv3ChghostExtensionProvider.java",
        List.of("Ircv3ChghostParser"),
        List.of());
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-setname",
        "ircafe-feature-ircv3-setname",
        "Ircv3SetnameExtensionProvider.java",
        List.of("Ircv3SetnameParser"),
        List.of());
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-invite-notify",
        "ircafe-feature-ircv3-invite-notify",
        "Ircv3InviteNotifyExtensionProvider.java",
        List.of("Ircv3InviteNotifyParser"),
        List.of());
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-standard-replies",
        "ircafe-feature-ircv3-standard-replies",
        "Ircv3StandardRepliesExtensionProvider.java",
        List.of("Ircv3StandardReplyParser"),
        List.of());
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-monitor",
        "ircafe-feature-ircv3-monitor",
        "Ircv3MonitorExtensionProvider.java",
        List.of("Ircv3MonitorParser"),
        List.of("ircafe-feature-ircv3-common"));
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-user-identity",
        "ircafe-feature-ircv3-user-identity",
        "Ircv3UserIdentityRuntimeProvider.java",
        List.of("Ircv3WhoUserhostParser", "Ircv3WhoisParser"),
        List.of("ircafe-feature-ircv3-common"),
        false);
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-batch",
        "ircafe-feature-ircv3-batch",
        "Ircv3BatchExtensionProvider.java",
        List.of("Ircv3HistoryBatchControlParser"),
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-znc-playback",
        "ircafe-feature-ircv3-znc-playback",
        "Ircv3ZncPlaybackExtensionProvider.java",
        List.of("Ircv3ZncDetector"),
        List.of());
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-multiline",
        "ircafe-feature-ircv3-multiline",
        "Ircv3MultilineExtensionProvider.java",
        List.of("Ircv3MultilineCapabilityStatePlanner"),
        List.of("ircafe-feature-ircv3-common"));
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-read-marker",
        "ircafe-feature-ircv3-read-marker",
        "Ircv3ReadMarkerExtensionProvider.java",
        List.of("Ircv3ReadMarkerCommandSignal"),
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-message-redaction",
        "ircafe-feature-ircv3-message-redaction",
        "Ircv3MessageRedactionExtensionProvider.java",
        List.of("Ircv3MessageRedactionCommandSignal"),
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-negotiation",
        "ircafe-feature-ircv3-negotiation",
        "Ircv3NegotiationRuntimeProvider.java",
        List.of(
            "Ircv3IsupportLine", "Ircv3CapabilityChangePlanner", "Ircv3CapabilityFallbackPlanner"),
        List.of("ircafe-feature-ircv3-common"),
        false);
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-typing",
        "ircafe-feature-ircv3-typing",
        "Ircv3TypingExtensionProvider.java",
        List.of("Ircv3TypingClientTagPolicy"),
        List.of("ircafe-feature-ircv3-common", "ircafe-feature-ircv3-message-tags"));
    assertRuntimeInboundCommandProvider(
        "ircafe-builtins-ircv3-sasl",
        "ircafe-feature-ircv3-sasl",
        "Ircv3SaslRuntimeProvider.java",
        List.of("Ircv3SaslCapabilityOffer", "Ircv3SaslIrcLine", "Ircv3SaslFailureSignal"),
        List.of(),
        false);
  }

  @Test
  void ircv3CapabilityMetadataUsesFocusedBuiltInProviderProjects() throws IOException {
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-message-tags", "Ircv3MessageTagsExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-server-time", "Ircv3ServerTimeExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-echo-message", "Ircv3EchoMessageExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-standard-replies", "Ircv3StandardRepliesExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-account-tag", "Ircv3AccountTagExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-labeled-response", "Ircv3LabeledResponseExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-away-notify", "Ircv3AwayNotifyExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-account-notify", "Ircv3AccountNotifyExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-extended-join", "Ircv3ExtendedJoinExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-chghost", "Ircv3ChghostExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-setname", "Ircv3SetnameExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-invite-notify", "Ircv3InviteNotifyExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-monitor", "Ircv3MonitorExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-extended-monitor", "Ircv3ExtendedMonitorExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-multi-prefix", "Ircv3MultiPrefixExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-userhost-in-names", "Ircv3UserhostInNamesExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-cap-notify", "Ircv3CapNotifyExtensionProvider.java");
    assertFocusedIrcv3Provider("ircafe-builtins-ircv3-batch", "Ircv3BatchExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-znc-playback", "Ircv3ZncPlaybackExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-chat-history", "Ircv3ChatHistoryExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-multiline", "Ircv3MultilineExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-read-marker", "Ircv3ReadMarkerExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-message-redaction", "Ircv3MessageRedactionExtensionProvider.java");
    assertFocusedIrcv3Provider("ircafe-builtins-ircv3-sts", "Ircv3StsExtensionProvider.java");
    assertFocusedIrcv3Provider("ircafe-builtins-ircv3-reply", "Ircv3ReplyExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-reactions", "Ircv3ReactionsExtensionProvider.java");
    assertFocusedIrcv3Provider("ircafe-builtins-ircv3-typing", "Ircv3TypingExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-channel-context", "Ircv3ChannelContextExtensionProvider.java");
    assertFocusedIrcv3Provider(
        "ircafe-builtins-ircv3-message-edit", "Ircv3MessageEditExtensionProvider.java");

    String settings = Files.readString(Path.of("settings.gradle"));
    String build = Files.readString(Path.of("build.gradle"));
    Path retiredProject = Path.of("ircafe-builtins-ircv3");

    assertTrue(
        !builtInProviderProjectNames(settings).contains("ircafe-builtins-ircv3"),
        "the shared IRCv3 metadata aggregate should remain excluded from settings.gradle");
    assertTrue(
        !Files.isRegularFile(retiredProject.resolve("build.gradle")),
        "the shared IRCv3 metadata aggregate should not retain a Gradle project");
    assertTrue(
        !build.contains("project(':ircafe-builtins-ircv3')"),
        "the app should not retain dependencies on the retired IRCv3 metadata aggregate");

    Path retiredPresenceProject = Path.of("ircafe-builtins-ircv3-presence");
    assertTrue(
        !builtInProviderProjectNames(settings).contains("ircafe-builtins-ircv3-presence"),
        "the presence metadata/runtime aggregate should be excluded from settings.gradle");
    assertTrue(
        !Files.isRegularFile(retiredPresenceProject.resolve("build.gradle")),
        "the presence metadata/runtime aggregate should not retain a Gradle project");
    assertTrue(
        !build.contains("project(':ircafe-builtins-ircv3-presence')"),
        "the app should not retain dependencies on the retired presence provider");

    Path retiredCorrelationProject = Path.of("ircafe-builtins-ircv3-message-correlation");
    assertTrue(
        !builtInProviderProjectNames(settings)
            .contains("ircafe-builtins-ircv3-message-correlation"),
        "the message-correlation metadata aggregate should remain excluded from settings.gradle");
    assertTrue(
        !Files.isRegularFile(retiredCorrelationProject.resolve("build.gradle")),
        "the message-correlation metadata aggregate should not retain a Gradle project");
    assertTrue(
        !build.contains("project(':ircafe-builtins-ircv3-message-correlation')"),
        "the app should not retain dependencies on the retired message-correlation provider");

    Path retiredNamesProject = Path.of("ircafe-builtins-ircv3-names");
    assertTrue(
        !builtInProviderProjectNames(settings).contains("ircafe-builtins-ircv3-names"),
        "the aggregate names metadata/runtime provider should be excluded from settings.gradle");
    assertTrue(
        !Files.isRegularFile(retiredNamesProject.resolve("build.gradle")),
        "the aggregate names metadata/runtime provider should not retain a Gradle project");
    assertTrue(
        !build.contains("project(':ircafe-builtins-ircv3-names')"),
        "the app should not retain dependencies on the retired names provider");
  }

  private static void assertRuntimeMessageTagParserProvider(
      String projectName, String featureProject, String providerFile) throws IOException {
    Path project = Path.of(projectName);
    String build = Files.readString(project.resolve("build.gradle"));
    String provider =
        Files.readString(
            project.resolve("src/main/java/cafe/woden/ircclient/irc/ircv3").resolve(providerFile));

    assertTrue(
        build.contains("implementation project(':" + featureProject + "')"),
        projectName + " should package the focused message-tag parser feature");
    assertTrue(
        provider.contains("Ircv3MessageTagParserProvider")
            && !provider.contains("Ircv3InboundTagSignalProvider")
            && !provider.contains("Ircv3InboundTagOperation.MESSAGE_ID")
            && provider.contains("Ircv3Tags.from")
            && provider.contains("@AutoService({"),
        providerFile + " should expose only metadata and message-tag parser contracts");
  }

  private static void assertRuntimeMutationProvider(
      String projectName, String featureProject, String providerFile, String builderClass)
      throws IOException {
    Path project = Path.of(projectName);
    String build = Files.readString(project.resolve("build.gradle"));
    String provider =
        Files.readString(
            project.resolve("src/main/java/cafe/woden/ircclient/irc/ircv3").resolve(providerFile));

    assertTrue(
        build.contains("implementation project(':" + featureProject + "')"),
        projectName + " should package its focused runtime feature");
    assertTrue(
        provider.contains("Ircv3MessageMutationProvider")
            && provider.contains(builderClass)
            && provider.contains("@AutoService({"),
        providerFile + " should expose both metadata and runtime ServiceLoader contracts");
  }

  private static void assertRuntimeOutboundProvider(
      String projectName,
      String featureProject,
      String providerFile,
      String builderClass,
      List<String> transitiveFeatureProjects)
      throws IOException {
    Path project = Path.of(projectName);
    String build = Files.readString(project.resolve("build.gradle"));
    String provider =
        Files.readString(
            project.resolve("src/main/java/cafe/woden/ircclient/irc/ircv3").resolve(providerFile));

    assertTrue(
        build.contains("implementation project(':" + featureProject + "')"),
        projectName + " should package its focused runtime feature");
    assertTrue(
        provider.contains("Ircv3OutboundCommandProvider")
            && provider.contains(builderClass)
            && provider.contains("@AutoService({"),
        providerFile + " should expose metadata and outbound runtime ServiceLoader contracts");
  }

  private static void assertRuntimeInboundTagProvider(
      String projectName,
      String featureProject,
      String providerFile,
      String signalPolicyClass,
      List<String> transitiveFeatureProjects)
      throws IOException {
    assertRuntimeInboundTagProvider(
        projectName,
        featureProject,
        providerFile,
        signalPolicyClass,
        transitiveFeatureProjects,
        true);
  }

  private static void assertRuntimeInboundTagProvider(
      String projectName,
      String featureProject,
      String providerFile,
      String signalPolicyClass,
      List<String> transitiveFeatureProjects,
      boolean publishesCapabilityMetadata)
      throws IOException {
    Path project = Path.of(projectName);
    String build = Files.readString(project.resolve("build.gradle"));
    String provider =
        Files.readString(
            project.resolve("src/main/java/cafe/woden/ircclient/irc/ircv3").resolve(providerFile));

    assertTrue(
        build.contains("implementation project(':" + featureProject + "')"),
        projectName + " should package its focused inbound tag feature");
    assertTrue(
        provider.contains("Ircv3InboundTagSignalProvider") && provider.contains(signalPolicyClass),
        providerFile + " should expose the inbound tag runtime ServiceLoader contract");
    if (publishesCapabilityMetadata) {
      assertTrue(
          provider.contains("Ircv3ExtensionProvider") && provider.contains("@AutoService({"),
          providerFile + " should also expose the capability metadata ServiceLoader contract");
    } else {
      assertTrue(
          !provider.contains("Ircv3ExtensionProvider")
              && provider.contains("@AutoService(Ircv3InboundTagSignalProvider.class)"),
          providerFile + " should remain a runtime-only provider");
    }
  }

  private static void assertRuntimeInboundCommandProvider(
      String projectName,
      String featureProject,
      String providerFile,
      List<String> signalPolicyClasses,
      List<String> transitiveFeatureProjects)
      throws IOException {
    assertRuntimeInboundCommandProvider(
        projectName,
        featureProject,
        providerFile,
        signalPolicyClasses,
        transitiveFeatureProjects,
        true);
  }

  private static void assertRuntimeInboundCommandProvider(
      String projectName,
      String featureProject,
      String providerFile,
      List<String> signalPolicyClasses,
      List<String> transitiveFeatureProjects,
      boolean publishesCapabilityMetadata)
      throws IOException {
    Path project = Path.of(projectName);
    String build = Files.readString(project.resolve("build.gradle"));
    String provider =
        Files.readString(
            project.resolve("src/main/java/cafe/woden/ircclient/irc/ircv3").resolve(providerFile));

    assertTrue(
        build.contains("implementation project(':" + featureProject + "')"),
        projectName + " should package its focused inbound command feature");
    assertTrue(
        provider.contains("Ircv3InboundCommandSignalProvider"),
        providerFile + " should expose the inbound command runtime ServiceLoader contract");
    if (publishesCapabilityMetadata) {
      assertTrue(
          provider.contains("Ircv3ExtensionProvider") && provider.contains("@AutoService({"),
          providerFile + " should also expose the capability metadata ServiceLoader contract");
    } else {
      assertTrue(
          provider.contains("@AutoService(Ircv3InboundCommandSignalProvider.class)"),
          providerFile + " should remain runtime-only and avoid automatic capability requests");
    }
    for (String policyClass : signalPolicyClasses) {
      assertTrue(
          provider.contains(policyClass),
          providerFile + " should delegate inbound command interpretation to " + policyClass);
    }
  }

  private static void assertFocusedIrcv3Provider(String projectName, String providerFile) {
    Path provider =
        Path.of(projectName, "src/main/java/cafe/woden/ircclient/irc/ircv3", providerFile);
    assertTrue(Files.isRegularFile(provider), provider + " should remain capability-owned");
  }

  private static void assertBuiltInProviderJarIncluded(String build, String projectName) {
    assertTrue(
        build.contains("implementation project(':" + projectName + "')")
            || build.contains("runtimeOnly project(':" + projectName + "')"),
        "the app should include the "
            + projectName
            + " provider jar on the runtime classpath so bootJar packages it");
  }

  private static Set<String> builtInProviderProjectNames(String settings) {
    Set<String> projectNames = new TreeSet<>();
    Matcher matcher = BUILT_IN_PROVIDER_INCLUDE_PATTERN.matcher(settings);
    while (matcher.find()) {
      projectNames.add(matcher.group(1));
    }
    assertTrue(
        !projectNames.isEmpty(), "settings.gradle should declare built-in provider projects");
    return projectNames;
  }

  private static void assertServiceLoaderOnlyProvider(String build, String projectName) {
    assertTrue(
        !build.contains("implementation project(':" + projectName + "')"),
        projectName + " should not be on the app compile classpath");
    assertTrue(
        build.contains("runtimeOnly project(':" + projectName + "')"),
        projectName + " should be loaded from the app runtime classpath");
    assertTrue(
        build.contains("testImplementation project(':" + projectName + "')"),
        projectName + " should remain visible to focused tests that assert built-in behavior");
  }

  private static Set<Path> builtInProviderSourceRoots() throws IOException {
    Set<Path> sourceRoots = new TreeSet<>();
    for (Path path : builtInProviderProjectDirs()) {
      Path sourceRoot = path.resolve("src/main/java");
      if (Files.isDirectory(sourceRoot)) {
        sourceRoots.add(sourceRoot);
      }
    }
    return sourceRoots;
  }

  private static Set<Path> builtInProviderProjectDirs() throws IOException {
    Set<Path> projectDirs = new TreeSet<>();
    String settings = Files.readString(Path.of("settings.gradle"));
    for (String projectName : builtInProviderProjectNames(settings)) {
      Path projectDir = Path.of(projectName);
      assertTrue(
          Files.isDirectory(projectDir),
          "settings.gradle includes " + projectName + " but its project directory is missing");
      projectDirs.add(projectDir);
    }
    return projectDirs;
  }

  private static boolean isBuiltInProviderDependency(Path sourceRoot, String dependency) {
    if (dependency.startsWith("java.")
        || dependency.equals("com.google.auto.service.AutoService")
        || (dependency.startsWith("cafe.woden.ircclient.") && dependency.contains(".spi."))) {
      return true;
    }
    return isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-message-tags",
            "cafe.woden.ircclient.irc.ircv3.Ircv3Tags")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-message-id",
            "cafe.woden.ircclient.irc.ircv3.Ircv3MessageIdTagPolicy")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-reply",
            "cafe.woden.ircclient.irc.ircv3.Ircv3ReplyCommandBuilder")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-reactions",
            "cafe.woden.ircclient.irc.ircv3.Ircv3ReactionCommandBuilder")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-message-edit",
            "cafe.woden.ircclient.irc.ircv3.Ircv3MessageEditCommandBuilder")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-message-redaction",
            "cafe.woden.ircclient.irc.ircv3.Ircv3MessageRedactionCommandBuilder")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-typing",
            "cafe.woden.ircclient.irc.ircv3.Ircv3TypingCommandBuilder")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-read-marker",
            "cafe.woden.ircclient.irc.ircv3.Ircv3ReadMarkerCommandBuilder")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-chat-history",
            "cafe.woden.ircclient.irc.ircv3.Ircv3ChatHistoryCommandBuilder")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-multiline",
            "cafe.woden.ircclient.irc.ircv3.Ircv3MultilineCommandPlanner")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-away-notify",
            "cafe.woden.ircclient.irc.ircv3.Ircv3AwayLineParser")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-away-notify",
            "cafe.woden.ircclient.irc.ircv3.Ircv3AwayNotifySignalParser")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-account-notify",
            "cafe.woden.ircclient.irc.ircv3.Ircv3AccountNotifySignalParser")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-extended-join",
            "cafe.woden.ircclient.irc.ircv3.Ircv3ExtendedJoinSignalParser")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-chghost",
            "cafe.woden.ircclient.irc.ircv3.Ircv3ChghostParser")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-setname",
            "cafe.woden.ircclient.irc.ircv3.Ircv3SetnameParser")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-invite-notify",
            "cafe.woden.ircclient.irc.ircv3.Ircv3InviteNotifyParser")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-account-tag",
            "cafe.woden.ircclient.irc.ircv3.Ircv3AccountTagSignal")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-monitor",
            "cafe.woden.ircclient.irc.ircv3.Ircv3MonitorParser")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-monitor",
            "cafe.woden.ircclient.irc.ircv3.Ircv3MonitorCommandPlanner")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-echo-message",
            "cafe.woden.ircclient.irc.ircv3.Ircv3EchoMessageTargetHintPlanner")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-batch",
            "cafe.woden.ircclient.irc.ircv3.Ircv3HistoryBatchControlParser")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-znc-playback",
            "cafe.woden.ircclient.irc.ircv3.Ircv3ZncPlaybackRequestPlanner")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-batch",
            "cafe.woden.ircclient.irc.ircv3.Ircv3BatchTag")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-znc-playback",
            "cafe.woden.ircclient.irc.ircv3.Ircv3ZncDetector")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-znc-playback",
            "cafe.woden.ircclient.irc.ircv3.Ircv3HistoryBootstrapSuppressionPolicy")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-sts",
            "cafe.woden.ircclient.irc.ircv3.Ircv3StsPolicy")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-sts",
            "cafe.woden.ircclient.irc.ircv3.Ircv3StsPolicyLearningPlanner")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-sts",
            "cafe.woden.ircclient.irc.ircv3.Ircv3StsPolicyParser")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-sasl",
            "cafe.woden.ircclient.irc.ircv3.Ircv3SaslCapabilityOffer")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-sasl",
            "cafe.woden.ircclient.irc.ircv3.Ircv3SaslIrcLine")
        || isPairedIrcv3RuntimeDependency(
            sourceRoot,
            dependency,
            "ircafe-builtins-ircv3-sasl",
            "cafe.woden.ircclient.irc.ircv3.Ircv3SaslFailureSignal");
  }

  private static boolean isPairedIrcv3RuntimeDependency(
      Path sourceRoot, String dependency, String projectName, String allowedDependency) {
    return sourceRoot.startsWith(Path.of(projectName, "src", "main", "java"))
        && dependency.equals(allowedDependency);
  }
}

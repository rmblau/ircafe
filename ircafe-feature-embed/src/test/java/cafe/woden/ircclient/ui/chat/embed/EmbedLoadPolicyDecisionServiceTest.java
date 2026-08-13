package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class EmbedLoadPolicyDecisionServiceTest {

  private final EmbedLoadPolicyDecisionService service = new EmbedLoadPolicyDecisionService();

  @Test
  void validatePatternSyntaxAcceptsGlobRegexAndUserPrefixes() {
    assertTrue(service.validatePatternSyntax("*.example.org").isEmpty());
    assertTrue(service.validatePatternSyntax("glob:*.example.org").isEmpty());
    assertTrue(service.validatePatternSyntax("re:^https://example").isEmpty());
    assertTrue(service.validatePatternSyntax("nick:re:^alice$").isEmpty());
    assertTrue(service.validatePatternSyntax("host:*.trusted.net").isEmpty());
  }

  @Test
  void validatePatternSyntaxRejectsBadRegex() {
    assertTrue(service.validatePatternSyntax("re:[unterminated").isPresent());
    assertTrue(service.validatePatternSyntax("nick:re:(").isPresent());
    assertTrue(service.validatePatternSyntax("host:regex:").isPresent());
  }

  @Test
  void blocksBlacklistedDomainsAndAllowsOthers() {
    EmbedLoadPolicyDecisionScope scope =
        new EmbedLoadPolicyDecisionScope(
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            false,
            false,
            0,
            List.of(),
            List.of(),
            List.of(),
            List.of("*.evil.example"));
    EmbedLoadPolicySenderFacts sender =
        new EmbedLoadPolicySenderFacts("alice", "alice!ident@a.trusted.net", true, true, 30);

    assertTrue(service.allow(scope, "#chat", sender, "https://safe.example/image.png"));
    assertFalse(service.allow(scope, "#chat", sender, "https://cdn.evil.example/image.png"));
  }

  @Test
  void enforcesUserChannelAndLinkRules() {
    EmbedLoadPolicyDecisionScope scope =
        new EmbedLoadPolicyDecisionScope(
            List.of("host:*.trusted.net"),
            List.of("nick:mallory"),
            List.of("#chat"),
            List.of("#bad"),
            false,
            false,
            0,
            List.of("https://*.example.com/*"),
            List.of("*tracker*"),
            List.of(),
            List.of());

    EmbedLoadPolicySenderFacts alice =
        new EmbedLoadPolicySenderFacts("alice", "alice!ident@a.trusted.net", true, true, 30);
    EmbedLoadPolicySenderFacts mallory =
        new EmbedLoadPolicySenderFacts("mallory", "mallory!ident@a.trusted.net", true, true, 30);
    EmbedLoadPolicySenderFacts bob =
        new EmbedLoadPolicySenderFacts("bob", "bob!ident@elsewhere.net", true, true, 30);

    assertTrue(service.allow(scope, "#chat", alice, "https://cdn.example.com/image.png"));
    assertFalse(service.allow(scope, "#chat", mallory, "https://cdn.example.com/image.png"));
    assertFalse(service.allow(scope, "#chat", bob, "https://cdn.example.com/image.png"));
    assertFalse(service.allow(scope, "#bad", alice, "https://cdn.example.com/image.png"));
    assertFalse(service.allow(scope, "#chat", alice, "https://tracker.example.com/image.png"));
  }

  @Test
  void enforcesVoiceOrOpLoggedInAndAccountAge() {
    EmbedLoadPolicyDecisionScope scope =
        new EmbedLoadPolicyDecisionScope(
            List.of(), List.of(), List.of(), List.of(), true, true, 14, List.of(), List.of(),
            List.of(), List.of());

    assertTrue(
        service.allow(
            scope,
            "#chat",
            new EmbedLoadPolicySenderFacts("alice", "alice!ident@trusted.net", true, true, 30),
            "https://example.com/page"));
    assertFalse(
        service.allow(
            scope,
            "#chat",
            new EmbedLoadPolicySenderFacts("bob", "bob!ident@trusted.net", true, false, 30),
            "https://example.com/page"));
    assertFalse(
        service.allow(
            scope,
            "#chat",
            new EmbedLoadPolicySenderFacts("carol", "carol!ident@trusted.net", false, true, 30),
            "https://example.com/page"));
    assertFalse(
        service.allow(
            scope,
            "#chat",
            new EmbedLoadPolicySenderFacts("dave", "dave!ident@trusted.net", true, true, 7),
            "https://example.com/page"));
  }
}

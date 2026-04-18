package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.FilterDirection;
import cafe.woden.ircclient.model.FilterRule;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.RegexFlag;
import cafe.woden.ircclient.model.RegexSpec;
import cafe.woden.ircclient.model.TagSpec;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import cafe.woden.ircclient.ui.filter.FilterSettings;
import cafe.woden.ircclient.ui.filter.FilterSettingsBus;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ChatTranscriptFilterRoutingSupportTest {

  @Test
  void matchForFallsBackToExplicitFromWhenMetaFromIsBlank() {
    ChatTranscriptFilterRoutingSupport support =
        newSupport(
            new FilterSettings(
                true,
                true,
                true,
                3,
                250,
                12,
                10,
                true,
                List.of(
                    new FilterRule(
                        null,
                        "hide-alice",
                        true,
                        "srv/#chan",
                        FilterAction.HIDE,
                        FilterDirection.ANY,
                        EnumSet.of(LogKind.CHAT),
                        List.of("alice"),
                        new RegexSpec("hello", EnumSet.of(RegexFlag.I)),
                        TagSpec.empty())),
                List.of()),
            (ref, preview, meta, match) -> {},
            (ref, insertAt, preview, meta, match) -> insertAt,
            ref -> {},
            ref -> {});

    FilterEngine.Match match =
        support.matchFor(
            new TargetRef("srv", "#chan"),
            new LineMeta(
                "srv/#chan",
                LogKind.CHAT,
                LogDirection.IN,
                "",
                1_000L,
                Set.of(),
                "m-1",
                "msgid=m-1",
                Map.of("msgid", "m-1")),
            "alice",
            "hello there");

    assertNotNull(match);
    assertTrue(match.isHide());
    assertEquals("hide-alice", match.ruleName());
  }

  @Test
  void handleHiddenAppendDelegatesOnlyForHideMatches() {
    AtomicInteger appendCalls = new AtomicInteger();
    AtomicReference<String> previewRef = new AtomicReference<>();
    ChatTranscriptFilterRoutingSupport support =
        newSupport(
            FilterSettings.defaults(),
            (ref, preview, meta, match) -> {
              appendCalls.incrementAndGet();
              previewRef.set(preview);
            },
            (ref, insertAt, preview, meta, match) -> insertAt,
            ref -> {},
            ref -> {});

    boolean hiddenHandled =
        support.handleHiddenAppend(
            new TargetRef("srv", "#chan"),
            "preview",
            lineMeta(),
            new FilterEngine.Match(UUID.randomUUID(), "hide", FilterAction.HIDE));
    boolean highlightHandled =
        support.handleHiddenAppend(
            new TargetRef("srv", "#chan"),
            "ignored",
            lineMeta(),
            new FilterEngine.Match(UUID.randomUUID(), "highlight", FilterAction.HIGHLIGHT));

    assertTrue(hiddenHandled);
    assertFalse(highlightHandled);
    assertEquals(1, appendCalls.get());
    assertEquals("preview", previewRef.get());
  }

  @Test
  void handleHiddenTextAppendUsesChatPreviewFormatting() {
    AtomicReference<String> previewRef = new AtomicReference<>();
    ChatTranscriptFilterRoutingSupport support =
        newSupport(
            FilterSettings.defaults(),
            (ref, preview, meta, match) -> previewRef.set(preview),
            (ref, insertAt, preview, meta, match) -> insertAt,
            ref -> {},
            ref -> {});

    boolean handled =
        support.handleHiddenTextAppend(
            new TargetRef("srv", "#chan"),
            "alice",
            "hello",
            lineMeta(),
            new FilterEngine.Match(UUID.randomUUID(), "hide", FilterAction.HIDE));

    assertTrue(handled);
    assertEquals("alice: hello", previewRef.get());
  }

  @Test
  void handleHiddenActionAppendUsesActionPreviewFormatting() {
    AtomicReference<String> previewRef = new AtomicReference<>();
    ChatTranscriptFilterRoutingSupport support =
        newSupport(
            FilterSettings.defaults(),
            (ref, preview, meta, match) -> previewRef.set(preview),
            (ref, insertAt, preview, meta, match) -> insertAt,
            ref -> {},
            ref -> {});

    boolean handled =
        support.handleHiddenActionAppend(
            new TargetRef("srv", "#chan"),
            "alice",
            "waves",
            lineMeta(),
            new FilterEngine.Match(UUID.randomUUID(), "hide", FilterAction.HIDE));

    assertTrue(handled);
    assertEquals("* alice waves", previewRef.get());
  }

  @Test
  void handleHiddenHistoryInsertUsesPlaceholderWhenEnabled() {
    AtomicInteger insertCalls = new AtomicInteger();
    AtomicInteger endCalls = new AtomicInteger();
    ChatTranscriptFilterRoutingSupport support =
        newSupport(
            FilterSettings.defaults(),
            (ref, preview, meta, match) -> {},
            (ref, insertAt, preview, meta, match) -> {
              insertCalls.incrementAndGet();
              return insertAt + 7;
            },
            ref -> endCalls.incrementAndGet(),
            ref -> {});

    ChatTranscriptFilterRoutingSupport.HistoryDecision decision =
        support.handleHiddenHistoryInsert(
            new TargetRef("srv", "#chan"),
            5,
            "preview",
            lineMeta(),
            new FilterEngine.Match(UUID.randomUUID(), "hide", FilterAction.HIDE));

    assertTrue(decision.handled());
    assertEquals(12, decision.nextInsertAt());
    assertEquals(1, insertCalls.get());
    assertEquals(0, endCalls.get());
  }

  @Test
  void handleHiddenTextHistoryInsertUsesChatPreviewFormatting() {
    AtomicReference<String> previewRef = new AtomicReference<>();
    ChatTranscriptFilterRoutingSupport support =
        newSupport(
            FilterSettings.defaults(),
            (ref, preview, meta, match) -> {},
            (ref, insertAt, preview, meta, match) -> {
              previewRef.set(preview);
              return insertAt + 1;
            },
            ref -> {},
            ref -> {});

    ChatTranscriptFilterRoutingSupport.HistoryDecision decision =
        support.handleHiddenTextHistoryInsert(
            new TargetRef("srv", "#chan"),
            2,
            "alice",
            "hello",
            lineMeta(),
            new FilterEngine.Match(UUID.randomUUID(), "hide", FilterAction.HIDE));

    assertTrue(decision.handled());
    assertEquals("alice: hello", previewRef.get());
  }

  @Test
  void handleHiddenActionHistoryInsertUsesActionPreviewFormatting() {
    AtomicReference<String> previewRef = new AtomicReference<>();
    ChatTranscriptFilterRoutingSupport support =
        newSupport(
            FilterSettings.defaults(),
            (ref, preview, meta, match) -> {},
            (ref, insertAt, preview, meta, match) -> {
              previewRef.set(preview);
              return insertAt + 1;
            },
            ref -> {},
            ref -> {});

    ChatTranscriptFilterRoutingSupport.HistoryDecision decision =
        support.handleHiddenActionHistoryInsert(
            new TargetRef("srv", "#chan"),
            2,
            "alice",
            "waves",
            lineMeta(),
            new FilterEngine.Match(UUID.randomUUID(), "hide", FilterAction.HIDE));

    assertTrue(decision.handled());
    assertEquals("* alice waves", previewRef.get());
  }

  @Test
  void prepareVisibleTextAppendReturnsMetaAndBreaksPresenceRunWhenVisible() {
    AtomicInteger appendCalls = new AtomicInteger();
    AtomicInteger breakCalls = new AtomicInteger();
    ChatTranscriptFilterRoutingSupport support =
        newSupport(
            FilterSettings.defaults(),
            (ref, preview, meta, match) -> appendCalls.incrementAndGet(),
            (ref, insertAt, preview, meta, match) -> insertAt,
            ref -> {},
            ref -> breakCalls.incrementAndGet());

    LineMeta meta =
        support.prepareVisibleTextAppend(
            new TargetRef("srv", "#chan"),
            LogKind.CHAT,
            LogDirection.IN,
            "alice",
            "hello there",
            2_000L,
            "m-1",
            Map.of("msgid", "m-1"));

    assertNotNull(meta);
    assertEquals(LogKind.CHAT, meta.kind());
    assertEquals(LogDirection.IN, meta.direction());
    assertEquals("alice", meta.fromNick());
    assertEquals(2_000L, meta.epochMs());
    assertEquals("m-1", meta.messageIdDisplay());
    assertEquals(0, appendCalls.get());
    assertEquals(1, breakCalls.get());
  }

  @Test
  void prepareVisibleTextAppendDelegatesHiddenLinesWithoutBreakingPresenceRun() {
    AtomicReference<String> previewRef = new AtomicReference<>();
    AtomicInteger breakCalls = new AtomicInteger();
    ChatTranscriptFilterRoutingSupport support =
        newSupport(
            new FilterSettings(
                true,
                true,
                true,
                3,
                250,
                12,
                10,
                true,
                List.of(
                    new FilterRule(
                        null,
                        "hide-alice",
                        true,
                        "srv/#chan",
                        FilterAction.HIDE,
                        FilterDirection.ANY,
                        EnumSet.of(LogKind.CHAT),
                        List.of("alice"),
                        new RegexSpec("hello", EnumSet.of(RegexFlag.I)),
                        TagSpec.empty())),
                List.of()),
            (ref, preview, meta, match) -> previewRef.set(preview),
            (ref, insertAt, preview, meta, match) -> insertAt,
            ref -> {},
            ref -> breakCalls.incrementAndGet());

    LineMeta meta =
        support.prepareVisibleTextAppend(
            new TargetRef("srv", "#chan"),
            LogKind.CHAT,
            LogDirection.IN,
            "alice",
            "hello there",
            2_000L,
            "m-1",
            Map.of("msgid", "m-1"));

    assertNull(meta);
    assertEquals("alice: hello there", previewRef.get());
    assertEquals(0, breakCalls.get());
  }

  @Test
  void prepareVisibleTextAppendWithMatchReturnsVisibleMatchAndBreaksPresenceRun() {
    AtomicInteger appendCalls = new AtomicInteger();
    AtomicInteger breakCalls = new AtomicInteger();
    ChatTranscriptFilterRoutingSupport support =
        newSupport(
            new FilterSettings(
                true,
                true,
                true,
                3,
                250,
                12,
                10,
                true,
                List.of(
                    new FilterRule(
                        null,
                        "highlight-alice",
                        true,
                        "srv/#chan",
                        FilterAction.HIGHLIGHT,
                        FilterDirection.ANY,
                        EnumSet.of(LogKind.SPOILER),
                        List.of("alice"),
                        new RegexSpec("hello", EnumSet.of(RegexFlag.I)),
                        TagSpec.empty())),
                List.of()),
            (ref, preview, meta, match) -> appendCalls.incrementAndGet(),
            (ref, insertAt, preview, meta, match) -> insertAt,
            ref -> {},
            ref -> breakCalls.incrementAndGet());

    ChatTranscriptFilterRoutingSupport.VisibleAppend prepared =
        support.prepareVisibleTextAppendWithMatch(
            new TargetRef("srv", "#chan"),
            LogKind.SPOILER,
            LogDirection.IN,
            "alice",
            "hello there",
            3_000L,
            "",
            Map.of());

    assertNotNull(prepared);
    assertEquals(LogKind.SPOILER, prepared.meta().kind());
    assertNotNull(prepared.match());
    assertEquals(FilterAction.HIGHLIGHT, prepared.match().action());
    assertEquals(0, appendCalls.get());
    assertEquals(1, breakCalls.get());
  }

  @Test
  void prepareVisibleActionAppendReturnsVisibleMatchAndBreaksPresenceRun() {
    AtomicInteger appendCalls = new AtomicInteger();
    AtomicInteger breakCalls = new AtomicInteger();
    ChatTranscriptFilterRoutingSupport support =
        newSupport(
            new FilterSettings(
                true,
                true,
                true,
                3,
                250,
                12,
                10,
                true,
                List.of(
                    new FilterRule(
                        null,
                        "highlight-alice-action",
                        true,
                        "srv/#chan",
                        FilterAction.HIGHLIGHT,
                        FilterDirection.ANY,
                        EnumSet.of(LogKind.ACTION),
                        List.of("alice"),
                        new RegexSpec("wave", EnumSet.of(RegexFlag.I)),
                        TagSpec.empty())),
                List.of()),
            (ref, preview, meta, match) -> appendCalls.incrementAndGet(),
            (ref, insertAt, preview, meta, match) -> insertAt,
            ref -> {},
            ref -> breakCalls.incrementAndGet());

    ChatTranscriptFilterRoutingSupport.VisibleAppend prepared =
        support.prepareVisibleActionAppend(
            new TargetRef("srv", "#chan"),
            LogDirection.IN,
            "alice",
            "waves",
            4_000L,
            "a-1",
            Map.of("msgid", "a-1"));

    assertNotNull(prepared);
    assertEquals(LogKind.ACTION, prepared.meta().kind());
    assertEquals("a-1", prepared.meta().messageIdDisplay());
    assertNotNull(prepared.match());
    assertEquals(FilterAction.HIGHLIGHT, prepared.match().action());
    assertEquals(0, appendCalls.get());
    assertEquals(1, breakCalls.get());
  }

  @Test
  void handleHiddenHistoryInsertDropsAndEndsRunWhenHistoryPlaceholdersAreDisabled() {
    AtomicInteger insertCalls = new AtomicInteger();
    AtomicInteger endCalls = new AtomicInteger();
    ChatTranscriptFilterRoutingSupport support =
        newSupport(
            new FilterSettings(true, true, true, 3, 250, 12, 10, false, List.of(), List.of()),
            (ref, preview, meta, match) -> {},
            (ref, insertAt, preview, meta, match) -> {
              insertCalls.incrementAndGet();
              return insertAt + 1;
            },
            ref -> endCalls.incrementAndGet(),
            ref -> {});

    ChatTranscriptFilterRoutingSupport.HistoryDecision decision =
        support.handleHiddenHistoryInsert(
            new TargetRef("srv", "#chan"),
            9,
            "preview",
            lineMeta(),
            new FilterEngine.Match(UUID.randomUUID(), "hide", FilterAction.HIDE));

    assertTrue(decision.handled());
    assertEquals(9, decision.nextInsertAt());
    assertEquals(0, insertCalls.get());
    assertEquals(1, endCalls.get());
  }

  private static ChatTranscriptFilterRoutingSupport newSupport(
      FilterSettings settings,
      ChatTranscriptFilterRoutingSupport.HiddenAppendHandler appendHandler,
      ChatTranscriptFilterRoutingSupport.HiddenInsertHandler insertHandler,
      ChatTranscriptFilterRoutingSupport.FilteredInsertRunEndHandler endHandler,
      ChatTranscriptFilterRoutingSupport.PresenceRunBreakHandler breakHandler) {
    FilterSettingsBus bus = mock(FilterSettingsBus.class);
    when(bus.get()).thenReturn(settings);
    return new ChatTranscriptFilterRoutingSupport(
        new FilterEngine(bus), appendHandler, insertHandler, endHandler, breakHandler);
  }

  private static LineMeta lineMeta() {
    return new LineMeta(
        "srv/#chan",
        LogKind.CHAT,
        LogDirection.IN,
        "alice",
        1_000L,
        Set.of("tag_alpha"),
        "m-1",
        "msgid=m-1",
        Map.of("msgid", "m-1"));
  }
}

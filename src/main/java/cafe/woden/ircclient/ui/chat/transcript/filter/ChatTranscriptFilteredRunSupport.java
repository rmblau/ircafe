package cafe.woden.ircclient.ui.chat.transcript.filter;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.FilteredLineComponent;
import cafe.woden.ircclient.ui.chat.transcript.LineMeta;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/** Shared metadata and document-update helpers for filtered transcript runs. */
public final class ChatTranscriptFilteredRunSupport {

  public record Context(
      ChatStyles styles, BiFunction<AttributeSet, LineMeta, SimpleAttributeSet> withLineMeta) {
    public Context {
      Objects.requireNonNull(styles, "styles");
      Objects.requireNonNull(withLineMeta, "withLineMeta");
    }
  }

  private ChatTranscriptFilteredRunSupport() {}

  public static LineMeta buildFilteredMeta(
      LineMeta base, long tsEpochMs, boolean hint, Set<String> unionTags) {
    if (base == null) {
      base =
          new LineMeta(
              "", LogKind.STATUS, LogDirection.SYSTEM, null, tsEpochMs, Set.of(), "", "", Map.of());
    }

    LinkedHashSet<String> tags = new LinkedHashSet<>();
    if (unionTags != null && !unionTags.isEmpty()) {
      tags.addAll(unionTags);
    } else {
      tags.addAll(base.tags());
    }

    tags.add("irc_filtered");
    tags.add(hint ? "irc_filtered_hint" : "irc_filtered_placeholder");

    return new LineMeta(
        base.bufferKey(),
        base.kind(),
        base.direction(),
        base.fromNick(),
        tsEpochMs,
        Set.copyOf(tags),
        base.messageId(),
        base.ircv3Tags(),
        base.ircv3TagsMap());
  }

  public static LineMeta buildFilteredOverflowMeta(
      LineMeta base, long tsEpochMs, Set<String> unionTags) {
    if (base == null) {
      base =
          new LineMeta(
              "", LogKind.STATUS, LogDirection.SYSTEM, null, tsEpochMs, Set.of(), "", "", Map.of());
    }

    LinkedHashSet<String> tags = new LinkedHashSet<>();
    if (unionTags != null && !unionTags.isEmpty()) {
      tags.addAll(unionTags);
    } else {
      tags.addAll(base.tags());
    }

    tags.add("irc_filtered");
    tags.add("irc_filtered_overflow");

    return new LineMeta(
        base.bufferKey(),
        base.kind(),
        base.direction(),
        base.fromNick(),
        tsEpochMs,
        Set.copyOf(tags),
        base.messageId(),
        base.ircv3Tags(),
        base.ircv3TagsMap());
  }

  public static void attachFilterMatch(
      SimpleAttributeSet attrs, FilterEngine.Match match, boolean multiple) {
    if (attrs == null) return;

    String action = (match != null && match.action() != null) ? match.action().name() : "HIDE";
    attrs.addAttribute(ChatStyles.ATTR_META_FILTER_ACTION, action);
    attrs.addAttribute(
        ChatStyles.ATTR_META_FILTER_MULTIPLE, multiple ? Boolean.TRUE : Boolean.FALSE);

    if (multiple) {
      attrs.addAttribute(ChatStyles.ATTR_META_FILTER_RULE_NAME, "(multiple)");
      attrs.addAttribute(ChatStyles.ATTR_META_FILTER_RULE_ID, "");
      return;
    }

    if (match != null) {
      attrs.addAttribute(ChatStyles.ATTR_META_FILTER_RULE_NAME, String.valueOf(match.ruleName()));
      attrs.addAttribute(ChatStyles.ATTR_META_FILTER_RULE_ID, String.valueOf(match.ruleId()));
    } else {
      attrs.addAttribute(ChatStyles.ATTR_META_FILTER_RULE_NAME, "(unknown)");
      attrs.addAttribute(ChatStyles.ATTR_META_FILTER_RULE_ID, "");
    }
  }

  public static <C extends FilteredLineComponent> void updateFilteredRunAttributes(
      Context context,
      StyledDocument doc,
      ChatTranscriptFilteredLinesSupport.AbstractFilteredRun<C> run,
      boolean hint) {
    if (context == null || doc == null || run == null || run.pos == null || run.component == null) {
      return;
    }

    LineMeta base = run.lastHiddenMeta;
    if (base == null) return;

    long tsEpochMs =
        (base.epochMs() != null && base.epochMs() > 0)
            ? base.epochMs()
            : System.currentTimeMillis();
    LineMeta meta = buildFilteredMeta(base, tsEpochMs, hint, run.unionTags);
    applyFilteredRunAttributesToDoc(context, doc, run, meta);
  }

  public static void updateFilteredOverflowRunAttributes(
      Context context,
      StyledDocument doc,
      ChatTranscriptFilteredLinesSupport.AbstractFilteredRun<?> run) {
    if (context == null || doc == null || run == null || run.pos == null || run.component == null) {
      return;
    }

    LineMeta base = run.lastHiddenMeta;
    if (base == null) return;

    long tsEpochMs =
        (base.epochMs() != null && base.epochMs() > 0)
            ? base.epochMs()
            : System.currentTimeMillis();
    LineMeta meta = buildFilteredOverflowMeta(base, tsEpochMs, run.unionTags);
    applyFilteredRunAttributesToDoc(context, doc, run, meta);
  }

  static <C extends FilteredLineComponent> void applyFilteredRunAttributesToDoc(
      Context context,
      StyledDocument doc,
      ChatTranscriptFilteredLinesSupport.AbstractFilteredRun<C> run,
      LineMeta meta) {
    if (context == null || doc == null || run == null || meta == null || run.component == null) {
      return;
    }

    SimpleAttributeSet attrs = context.withLineMeta().apply(context.styles().status(), meta);
    attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_STATUS);
    attachFilterMatch(attrs, run.primaryMatch, run.multiple);
    StyleConstants.setComponent(attrs, (java.awt.Component) run.component);

    try {
      int off = run.pos.getOffset();
      doc.setCharacterAttributes(off, 1, attrs, true);
      if (off + 1 < doc.getLength()) {
        SimpleAttributeSet nl = context.withLineMeta().apply(context.styles().timestamp(), meta);
        attachFilterMatch(nl, run.primaryMatch, run.multiple);
        doc.setCharacterAttributes(off + 1, 1, nl, true);
      }
    } catch (Exception ignored) {
    }

    try {
      String ruleLabel;
      if (run.multiple) {
        ruleLabel = "(multiple)";
      } else if (run.primaryMatch != null && run.primaryMatch.ruleName() != null) {
        ruleLabel = String.valueOf(run.primaryMatch.ruleName());
      } else {
        ruleLabel = "(unknown)";
      }
      run.component.setFilterDetails(ruleLabel, run.multiple, run.unionTags);
    } catch (Exception ignored) {
    }
  }
}

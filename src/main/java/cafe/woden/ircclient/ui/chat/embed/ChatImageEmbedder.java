package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.settings.EmbedCardStyle;
import cafe.woden.ircclient.ui.settings.EmbedCardStyleBus;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.util.List;
import java.util.Map;
import javax.swing.text.StyledDocument;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Appends inline image previews to a transcript {@link StyledDocument}. */
@Component
@InterfaceLayer
@Lazy
public class ChatImageEmbedder {

  private final UiSettingsBus uiSettings;
  private final ImageFetchService fetch;
  private final EmbedLoadPolicyMatcher policyMatcher;
  private final EmbedCardStyleBus embedCardStyleBus;
  private final EmbedDocumentApplicationService documentApplication;
  private final EmbedRenderRequestService renderRequestService;
  private volatile List<cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>
      imageUrlExtensionProviders = List.of();

  private final java.util.Map<StyledDocument, DocState> perDocState =
      java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

  public ChatImageEmbedder(
      UiSettingsBus uiSettings,
      ImageFetchService fetch,
      EmbedLoadPolicyMatcher policyMatcher,
      EmbedCardStyleBus embedCardStyleBus,
      EmbedDocumentApplicationService documentApplication,
      EmbedRenderRequestService renderRequestService) {
    this.uiSettings = uiSettings;
    this.fetch = fetch;
    this.policyMatcher = policyMatcher;
    this.embedCardStyleBus = embedCardStyleBus;
    this.documentApplication = documentApplication;
    this.renderRequestService =
        renderRequestService != null ? renderRequestService : new EmbedRenderRequestService();
  }

  @Autowired(required = false)
  void setInstalledPluginsPort(InstalledPluginsPort installedPlugins) {
    imageUrlExtensionProviders =
        ImageUrlExtensionProviders.loadInstalledProviders(installedPlugins);
  }

  private DocState stateFor(StyledDocument doc) {
    return perDocState.computeIfAbsent(doc, d -> new DocState());
  }

  private static final class DocState {
    long nextSeq = 0;
    final GifAnimationCoordinator gifCoordinator = new GifAnimationCoordinator();
  }

  public record AppendResult(int appendedCount, List<String> blockedUrls) {
    static AppendResult empty() {
      return new AppendResult(0, List.of());
    }
  }

  /**
   * Scan the message text for direct image URLs and append a preview block for each.
   *
   * <p>Must be called on the Swing EDT (the caller in IRCafe already runs on EDT).
   */
  public AppendResult appendEmbeds(
      TargetRef ctx,
      StyledDocument doc,
      String messageText,
      String fromNick,
      Map<String, String> ircv3Tags) {
    if (doc == null) return AppendResult.empty();
    String serverId = (ctx != null) ? ctx.serverId() : null;

    DocState st = stateFor(doc);
    EmbedAppendResultAccumulator accumulator =
        EmbedAppendResultAccumulator.startingAt(doc.getLength());
    for (String url : ImageUrlExtractor.extractImageUrls(messageText, imageUrlExtensionProviders)) {
      try {
        accumulator.add(
            insertEmbed(
                ctx,
                doc,
                fromNick,
                ircv3Tags,
                serverId,
                st,
                url,
                false,
                accumulator.nextInsertAt()));
      } catch (Exception ignored) {
        // best-effort
      }
    }
    return toAppendResult(accumulator.finish());
  }

  private static AppendResult toAppendResult(EmbedAppendResult result) {
    return result == null
        ? AppendResult.empty()
        : new AppendResult(result.appendedCount(), result.blockedUrls());
  }

  public boolean insertEmbedForUrlAt(TargetRef ctx, StyledDocument doc, String url, int insertAt) {
    if (doc == null) return false;
    String serverId = (ctx != null) ? ctx.serverId() : null;
    DocState st = stateFor(doc);
    try {
      EmbedApplicationResult result =
          insertEmbed(ctx, doc, "", Map.of(), serverId, st, url, true, Math.max(0, insertAt));
      return result.appended();
    } catch (Exception ignored) {
      return false;
    }
  }

  private EmbedApplicationResult insertEmbed(
      TargetRef ctx,
      StyledDocument doc,
      String fromNick,
      Map<String, String> ircv3Tags,
      String serverId,
      DocState st,
      String rawUrl,
      boolean bypassPolicy,
      int insertAt) {
    if (doc == null) {
      return EmbedApplicationResult.skipped(insertAt);
    }

    long seq = st.nextSeq;
    ImageEmbedRenderRequest request;
    try {
      request =
          renderRequestService.imageRequest(
              serverId, rawUrl, uiSettings.get().imageEmbedsCollapsedByDefault(), seq);
    } catch (IllegalArgumentException ignored) {
      return EmbedApplicationResult.skipped(insertAt);
    }

    if (!bypassPolicy
        && policyMatcher != null
        && !policyMatcher.allow(ctx, fromNick, ircv3Tags, request.url())) {
      return EmbedApplicationResult.blocked(insertAt, request.url());
    }
    st.nextSeq = seq + 1;

    // If it looks like a GIF by URL, proactively hint to stop older GIFs immediately.
    // If the decode later shows it's NOT a GIF, the component will call rejectGifHint(seq).
    if (request.gifUrlHint()) {
      st.gifCoordinator.hintNewGifPlaceholder(request.sequence());
    }

    ChatImageComponent comp =
        new ChatImageComponent(
            request.serverId(),
            request.url(),
            fetch,
            request.collapsedByDefault(),
            uiSettings,
            embedCardStyleBus != null ? embedCardStyleBus.get() : EmbedCardStyle.DEFAULT,
            st.gifCoordinator,
            request.sequence(),
            imageUrlExtensionProviders);

    EmbedDocumentApplicationService.InsertResult result =
        documentApplication.insertComponent(doc, request.url(), comp, insertAt);
    return result.inserted()
        ? EmbedApplicationResult.appended(result.nextInsertAt())
        : EmbedApplicationResult.skipped(insertAt);
  }
}

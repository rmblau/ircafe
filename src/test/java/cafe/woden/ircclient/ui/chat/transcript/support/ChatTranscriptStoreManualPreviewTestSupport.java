package cafe.woden.ircclient.ui.chat.transcript.support;

import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreTestFactory.settingsWithTranscriptCap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.embed.ChatImageEmbedder;
import cafe.woden.ircclient.ui.chat.embed.ChatLinkPreviewEmbedder;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.util.List;

/** Test support for manual-preview transcript fixture setup. */
public final class ChatTranscriptStoreManualPreviewTestSupport {

  public record ManualPreviewFallbackFixture(
      ChatTranscriptStore store,
      ChatImageEmbedder imageEmbeds,
      ChatLinkPreviewEmbedder linkPreviews) {}

  private ChatTranscriptStoreManualPreviewTestSupport() {}

  public static ChatTranscriptStore newStoreWithBlockedImagePreview(String blockedUrl) {
    ChatStyles styles = new ChatStyles(null);
    ChatRichTextRenderer renderer = renderer(styles);
    UiSettingsBus settingsBus = mock(UiSettingsBus.class);
    when(settingsBus.get()).thenReturn(settingsWithTranscriptCap(0));

    ChatImageEmbedder imageEmbeds = mock(ChatImageEmbedder.class);
    ChatLinkPreviewEmbedder linkPreviews = mock(ChatLinkPreviewEmbedder.class);
    when(imageEmbeds.appendEmbeds(any(), any(), anyString(), anyString(), any()))
        .thenReturn(new ChatImageEmbedder.AppendResult(0, List.of(blockedUrl)));
    when(linkPreviews.appendPreviews(any(), any(), anyString(), anyString(), any()))
        .thenReturn(new ChatLinkPreviewEmbedder.AppendResult(0, List.of()));

    return new ChatTranscriptStore(
        styles, renderer, null, null, null, imageEmbeds, linkPreviews, settingsBus, null, null);
  }

  public static ManualPreviewFallbackFixture newManualPreviewFallbackFixture() {
    ChatStyles styles = new ChatStyles(null);
    ChatRichTextRenderer renderer = renderer(styles);
    ChatImageEmbedder imageEmbeds = mock(ChatImageEmbedder.class);
    ChatLinkPreviewEmbedder linkPreviews = mock(ChatLinkPreviewEmbedder.class);
    when(imageEmbeds.insertEmbedForUrlAt(any(), any(), anyString(), anyInt())).thenReturn(false);
    when(linkPreviews.insertPreviewForUrlAt(any(), any(), anyString(), anyInt())).thenReturn(true);
    ChatTranscriptStore store =
        new ChatTranscriptStore(
            styles, renderer, null, null, null, imageEmbeds, linkPreviews, null, null, null);
    return new ManualPreviewFallbackFixture(store, imageEmbeds, linkPreviews);
  }

  public static void verifyManualPreviewFallbackAttempted(ManualPreviewFallbackFixture fixture) {
    verify(fixture.imageEmbeds()).insertEmbedForUrlAt(any(), any(), anyString(), anyInt());
    verify(fixture.linkPreviews()).insertPreviewForUrlAt(any(), any(), anyString(), anyInt());
  }

  private static ChatRichTextRenderer renderer(ChatStyles styles) {
    return new ChatRichTextRenderer(null, null, styles, null);
  }
}

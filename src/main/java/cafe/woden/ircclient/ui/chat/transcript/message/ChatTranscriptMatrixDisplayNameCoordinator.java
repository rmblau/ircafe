package cafe.woden.ircclient.ui.chat.transcript.message;

import cafe.woden.ircclient.irc.roster.UserListPort;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.util.Map;
import java.util.Objects;
import javax.swing.text.StyledDocument;

/** Holds Matrix transcript display-name rendering state and refresh operations. */
public final class ChatTranscriptMatrixDisplayNameCoordinator {

  private final ChatTranscriptMatrixDisplayNameSupport.Context context;
  private final Map<TargetRef, StyledDocument> docs;

  public ChatTranscriptMatrixDisplayNameCoordinator(
      UiSettingsBus uiSettings, UserListPort userListStore, Map<TargetRef, StyledDocument> docs) {
    this.context =
        new ChatTranscriptMatrixDisplayNameSupport.Context(uiSettings, userListStore, docs::get);
    this.docs = Objects.requireNonNull(docs, "docs");
  }

  public String renderTranscriptFrom(TargetRef ref, String fromNick) {
    return ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(context, ref, fromNick);
  }

  public int refreshMatrixDisplayNames(TargetRef ref) {
    return ChatTranscriptMatrixDisplayNameSupport.refreshMatrixDisplayNames(context, ref, "");
  }

  public int refreshMatrixDisplayNamesAcrossServer(String serverId, String matrixUserId) {
    return ChatTranscriptMatrixDisplayNameSupport.refreshMatrixDisplayNamesAcrossServer(
        context, docs, serverId, matrixUserId);
  }
}

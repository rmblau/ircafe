package cafe.woden.ircclient.ui;

import cafe.woden.ircclient.app.api.UiMemoServPort;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.servertree.ServerTreeDockable;
import java.time.Instant;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Swing adapter for MemoServ UI updates. */
final class SwingUiMemoServPort implements UiMemoServPort {

  private static final Logger log = LoggerFactory.getLogger(SwingUiMemoServPort.class);

  private final SwingEdtExecutor edt;
  private final ServerTreeDockable serverTree;
  private final ChatDockable chat;

  SwingUiMemoServPort(SwingEdtExecutor edt, ServerTreeDockable serverTree, ChatDockable chat) {
    this.edt = Objects.requireNonNull(edt, "edt");
    this.serverTree = Objects.requireNonNull(serverTree, "serverTree");
    this.chat = Objects.requireNonNull(chat, "chat");
  }

  @Override
  public void ensureMemoServAvailable(String serverId) {
    edt.run(
        () -> {
          String sid = Objects.toString(serverId, "").trim();
          if (sid.isEmpty()) {
            log.info("[memoserv] Swing UI skipped ensure node because server id is blank");
            return;
          }
          log.info("[memoserv] Swing UI ensuring MemoServ node serverId={}", sid);
          serverTree.ensureNode(TargetRef.memoServ(sid));
        });
  }

  @Override
  public void observeMemoServNotice(String serverId, Instant at, String from, String text) {
    edt.run(
        () -> {
          String sid = Objects.toString(serverId, "").trim();
          if (sid.isEmpty()) {
            log.info(
                "[memoserv] Swing UI dropped notice because server id is blank from={} textLength={}",
                from,
                Objects.toString(text, "").length());
            return;
          }
          log.info(
              "[memoserv] Swing UI forwarding notice serverId={} from={} textLength={} preview={}",
              sid,
              from,
              Objects.toString(text, "").length(),
              preview(text));
          serverTree.ensureNode(TargetRef.memoServ(sid));
          chat.observeMemoServNotice(sid, at, from, text);
        });
  }

  private static String preview(String value) {
    String text = Objects.toString(value, "").replace('\n', ' ').replace('\r', ' ').trim();
    if (text.length() <= 180) return text;
    return text.substring(0, 177) + "...";
  }
}

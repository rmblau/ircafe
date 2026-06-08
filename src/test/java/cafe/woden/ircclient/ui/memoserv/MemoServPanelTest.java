package cafe.woden.ircclient.ui.memoserv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class MemoServPanelTest {

  @Test
  void refreshButtonEmitsMemoServListCommand() throws Exception {
    MemoServPanel panel = onEdtCall(MemoServPanel::new);
    AtomicReference<String> emitted = new AtomicReference<>();

    onEdt(
        () -> {
          panel.setServerId("libera");
          panel.setOnEmitCommand(emitted::set);
          JButton refresh = findByName(panel, JButton.class, "memoserv.refreshButton");
          assertNotNull(refresh);
          refresh.doClick();
        });

    assertEquals("LIST", emitted.get());
  }

  @Test
  void observeMemoServNoticeParsesAndDedupesRowsByMemoId() throws Exception {
    MemoServPanel panel = onEdtCall(MemoServPanel::new);

    onEdt(
        () -> {
          panel.setServerId("libera");
          panel.observeMemoServNotice(
              "libera", Instant.parse("2026-06-07T12:00:00Z"), "MemoServ", "1 from alice: hello");
          panel.observeMemoServNotice(
              "libera",
              Instant.parse("2026-06-07T12:01:00Z"),
              "MemoServ",
              "Memo 1 from alice: updated");
        });

    JTable table = findByName(panel, JTable.class, "memoserv.table");
    assertNotNull(table);
    assertEquals(1, table.getRowCount());
    assertEquals("1", table.getValueAt(0, 2));
    assertEquals("alice", table.getValueAt(0, 3));
    assertEquals("updated", table.getValueAt(0, 5));
  }

  @Test
  void observeMemoServNoticeParsesLiberaListRowsAndSkipsServiceStatusRows() throws Exception {
    MemoServPanel panel = onEdtCall(MemoServPanel::new);

    onEdt(
        () -> {
          panel.setServerId("libera");
          panel.observeMemoServNotice(
              "libera",
              Instant.parse("2026-06-07T19:58:54Z"),
              "server",
              "To read them, type /msg MemoServ READ NEW");
          panel.observeMemoServNotice(
              "libera",
              Instant.parse("2026-06-07T19:58:54Z"),
              "server",
              "You have 15 memos (2 new).");
          panel.observeMemoServNotice(
              "libera",
              Instant.parse("2026-06-07T19:58:54Z"),
              "server",
              "- 1 From: Gladwyn Sent: Jul 05 22:50:07 2021 +0000");
          panel.observeMemoServNotice(
              "libera",
              Instant.parse("2026-06-07T19:58:55Z"),
              "server",
              "- 15 From: wodencafe Sent: Jun 07 16:01:25 2026 +0000 [unread]");
        });

    JTable table = findByName(panel, JTable.class, "memoserv.table");
    assertNotNull(table);
    assertEquals(2, table.getRowCount());
    assertEquals("1", table.getValueAt(0, 2));
    assertEquals("Gladwyn", table.getValueAt(0, 3));
    assertEquals("Sent: Jul 05 22:50:07 2021 +0000", table.getValueAt(0, 5));
    assertEquals("15", table.getValueAt(1, 2));
    assertEquals("wodencafe", table.getValueAt(1, 3));
    assertEquals("Unread", table.getValueAt(1, 4));

    JLabel summary = findByName(panel, JLabel.class, "memoserv.summary");
    assertNotNull(summary);
    assertEquals("MemoServ - libera: 2 row(s). You have 15 memos (2 new).", summary.getText());
  }

  @Test
  void rowsAreScopedByServer() throws Exception {
    MemoServPanel panel = onEdtCall(MemoServPanel::new);

    onEdt(
        () -> {
          panel.setServerId("libera");
          panel.observeMemoServNotice(
              "libera", Instant.parse("2026-06-07T12:00:00Z"), "MemoServ", "1 from alice: one");
          panel.observeMemoServNotice(
              "oftc", Instant.parse("2026-06-07T12:00:01Z"), "MemoServ", "2 from bob: two");
        });

    JTable table = findByName(panel, JTable.class, "memoserv.table");
    assertNotNull(table);
    assertEquals(1, table.getRowCount());
    assertEquals("alice", table.getValueAt(0, 3));

    onEdt(() -> panel.setServerId("oftc"));
    assertEquals(1, table.getRowCount());
    assertEquals("bob", table.getValueAt(0, 3));
  }

  @Test
  void appendOutboundMemoAddsSentRow() throws Exception {
    MemoServPanel panel = onEdtCall(MemoServPanel::new);

    onEdt(
        () -> {
          panel.setServerId("libera");
          panel.appendOutboundMemo("libera", "alice", "hello from ircafe");
        });

    JTable table = findByName(panel, JTable.class, "memoserv.table");
    assertNotNull(table);
    assertEquals(1, table.getRowCount());
    assertEquals("Outbound", table.getValueAt(0, 1));
    assertEquals("alice", table.getValueAt(0, 3));
    assertEquals("Sent", table.getValueAt(0, 4));
    assertEquals("hello from ircafe", table.getValueAt(0, 5));
  }

  @Test
  void rowsAreBoundedPerServer() throws Exception {
    MemoServPanel panel = onEdtCall(MemoServPanel::new);

    onEdt(
        () -> {
          panel.setServerId("libera");
          for (int i = 1; i <= 505; i++) {
            panel.observeMemoServNotice(
                "libera",
                Instant.parse("2026-06-07T12:00:00Z"),
                "MemoServ",
                i + " from alice: memo " + i);
          }
        });

    JTable table = findByName(panel, JTable.class, "memoserv.table");
    assertNotNull(table);
    assertEquals(500, table.getRowCount());
    assertEquals("6", table.getValueAt(0, 2));
  }

  private static void onEdt(Runnable runnable)
      throws InvocationTargetException, InterruptedException {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
      return;
    }
    SwingUtilities.invokeAndWait(runnable);
  }

  private static <T> T onEdtCall(ThrowingSupplier<T> supplier) throws Exception {
    AtomicReference<T> result = new AtomicReference<>();
    AtomicReference<Throwable> failure = new AtomicReference<>();
    onEdt(
        () -> {
          try {
            result.set(supplier.get());
          } catch (Throwable t) {
            failure.set(t);
          }
        });
    if (failure.get() != null) {
      throw new AssertionError(failure.get());
    }
    return result.get();
  }

  private static <T extends Component> T findByName(Container root, Class<T> type, String name) {
    if (root == null || type == null || name == null) return null;
    if (type.isInstance(root) && name.equals(root.getName())) {
      return type.cast(root);
    }
    for (Component child : root.getComponents()) {
      if (type.isInstance(child) && name.equals(child.getName())) {
        return type.cast(child);
      }
      if (child instanceof Container container) {
        T nested = findByName(container, type, name);
        if (nested != null) return nested;
      }
    }
    return null;
  }

  @FunctionalInterface
  private interface ThrowingSupplier<T> {
    T get() throws Exception;
  }
}

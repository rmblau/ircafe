package cafe.woden.ircclient.ui.memoserv;

import cafe.woden.ircclient.ui.icons.SvgIcons;
import cafe.woden.ircclient.ui.localization.UiMessages;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Swing panel for basic MemoServ list/read/send actions. */
public final class MemoServPanel extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(MemoServPanel.class);
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();
  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
  private static final int ACTION_ICON_SIZE = 16;
  private static final Dimension ACTION_BUTTON_SIZE = new Dimension(28, 28);
  private static final int MAX_SERVER_BUCKETS = 64;
  private static final int MAX_ROWS_PER_SERVER = 500;

  private static final Pattern MEMO_NUMBER_FROM_PATTERN =
      Pattern.compile(
          "^\\s*(?:[-*]\\s*)?(\\d+)\\.?\\s+(?:from\\s+)?([^:]+):\\s*(.*)$",
          Pattern.CASE_INSENSITIVE);
  private static final Pattern MEMO_WORD_FROM_PATTERN =
      Pattern.compile(
          "^\\s*memo\\s+(\\d+)\\s+(?:from|by)\\s+([^:]+):?\\s*(.*)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern LIBERA_MEMOSERV_LIST_PATTERN =
      Pattern.compile(
          "^\\s*(?:[-*]\\s*)?(\\d+)\\.?\\s+From:\\s+(\\S+)\\s+Sent:\\s+(.+?)(?:\\s+To:\\s+(\\S+))?(?:\\s+\\[([^\\]]+)\\])?\\s*$",
          Pattern.CASE_INSENSITIVE);
  private static final Pattern MEMO_NUMBER_PATTERN =
      Pattern.compile("\\b(?:memo\\s+)?(?:#\\s*)?(\\d+)\\b", Pattern.CASE_INSENSITIVE);

  public enum Direction {
    INBOUND,
    OUTBOUND,
    SERVICE
  }

  public record Row(
      Instant at,
      Direction direction,
      String memoId,
      String correspondent,
      String status,
      String text) {

    public Row {
      at = at == null ? Instant.now() : at;
      direction = direction == null ? Direction.SERVICE : direction;
      memoId = Objects.toString(memoId, "").trim();
      correspondent = Objects.toString(correspondent, "").trim();
      status = Objects.toString(status, "").trim();
      text = Objects.toString(text, "").trim();
    }
  }

  private final MemoTableModel model = new MemoTableModel();
  private final JTable table = new JTable(model);
  private final JLabel summaryLabel = new JLabel();
  private final JButton refreshButton = new JButton();
  private final JButton readButton = new JButton();
  private final JButton sendButton = new JButton();
  private final JButton deleteButton = new JButton();
  private final JButton clearButton = new JButton();

  private final Map<String, ArrayList<Row>> rowsByServer = newAccessOrderedMap();
  private final Map<String, String> statusByServer = newAccessOrderedMap();

  private volatile String serverId = "";
  private volatile Consumer<String> onEmitCommand = line -> {};

  public MemoServPanel() {
    super(new BorderLayout());

    JPanel header = new JPanel(new BorderLayout());
    header.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

    JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    actionRow.setOpaque(false);
    configureActionButtons();
    refreshButton.addActionListener(event -> refreshRequested());
    readButton.addActionListener(event -> readSelectedRequested());
    sendButton.addActionListener(event -> sendRequested());
    deleteButton.addActionListener(event -> deleteSelectedRequested());
    clearButton.addActionListener(event -> clearRowsForCurrentServer());
    actionRow.add(refreshButton);
    actionRow.add(readButton);
    actionRow.add(sendButton);
    actionRow.add(deleteButton);
    actionRow.add(clearButton);

    summaryLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
    summaryLabel.setName("memoserv.summary");
    summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.PLAIN));

    header.add(actionRow, BorderLayout.NORTH);
    header.add(summaryLabel, BorderLayout.SOUTH);
    add(header, BorderLayout.NORTH);

    table.setFillsViewportHeight(true);
    table.setRowSelectionAllowed(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setShowHorizontalLines(false);
    table.setShowVerticalLines(false);
    table.setName("memoserv.table");
    table.getTableHeader().setReorderingAllowed(false);
    table.getColumnModel().getColumn(MemoTableModel.COL_TIME).setPreferredWidth(150);
    table.getColumnModel().getColumn(MemoTableModel.COL_DIRECTION).setPreferredWidth(90);
    table.getColumnModel().getColumn(MemoTableModel.COL_ID).setPreferredWidth(60);
    table.getColumnModel().getColumn(MemoTableModel.COL_CORRESPONDENT).setPreferredWidth(180);
    table.getColumnModel().getColumn(MemoTableModel.COL_STATUS).setPreferredWidth(100);
    table.getColumnModel().getColumn(MemoTableModel.COL_TEXT).setPreferredWidth(620);
    table
        .getSelectionModel()
        .addListSelectionListener(
            event -> {
              if (!event.getValueIsAdjusting()) updateButtonState();
            });
    add(new JScrollPane(table), BorderLayout.CENTER);

    setServerId("");
  }

  public void setServerId(String serverId) {
    String previous = this.serverId;
    this.serverId = normalize(serverId);
    log.info(
        "[memoserv] panel server changed previous={} current={} cachedRows={}",
        previous,
        this.serverId,
        rowsByServer.getOrDefault(this.serverId, new ArrayList<>()).size());
    refreshRows();
  }

  public String currentServerId() {
    return serverId;
  }

  public void setOnEmitCommand(Consumer<String> onEmitCommand) {
    this.onEmitCommand = onEmitCommand == null ? line -> {} : onEmitCommand;
    updateButtonState();
  }

  public void observeMemoServNotice(String serverId, Instant at, String from, String text) {
    String sid = normalize(serverId);
    String body = Objects.toString(text, "").trim();
    if (sid.isEmpty() || body.isEmpty()) {
      log.info(
          "[memoserv] panel dropped inbound notice serverId={} bodyLength={}", sid, body.length());
      return;
    }

    Row parsed = parseIncomingRow(at, from, body);
    log.info(
        "[memoserv] panel observed inbound notice serverId={} activeServer={} from={} bodyLength={} preview={}",
        sid,
        this.serverId,
        from,
        body.length(),
        preview(body));
    if (parsed.direction() == Direction.SERVICE && parsed.memoId().isEmpty()) {
      statusByServer.put(sid, summarizeNotice(body));
      trimServerBuckets();
      log.info(
          "[memoserv] panel treated notice as service status serverId={} status={} preview={}",
          sid,
          parsed.status(),
          preview(body));
      if (sid.equals(this.serverId)) {
        refreshRows();
      }
      return;
    }
    ArrayList<Row> rows = rowsByServer.computeIfAbsent(sid, ignored -> new ArrayList<>());
    int before = rows.size();
    mergeOrAppend(rows, parsed);
    trimRows(rows);
    trimServerBuckets();
    log.info(
        "[memoserv] panel parsed inbound notice serverId={} direction={} memoId={} correspondent={} status={} rowsBefore={} rowsAfter={}",
        sid,
        parsed.direction(),
        parsed.memoId(),
        parsed.correspondent(),
        parsed.status(),
        before,
        rows.size());
    if (sid.equals(this.serverId)) {
      refreshRows();
    }
  }

  public void appendOutboundMemo(String serverId, String recipient, String text) {
    String sid = normalize(serverId);
    String to = normalize(recipient);
    String body = Objects.toString(text, "").trim();
    if (sid.isEmpty() || to.isEmpty() || body.isEmpty()) {
      log.info(
          "[memoserv] panel dropped outbound memo serverId={} recipient={} bodyLength={}",
          sid,
          to,
          body.length());
      return;
    }
    ArrayList<Row> rows = rowsByServer.computeIfAbsent(sid, ignored -> new ArrayList<>());
    rows.add(
        new Row(Instant.now(), Direction.OUTBOUND, "", to, message("memoserv.status.sent"), body));
    trimRows(rows);
    statusByServer.put(sid, message("memoserv.summary.sent", to));
    trimServerBuckets();
    log.info(
        "[memoserv] panel appended outbound memo serverId={} recipient={} bodyLength={} rows={}",
        sid,
        to,
        body.length(),
        rows.size());
    if (sid.equals(this.serverId)) {
      refreshRows();
    }
  }

  private void configureActionButtons() {
    configureActionButton(
        refreshButton,
        "refresh",
        message("memoserv.button.refresh.tooltip"),
        message("memoserv.button.refresh.accessibleName"));
    configureActionButton(
        readButton,
        "eye",
        message("memoserv.button.read.tooltip"),
        message("memoserv.button.read.accessibleName"));
    configureActionButton(
        sendButton,
        "plus",
        message("memoserv.button.send.tooltip"),
        message("memoserv.button.send.accessibleName"));
    configureActionButton(
        deleteButton,
        "trash",
        message("memoserv.button.delete.tooltip"),
        message("memoserv.button.delete.accessibleName"));
    configureActionButton(
        clearButton,
        "close",
        message("memoserv.button.clear.tooltip"),
        message("common.button.clear"));
    refreshButton.setName("memoserv.refreshButton");
    readButton.setName("memoserv.readButton");
    sendButton.setName("memoserv.sendButton");
    deleteButton.setName("memoserv.deleteButton");
    clearButton.setName("memoserv.clearButton");
  }

  private void configureActionButton(
      JButton button, String iconName, String tooltip, String accessibleName) {
    button.setText("");
    button.setIcon(SvgIcons.action(iconName, ACTION_ICON_SIZE));
    button.setDisabledIcon(SvgIcons.actionDisabled(iconName, ACTION_ICON_SIZE));
    button.setToolTipText(tooltip);
    button.setFocusable(false);
    button.setPreferredSize(ACTION_BUTTON_SIZE);
    button.getAccessibleContext().setAccessibleName(accessibleName);
  }

  private void refreshRequested() {
    emitMemoServCommand("LIST");
    setLocalStatus(message("memoserv.summary.refreshRequested"));
  }

  private void readSelectedRequested() {
    String memoId = selectedMemoId();
    if (memoId.isEmpty()) return;
    emitMemoServCommand("READ " + memoId);
    setLocalStatus(message("memoserv.summary.readRequested", memoId));
  }

  private void deleteSelectedRequested() {
    String memoId = selectedMemoId();
    if (memoId.isEmpty()) return;
    int choice =
        JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(this),
            message("memoserv.confirm.delete.message", memoId),
            message("memoserv.confirm.delete.title"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
    if (choice != JOptionPane.OK_OPTION) return;
    emitMemoServCommand("DEL " + memoId);
    setLocalStatus(message("memoserv.summary.deleteRequested", memoId));
  }

  private void sendRequested() {
    JTextField recipientField = new JTextField();
    recipientField.putClientProperty(
        FlatClientProperties.PLACEHOLDER_TEXT, message("memoserv.prompt.recipient.placeholder"));
    JTextArea messageArea = new JTextArea(6, 42);
    messageArea.setLineWrap(true);
    messageArea.setWrapStyleWord(true);
    Object[] fields = {
      message("memoserv.prompt.recipient.label"),
      recipientField,
      message("memoserv.prompt.message.label"),
      new JScrollPane(messageArea)
    };
    int choice =
        JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(this),
            fields,
            message("memoserv.prompt.send.title"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
    if (choice != JOptionPane.OK_OPTION) return;

    String recipient = normalize(recipientField.getText());
    String body = Objects.toString(messageArea.getText(), "").trim();
    if (recipient.isEmpty() || recipient.contains(" ") || body.isEmpty()) {
      JOptionPane.showMessageDialog(
          SwingUtilities.getWindowAncestor(this),
          message("memoserv.prompt.send.invalid"),
          message("memoserv.prompt.send.invalid.title"),
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    appendOutboundMemo(serverId, recipient, body);
    emitMemoServCommand("SEND " + recipient + " " + body);
  }

  private void clearRowsForCurrentServer() {
    String sid = serverId;
    if (sid.isEmpty()) {
      log.info("[memoserv] panel ignored clear with no active server");
      return;
    }
    int before = rowsByServer.getOrDefault(sid, new ArrayList<>()).size();
    rowsByServer.remove(sid);
    statusByServer.remove(sid);
    log.info("[memoserv] panel cleared rows serverId={} rowsBefore={}", sid, before);
    refreshRows();
  }

  private void emitMemoServCommand(String command) {
    String sid = serverId;
    String cmd = Objects.toString(command, "").trim();
    if (sid.isEmpty() || cmd.isEmpty()) {
      log.info("[memoserv] panel dropped command serverId={} commandLength={}", sid, cmd.length());
      return;
    }
    log.info(
        "[memoserv] panel emitting command serverId={} verb={} commandLength={} preview={}",
        sid,
        commandVerb(cmd),
        cmd.length(),
        preview(cmd));
    onEmitCommand.accept(cmd);
  }

  private void setLocalStatus(String status) {
    String sid = serverId;
    if (sid.isEmpty()) return;
    statusByServer.put(sid, Objects.toString(status, "").trim());
    trimServerBuckets();
    refreshRows();
  }

  private String selectedMemoId() {
    int viewRow = table.getSelectedRow();
    if (viewRow < 0) return "";
    Row row = model.rowAt(table.convertRowIndexToModel(viewRow));
    if (row == null || row.direction() != Direction.INBOUND) return "";
    return Objects.toString(row.memoId(), "").trim();
  }

  private void refreshRows() {
    String sid = serverId;
    List<Row> rows = sid.isEmpty() ? List.of() : rowsByServer.getOrDefault(sid, new ArrayList<>());
    model.setRows(rows);
    log.info(
        "[memoserv] panel refreshed table serverId={} visibleRows={} cachedServers={}",
        sid,
        rows.size(),
        rowsByServer.size());
    updateSummary();
    updateButtonState();
  }

  private void updateSummary() {
    String sid = serverId;
    if (sid.isEmpty()) {
      summaryLabel.setText(message("memoserv.summary.noServer"));
      return;
    }
    int count = rowsByServer.getOrDefault(sid, new ArrayList<>()).size();
    String status = Objects.toString(statusByServer.get(sid), "").trim();
    if (status.isEmpty()) {
      status = message("memoserv.summary.initial");
    }
    summaryLabel.setText(message("memoserv.summary.server", sid, count, status));
  }

  private void updateButtonState() {
    boolean hasServer = !serverId.isEmpty();
    boolean hasRows = model.getRowCount() > 0;
    String selectedMemoId = selectedMemoId();
    refreshButton.setEnabled(hasServer);
    sendButton.setEnabled(hasServer);
    readButton.setEnabled(hasServer && !selectedMemoId.isEmpty());
    deleteButton.setEnabled(hasServer && !selectedMemoId.isEmpty());
    clearButton.setEnabled(hasServer && hasRows);
  }

  private static Row parseIncomingRow(Instant at, String from, String text) {
    String body = Objects.toString(text, "").trim();
    Matcher liberaListMatcher = LIBERA_MEMOSERV_LIST_PATTERN.matcher(body);
    if (liberaListMatcher.matches()) {
      String sent = cleanText(liberaListMatcher.group(3), "");
      String to = cleanText(liberaListMatcher.group(4), "");
      String details = sent.isEmpty() ? "" : "Sent: " + sent;
      if (!to.isEmpty()) {
        details = details.isEmpty() ? "To: " + to : details + " To: " + to;
      }
      return new Row(
          at,
          Direction.INBOUND,
          liberaListMatcher.group(1),
          cleanCorrespondent(liberaListMatcher.group(2)),
          statusForText(body),
          details);
    }
    Matcher wordMatcher = MEMO_WORD_FROM_PATTERN.matcher(body);
    if (wordMatcher.matches()) {
      return new Row(
          at,
          Direction.INBOUND,
          wordMatcher.group(1),
          cleanCorrespondent(wordMatcher.group(2)),
          statusForText(body),
          cleanText(wordMatcher.group(3), body));
    }
    Matcher numberMatcher = MEMO_NUMBER_FROM_PATTERN.matcher(body);
    if (numberMatcher.matches()) {
      return new Row(
          at,
          Direction.INBOUND,
          numberMatcher.group(1),
          cleanCorrespondent(numberMatcher.group(2)),
          statusForText(body),
          cleanText(numberMatcher.group(3), body));
    }
    if (looksMemoServStatusLine(body)) {
      return new Row(
          at, Direction.SERVICE, "", cleanCorrespondent(from), statusForText(body), body);
    }
    String memoId = "";
    Matcher idMatcher = MEMO_NUMBER_PATTERN.matcher(body);
    if (idMatcher.find() && looksMemoRelated(body)) {
      memoId = idMatcher.group(1);
    }
    Direction direction = memoId.isEmpty() ? Direction.SERVICE : Direction.INBOUND;
    return new Row(at, direction, memoId, cleanCorrespondent(from), statusForText(body), body);
  }

  private static void mergeOrAppend(ArrayList<Row> rows, Row next) {
    if (rows == null || next == null) return;
    String id = next.memoId();
    if (!id.isEmpty() && next.direction() == Direction.INBOUND) {
      for (int i = 0; i < rows.size(); i++) {
        Row existing = rows.get(i);
        if (existing == null || existing.direction() != Direction.INBOUND) continue;
        if (!id.equals(existing.memoId())) continue;
        rows.set(i, mergeRows(existing, next));
        return;
      }
    }
    if (!rows.isEmpty()) {
      Row last = rows.get(rows.size() - 1);
      if (last != null
          && last.direction() == next.direction()
          && Objects.equals(last.memoId(), next.memoId())
          && Objects.equals(last.correspondent(), next.correspondent())
          && Objects.equals(last.text(), next.text())) {
        return;
      }
    }
    rows.add(next);
  }

  private static Row mergeRows(Row existing, Row next) {
    String text = next.text().isEmpty() ? existing.text() : next.text();
    String correspondent =
        next.correspondent().isEmpty() ? existing.correspondent() : next.correspondent();
    String status = next.status().isEmpty() ? existing.status() : next.status();
    return new Row(next.at(), next.direction(), existing.memoId(), correspondent, status, text);
  }

  private static void trimRows(ArrayList<Row> rows) {
    if (rows == null) return;
    while (rows.size() > MAX_ROWS_PER_SERVER) {
      rows.remove(0);
    }
  }

  private void trimServerBuckets() {
    while (rowsByServer.size() > MAX_SERVER_BUCKETS) {
      String eldest = rowsByServer.keySet().iterator().next();
      rowsByServer.remove(eldest);
      statusByServer.remove(eldest);
    }
    while (statusByServer.size() > MAX_SERVER_BUCKETS) {
      String eldest = statusByServer.keySet().iterator().next();
      statusByServer.remove(eldest);
    }
  }

  private static <T> Map<String, T> newAccessOrderedMap() {
    return new LinkedHashMap<>(16, 0.75f, true);
  }

  private static String summarizeNotice(String text) {
    String body = Objects.toString(text, "").trim();
    if (body.isEmpty()) return "";
    if (body.length() <= 180) return body;
    return body.substring(0, 177) + "...";
  }

  private static String statusForText(String text) {
    String lower = Objects.toString(text, "").toLowerCase(Locale.ROOT);
    if (lower.contains("unread") || lower.contains("new memo")) {
      return message("memoserv.status.unread");
    }
    if (lower.contains("read")) return message("memoserv.status.read");
    return "";
  }

  private static boolean looksMemoRelated(String text) {
    String lower = Objects.toString(text, "").toLowerCase(Locale.ROOT);
    return lower.contains("memo") || lower.contains("memoserv");
  }

  private static boolean looksMemoServStatusLine(String text) {
    String lower = Objects.toString(text, "").trim().toLowerCase(Locale.ROOT);
    return lower.matches("^you have \\d+ .*memo.*")
        || lower.startsWith("to read ")
        || lower.startsWith("last login from:")
        || lower.startsWith("no memos");
  }

  private static String cleanCorrespondent(String value) {
    String v = normalize(value);
    if (v.endsWith(".")) v = v.substring(0, v.length() - 1).trim();
    if (v.equalsIgnoreCase("MemoServ")) return "MemoServ";
    return v;
  }

  private static String cleanText(String candidate, String fallback) {
    String text = Objects.toString(candidate, "").trim();
    return text.isEmpty() ? Objects.toString(fallback, "").trim() : text;
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }

  private static String commandVerb(String command) {
    String cmd = Objects.toString(command, "").trim();
    int space = cmd.indexOf(' ');
    return space < 0 ? cmd : cmd.substring(0, space);
  }

  private static String preview(String value) {
    String text = Objects.toString(value, "").replace('\n', ' ').replace('\r', ' ').trim();
    if (text.length() <= 180) return text;
    return text.substring(0, 177) + "...";
  }

  private static String message(String code, Object... args) {
    return MESSAGES.text(code, args);
  }

  private static final class MemoTableModel extends AbstractTableModel {
    static final int COL_TIME = 0;
    static final int COL_DIRECTION = 1;
    static final int COL_ID = 2;
    static final int COL_CORRESPONDENT = 3;
    static final int COL_STATUS = 4;
    static final int COL_TEXT = 5;

    private final String[] columns = {
      message("memoserv.column.time"),
      message("memoserv.column.direction"),
      message("memoserv.column.id"),
      message("memoserv.column.correspondent"),
      message("memoserv.column.status"),
      message("memoserv.column.text")
    };
    private List<Row> rows = List.of();

    void setRows(List<Row> nextRows) {
      rows = nextRows == null ? List.of() : List.copyOf(nextRows);
      fireTableDataChanged();
    }

    Row rowAt(int modelRow) {
      if (modelRow < 0 || modelRow >= rows.size()) return null;
      return rows.get(modelRow);
    }

    @Override
    public int getRowCount() {
      return rows.size();
    }

    @Override
    public int getColumnCount() {
      return columns.length;
    }

    @Override
    public String getColumnName(int column) {
      return column >= 0 && column < columns.length ? columns[column] : "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      Row row = rowAt(rowIndex);
      if (row == null) return "";
      return switch (columnIndex) {
        case COL_TIME -> TIME_FORMAT.format(row.at());
        case COL_DIRECTION ->
            switch (row.direction()) {
              case INBOUND -> message("memoserv.direction.inbound");
              case OUTBOUND -> message("memoserv.direction.outbound");
              case SERVICE -> message("memoserv.direction.service");
            };
        case COL_ID -> row.memoId();
        case COL_CORRESPONDENT -> row.correspondent();
        case COL_STATUS -> row.status();
        case COL_TEXT -> row.text();
        default -> "";
      };
    }
  }
}

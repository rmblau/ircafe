package cafe.woden.ircclient.ui.input;

import cafe.woden.ircclient.ui.input.spi.MatrixUploadMsgTypeProvider;
import cafe.woden.ircclient.ui.input.spi.MatrixUploadMsgTypeRule;
import cafe.woden.ircclient.ui.localization.UiMessages;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MatrixMessageInputUploadUxMode implements MessageInputUploadUxMode {
  private static final Logger log = LoggerFactory.getLogger(MatrixMessageInputUploadUxMode.class);

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();
  private final List<MatrixUploadMsgTypeRule> msgTypeRules;

  MatrixMessageInputUploadUxMode() {
    this(MessageInputPluginProviders.builtInMatrixUploadMsgTypeProviders());
  }

  MatrixMessageInputUploadUxMode(List<MatrixUploadMsgTypeProvider> msgTypeProviders) {
    this.msgTypeRules = msgTypeRulesFromProviders(msgTypeProviders);
  }

  static List<MatrixUploadMsgTypeRule> defaultMsgTypeRules() {
    return new BuiltInMatrixUploadMsgTypeProvider().uploadMsgTypeRules();
  }

  static List<MatrixUploadMsgTypeRule> msgTypeRulesFromProviders(
      List<MatrixUploadMsgTypeProvider> providers) {
    ArrayList<MatrixUploadMsgTypeRule> rules = new ArrayList<>();
    for (MatrixUploadMsgTypeProvider provider : msgTypeProviderChain(providers)) {
      if (provider == null) continue;
      try {
        List<MatrixUploadMsgTypeRule> contributed = provider.uploadMsgTypeRules();
        if (contributed == null || contributed.isEmpty()) continue;
        for (MatrixUploadMsgTypeRule rule : contributed) {
          if (rule != null && rule.extensions().length > 0) rules.add(rule);
        }
      } catch (RuntimeException ex) {
        log.warn(
            "[ircafe] failed to load Matrix upload msgtype rules from {}",
            provider.getClass().getName(),
            ex);
      }
    }
    return List.copyOf(rules);
  }

  private static List<MatrixUploadMsgTypeProvider> msgTypeProviderChain(
      List<MatrixUploadMsgTypeProvider> providers) {
    LinkedHashSet<String> providerClassNames = new LinkedHashSet<>();
    ArrayList<MatrixUploadMsgTypeProvider> chain = new ArrayList<>();
    addProviders(
        chain,
        providerClassNames,
        MessageInputPluginProviders.builtInMatrixUploadMsgTypeProviders());
    addProviders(chain, providerClassNames, providers);
    return List.copyOf(chain);
  }

  private static void addProviders(
      ArrayList<MatrixUploadMsgTypeProvider> providers,
      LinkedHashSet<String> providerClassNames,
      List<MatrixUploadMsgTypeProvider> candidates) {
    for (MatrixUploadMsgTypeProvider provider :
        Objects.requireNonNullElse(candidates, List.<MatrixUploadMsgTypeProvider>of())) {
      if (provider == null || !providerClassNames.add(provider.getClass().getName())) {
        continue;
      }
      providers.add(provider);
    }
  }

  private static final ActionPresentation PRESENTATION =
      new ActionPresentation(
          true,
          MESSAGES.text("messageInput.upload.matrix.tooltip"),
          MESSAGES.text("messageInput.upload.matrix.description"));

  @Override
  public ActionPresentation presentation() {
    return PRESENTATION;
  }

  @Override
  public void runAttachAction(Context context) {
    if (context == null || !context.isInputEditable()) return;
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(MESSAGES.text("messageInput.upload.matrix.dialogTitle"));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(true);
    int result = chooser.showOpenDialog(context.ownerComponent());
    if (result != JFileChooser.APPROVE_OPTION) return;

    List<File> files = new ArrayList<>();
    File[] selected = chooser.getSelectedFiles();
    if (selected != null && selected.length > 0) {
      for (File file : selected) {
        if (file != null) files.add(file);
      }
    } else {
      File one = chooser.getSelectedFile();
      if (one != null) files.add(one);
    }
    importFileDrop(context, files);
  }

  @Override
  public boolean canImportFileDrop(Context context) {
    return context != null && context.isInputEditable();
  }

  @Override
  public boolean importFileDrop(Context context, List<File> files) {
    if (!canImportFileDrop(context)) return false;
    List<File> normalized = context.normalizeUploadFiles(files);
    if (normalized.isEmpty()) return false;

    String caption = context.consumeDraftCaptionForUpload();
    boolean first = true;
    boolean emitted = false;
    for (File file : normalized) {
      String path = normalizeUploadPath(file);
      if (path.isEmpty()) continue;
      String line =
          buildMatrixUploadCommand(inferMatrixUploadMsgType(path), path, first ? caption : "");
      if (line.isBlank()) continue;
      context.emitOutboundLine(line);
      first = false;
      emitted = true;
    }
    return emitted;
  }

  private static String normalizeUploadPath(File file) {
    if (file == null) return "";
    String path = Objects.toString(file.getAbsolutePath(), "").trim();
    if (path.isEmpty()) return "";
    if (path.indexOf('\n') >= 0 || path.indexOf('\r') >= 0) return "";
    return path;
  }

  private String inferMatrixUploadMsgType(String path) {
    String ext = extensionForPath(path);
    if (ext.isEmpty()) return "m.file";
    for (MatrixUploadMsgTypeRule rule : msgTypeRules) {
      if (rule == null) continue;
      for (String candidate : rule.extensions()) {
        if (ext.equals(candidate)) return rule.msgType();
      }
    }
    return "m.file";
  }

  private static String extensionForPath(String path) {
    String rawPath = Objects.toString(path, "").trim();
    if (rawPath.isEmpty()) return "";
    try {
      Path fileName = Path.of(rawPath).getFileName();
      String name = fileName == null ? rawPath : Objects.toString(fileName.toString(), "");
      int dot = name.lastIndexOf('.');
      if (dot < 0 || dot == name.length() - 1) return "";
      return name.substring(dot + 1).trim().toLowerCase(Locale.ROOT);
    } catch (Exception ignored) {
      return "";
    }
  }

  static String buildMatrixUploadCommand(String msgType, String path, String caption) {
    String type = Objects.toString(msgType, "").trim();
    String filePath = Objects.toString(path, "").trim();
    if (type.isEmpty() || filePath.isEmpty()) return "";
    StringBuilder line = new StringBuilder();
    line.append("/upload ").append(type).append(" ").append(quoteUploadPath(filePath));
    String text = Objects.toString(caption, "").trim();
    if (!text.isEmpty()) {
      line.append(" ").append(text);
    }
    return line.toString();
  }

  private static String quoteUploadPath(String path) {
    String escaped = Objects.toString(path, "").replace("\\", "\\\\").replace("\"", "\\\"");
    return "\"" + escaped + "\"";
  }
}

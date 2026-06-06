package cafe.woden.ircclient.ui.input;

import cafe.woden.ircclient.ui.localization.UiMessages;
import java.io.File;
import java.util.List;

final class IrcMessageInputUploadUxMode implements MessageInputUploadUxMode {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();
  private static final ActionPresentation PRESENTATION =
      new ActionPresentation(
          false,
          MESSAGES.text("messageInput.upload.matrixOnly.tooltip"),
          MESSAGES.text("messageInput.upload.matrixOnly.description"));

  @Override
  public ActionPresentation presentation() {
    return PRESENTATION;
  }

  @Override
  public void runAttachAction(Context context) {}

  @Override
  public boolean canImportFileDrop(Context context) {
    return false;
  }

  @Override
  public boolean importFileDrop(Context context, List<File> files) {
    return false;
  }
}

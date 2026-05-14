package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.ui.settings.appearance.AppearanceLivePreviewSession;
import cafe.woden.ircclient.ui.util.CloseableScope;
import cafe.woden.ircclient.ui.util.DialogCloseableScopeDecorator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

final class PreferencesDialogWindowSupport {
  private PreferencesDialogWindowSupport() {}

  static JDialog show(ShowRequest request) {
    JDialog dialog = createDialog(request.owner());
    if (request.dialogOpened() != null) {
      request.dialogOpened().accept(dialog);
    }

    AtomicBoolean rollbackOnClose = new AtomicBoolean(true);
    AtomicBoolean rollbackScheduled = new AtomicBoolean(false);
    dialog.addWindowListener(
        new java.awt.event.WindowAdapter() {
          @Override
          public void windowClosed(java.awt.event.WindowEvent event) {
            if (!rollbackOnClose.get()) return;
            if (!rollbackScheduled.compareAndSet(false, true)) return;
            SwingUtilities.invokeLater(
                () -> {
                  if (rollbackOnClose.get()) {
                    request.appearancePreview().restoreCommittedAppearance();
                  }
                });
          }
        });

    CloseableScope scope = DialogCloseableScopeDecorator.install(dialog);
    if (request.closeables() != null) {
      request.closeables().forEach(scope::add);
    }
    scope.addCleanup(
        () -> {
          if (request.dialogClosed() != null) {
            request.dialogClosed().accept(dialog);
          }
        });

    PreferencesDialogActionButtonsSupport.Buttons buttons = request.buttons();
    buttons.apply().addActionListener(e -> request.doApply().run());
    buttons
        .ok()
        .addActionListener(
            e -> {
              request.doApply().run();
              rollbackOnClose.set(false);
              dialog.dispose();
            });
    buttons
        .cancel()
        .addActionListener(
            e -> {
              dialog.dispose();
            });

    JTabbedPane tabs = buildTabs(request.tabs());
    dialog.setLayout(new BorderLayout());
    dialog.add(tabs, BorderLayout.CENTER);
    dialog.add(buildButtons(buttons), BorderLayout.SOUTH);
    dialog.setMinimumSize(new Dimension(680, 540));
    PreferencesDialogSizingSupport.installDynamicTabSizing(dialog, tabs, request.owner());
    dialog.setLocationRelativeTo(request.owner());
    dialog.setVisible(true);
    return dialog;
  }

  private static JDialog createDialog(Window owner) {
    JDialog dialog = new JDialog(owner, "Preferences", JDialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    return dialog;
  }

  private static JPanel buildButtons(PreferencesDialogActionButtonsSupport.Buttons buttons) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
    panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
    panel.add(buttons.apply());
    panel.add(buttons.ok());
    panel.add(buttons.cancel());
    return panel;
  }

  private static JTabbedPane buildTabs(List<Tab> entries) {
    JTabbedPane tabs = new DynamicTabbedPane();
    for (Tab entry : entries) {
      tabs.addTab(entry.title(), PreferencesDialogSizingSupport.wrapTab(entry.panel()));
    }
    return tabs;
  }

  record ShowRequest(
      Window owner,
      List<AutoCloseable> closeables,
      AppearanceLivePreviewSession appearancePreview,
      Runnable doApply,
      PreferencesDialogActionButtonsSupport.Buttons buttons,
      List<Tab> tabs,
      Consumer<JDialog> dialogOpened,
      Consumer<JDialog> dialogClosed) {}

  record Tab(String title, JPanel panel) {}
}

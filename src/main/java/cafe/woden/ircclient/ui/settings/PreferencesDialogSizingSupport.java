package cafe.woden.ircclient.ui.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

final class PreferencesDialogSizingSupport {
  private PreferencesDialogSizingSupport() {}

  static void installDynamicTabSizing(JDialog dialog, JTabbedPane tabs, Window owner) {
    ChangeListener listener =
        event -> {
          packClampAndKeepCenter(dialog, owner);
          // Some tabs with nested panels/subtabs report final preferred sizes only after
          // the first layout pass on selection.
          SwingUtilities.invokeLater(
              () -> {
                if (!dialog.isDisplayable()) return;
                packClampAndKeepCenter(dialog, owner);
              });
        };
    tabs.addChangeListener(listener);
    packClampAndKeepCenter(dialog, owner);
    // Run one more pass after the dialog is realized so viewport measurements are final.
    SwingUtilities.invokeLater(
        () -> {
          if (!dialog.isDisplayable()) return;
          packClampAndKeepCenter(dialog, owner);
        });
  }

  /**
   * Wrap a settings tab inside a scroll pane.
   *
   * <p>Important: this uses a Scrollable wrapper that tracks the viewport width. Without this, when
   * the dialog is resized larger and then smaller again, Swing can keep the tab view at the larger
   * width with no horizontal scrollbar, making controls appear to stay expanded.
   */
  static JScrollPane wrapTab(JPanel panel) {
    ScrollableViewportWidthPanel wrapper = new ScrollableViewportWidthPanel(new BorderLayout());
    wrapper.add(panel, BorderLayout.NORTH);

    JScrollPane scroll =
        new JScrollPane(
            wrapper,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBorder(null);
    scroll.setViewportBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    return scroll;
  }

  private static void packClampAndKeepCenter(JDialog dialog, Window owner) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> packClampAndKeepCenter(dialog, owner));
      return;
    }

    Point center =
        new Point(dialog.getX() + dialog.getWidth() / 2, dialog.getY() + dialog.getHeight() / 2);
    dialog.pack();
    dialog.validate();

    Rectangle usable = usableBounds(owner, dialog);
    int margin = 32;
    int maxW = Math.max(usable.width - margin, dialog.getMinimumSize().width);
    int maxH = Math.max(usable.height - margin, dialog.getMinimumSize().height);

    clampSize(dialog, maxW, maxH);
    nudgeToAvoidUnnecessaryVerticalScroll(dialog, maxH);
    clampSize(dialog, maxW, maxH);
    keepCentered(dialog, usable, center);
  }

  private static void clampSize(JDialog dialog, int maxW, int maxH) {
    Dimension size = dialog.getSize();
    int width = Math.max(dialog.getMinimumSize().width, Math.min(size.width, maxW));
    int height = Math.max(dialog.getMinimumSize().height, Math.min(size.height, maxH));
    if (width != size.width || height != size.height) {
      dialog.setSize(width, height);
      dialog.validate();
    }
  }

  private static void keepCentered(JDialog dialog, Rectangle usable, Point center) {
    int x = center.x - dialog.getWidth() / 2;
    int y = center.y - dialog.getHeight() / 2;
    x = Math.max(usable.x, Math.min(x, usable.x + usable.width - dialog.getWidth()));
    y = Math.max(usable.y, Math.min(y, usable.y + usable.height - dialog.getHeight()));
    dialog.setLocation(x, y);
  }

  private static void nudgeToAvoidUnnecessaryVerticalScroll(JDialog dialog, int maxDialogHeight) {
    if (dialog == null || !dialog.isShowing()) return;
    Container root = dialog.getContentPane();
    if (root == null) return;

    JTabbedPane tabs = null;
    for (Component component : root.getComponents()) {
      if (component instanceof JTabbedPane tabbedPane) {
        tabs = tabbedPane;
        break;
      }
    }
    if (tabs == null) return;

    Component selected = tabs.getSelectedComponent();
    if (!(selected instanceof JScrollPane scrollPane)) return;

    Component view = scrollPane.getViewport() != null ? scrollPane.getViewport().getView() : null;
    if (view == null) return;

    // Force layout so viewport sizes are current.
    scrollPane.doLayout();
    if (scrollPane.getViewport() != null) scrollPane.getViewport().doLayout();
    view.doLayout();

    Dimension viewPref = view.getPreferredSize();
    Dimension extent =
        scrollPane.getViewport() != null ? scrollPane.getViewport().getExtentSize() : null;
    if (viewPref == null || extent == null) return;

    int missing = viewPref.height - extent.height;
    if (missing <= 0) return;

    // Only nudge if the missing amount is small-ish (we're fixing "almost fits" cases).
    if (missing > 220) return;

    Dimension dialogSize = dialog.getSize();
    int targetHeight = Math.min(maxDialogHeight, dialogSize.height + missing);
    if (targetHeight > dialogSize.height) {
      dialog.setSize(dialogSize.width, targetHeight);
      dialog.validate();
    }
  }

  private static Rectangle usableBounds(Window owner, Window fallback) {
    try {
      var graphicsConfiguration =
          owner != null ? owner.getGraphicsConfiguration() : fallback.getGraphicsConfiguration();
      if (graphicsConfiguration == null) {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
      }

      Rectangle bounds = graphicsConfiguration.getBounds();
      Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);
      return new Rectangle(
          bounds.x + insets.left,
          bounds.y + insets.top,
          bounds.width - insets.left - insets.right,
          bounds.height - insets.top - insets.bottom);
    } catch (Exception ignored) {
      return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    }
  }

  private static final class ScrollableViewportWidthPanel extends JPanel implements Scrollable {
    private ScrollableViewportWidthPanel(LayoutManager layout) {
      super(layout);
    }

    @Override
    public Dimension getMinimumSize() {
      Dimension dimension = super.getMinimumSize();
      return new Dimension(0, dimension != null ? dimension.height : 0);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
      return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
      if (orientation == SwingConstants.VERTICAL) {
        return Math.max(32, visibleRect.height - 32);
      }
      return Math.max(32, visibleRect.width - 32);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
      return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
      return false;
    }
  }
}

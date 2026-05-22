package cafe.woden.ircclient.ui.util;

import net.miginfocom.swing.MigLayout;

/** Factory methods for common MigLayout panel layouts. */
public final class MigLayouts {

  public static MigLayout singleColumn() {
    return singleColumn("");
  }

  public static MigLayout singleColumn(String rows) {
    return singleColumn(0, rows);
  }

  public static MigLayout singleColumn(int insets) {
    return singleColumn(insets, "");
  }

  public static MigLayout singleColumn(int insets, String rows) {
    return fillXWrap(insets, 1, MigLayoutConstraints.GROW_FILL, rows);
  }

  public static MigLayout singleColumnFill(int insets) {
    return singleColumnFill(insets, "");
  }

  public static MigLayout singleColumnFill(int insets, String rows) {
    return fillWrap(insets, 1, MigLayoutConstraints.GROW_FILL, rows);
  }

  public static MigLayout twoColumnForm(int labelGap) {
    return twoColumnForm(labelGap, "");
  }

  public static MigLayout twoColumnForm(int labelGap, String rows) {
    return twoColumnForm(0, labelGap, rows);
  }

  public static MigLayout twoColumnForm(int insets, int labelGap) {
    return twoColumnForm(insets, labelGap, "");
  }

  public static MigLayout twoColumnForm(int insets, int labelGap, String rows) {
    return fillXWrap(insets, 2, rightGrowFill(labelGap), rows);
  }

  public static MigLayout twoColumnFillForm(int insets, int labelGap, String rows) {
    return fillWrap(insets, 2, rightGrowFill(labelGap), rows);
  }

  public static MigLayout twoColumnFormWithHideMode(
      int insets, int labelGap, int hideMode, String rows) {
    return fillXWrapWithHideMode(insets, 2, hideMode, rightGrowFill(labelGap), rows);
  }

  public static MigLayout labelFieldActionsForm(int labelGap, int actionCount, String rows) {
    return fillXWrap(0, 2 + actionCount, labelFieldActionsColumns(labelGap, actionCount), rows);
  }

  public static MigLayout labelFieldActionsFormWithHideMode(
      int insets, int labelGap, int actionCount, int hideMode, String rows) {
    return fillXWrapWithHideMode(
        insets, 2 + actionCount, hideMode, labelFieldActionsColumns(labelGap, actionCount), rows);
  }

  public static MigLayout fillX(String columns, String rows) {
    return new MigLayout(MigLayoutConstraints.INSETS_0_FILL_X, columns, rows);
  }

  public static MigLayout fillXGrowTrailing() {
    return fillX(MigLayoutConstraints.GROW_FILL_TRAILING, MigLayoutConstraints.ROW);
  }

  public static MigLayout fillXGrowTrailing(int gap) {
    return fillX(MigLayoutConstraints.growFillGapTrailing(gap), MigLayoutConstraints.ROW);
  }

  public static MigLayout fillX(int insets, String columns, String rows) {
    return new MigLayout(layout(insets, "fillx"), columns, rows);
  }

  public static MigLayout fillX(String insets, String columns, String rows) {
    return new MigLayout(layout(insets, "fillx"), columns, rows);
  }

  public static MigLayout fillXWithHideMode(int insets, int hideMode, String columns, String rows) {
    return new MigLayout(layout(insets, "fillx") + ", hidemode " + hideMode, columns, rows);
  }

  public static MigLayout fillXWithHideMode(
      String insets, int hideMode, String columns, String rows) {
    return new MigLayout(layout(insets, "fillx") + ", hidemode " + hideMode, columns, rows);
  }

  public static MigLayout insets0(String columns, String rows) {
    return new MigLayout(MigLayoutConstraints.INSETS_0, columns, rows);
  }

  public static MigLayout fillXWrap(int insets, int wrap, String columns, String rows) {
    return new MigLayout(layout(insets, "fillx", wrap), columns, rows);
  }

  public static MigLayout fillXWrap(String insets, int wrap, String columns, String rows) {
    return new MigLayout(layout(insets, "fillx", wrap), columns, rows);
  }

  public static MigLayout fillXWrapWithHideMode(
      int insets, int wrap, int hideMode, String columns, String rows) {
    return new MigLayout(layout(insets, "fillx", wrap) + ", hidemode " + hideMode, columns, rows);
  }

  public static MigLayout fillXWrapWithHideMode(
      String insets, int wrap, int hideMode, String columns, String rows) {
    return new MigLayout(layout(insets, "fillx", wrap) + ", hidemode " + hideMode, columns, rows);
  }

  public static MigLayout fillWrap(int insets, int wrap, String columns, String rows) {
    return new MigLayout(layout(insets, "fill", wrap), columns, rows);
  }

  public static MigLayout fillWrapWithHideMode(
      int insets, int wrap, int hideMode, String columns, String rows) {
    return new MigLayout(layout(insets, "fill", wrap) + ", hidemode " + hideMode, columns, rows);
  }

  public static MigLayout wrap(int insets, int wrap, String columns, String rows) {
    return new MigLayout(layout(insets, wrap), columns, rows);
  }

  public static MigLayout wrapWithGap(int insets, int wrap, int gap, String columns, String rows) {
    return new MigLayout(layout(insets, wrap) + ", gap " + gap, columns, rows);
  }

  public static String rows(int count, int gap) {
    return MigLayoutConstraints.rows(count, gap);
  }

  public static String rowGaps(int firstGap, int... remainingGaps) {
    return MigLayoutConstraints.rowGaps(firstGap, remainingGaps);
  }

  public static String rightGrowFill(int gap) {
    return gap > 0 ? "[right]" + gap + "[grow,fill]" : "[right][grow,fill]";
  }

  private static String labelFieldActionsColumns(int labelGap, int actionCount) {
    StringBuilder columns = new StringBuilder(rightGrowFill(labelGap));
    for (int i = 0; i < actionCount; i++) {
      columns.append(labelGap).append("[]");
    }
    return columns.toString();
  }

  private static String layout(int insets, String fillMode, int wrap) {
    return "insets " + insets + ", " + fillMode + ", wrap " + wrap;
  }

  private static String layout(int insets, String fillMode) {
    return "insets " + insets + ", " + fillMode;
  }

  private static String layout(String insets, String fillMode, int wrap) {
    return layout(insets, fillMode) + ", wrap " + wrap;
  }

  private static String layout(String insets, String fillMode) {
    return "insets " + insets + ", " + fillMode;
  }

  private static String layout(int insets, int wrap) {
    return "insets " + insets + ", wrap " + wrap;
  }

  private MigLayouts() {}
}

package cafe.woden.ircclient.ui.util;

/** Common MigLayout layout, row, and column constraint strings used by the Swing UI. */
public final class MigLayoutConstraints {

  public static final String LEADING_GROW_FILL = "[][grow,fill]";
  public static final String GROW_FILL = "[grow,fill]";
  public static final String GROW_FILL_TRAILING = "[grow,fill][]";
  public static final String GROW_FILL_GAP_6_TRAILING = "[grow,fill]6[]";
  public static final String GROW_FILL_GAP_12_GROW_FILL = "[grow,fill]12[grow,fill]";
  public static final String GROW_FILL_PAIR = "[grow,fill][grow,fill]";
  public static final String ROW_6_GROW_FILL = "[]6[grow,fill]";
  public static final String ROW_8_GROW_FILL = "[]8[grow,fill]";
  public static final String RIGHT_GROW_FILL = "[right][grow,fill]";
  public static final String RIGHT_12_GROW_FILL_MIN_0 = "[right]12[grow,fill,min:0]";

  public static final String INSETS_0 = "insets 0";
  public static final String INSETS_0_FILL_X = "insets 0, fillx";
  public static final String INSETS_0_FILL_X_WRAP_2 = "insets 0, fillx, wrap 2";
  public static final String INSETS_0_FILL_X_WRAP_4 = "insets 0, fillx, wrap 4";
  public static final String INSETS_0_FILL_WRAP_1 = "insets 0, fill, wrap 1";

  public static String rows(int count, int gap) {
    if (count <= 0) {
      throw new IllegalArgumentException("count must be positive");
    }
    if (gap < 0) {
      throw new IllegalArgumentException("gap must not be negative");
    }
    StringBuilder rows = new StringBuilder("[]");
    for (int i = 1; i < count; i++) {
      rows.append(gap).append("[]");
    }
    return rows.toString();
  }

  public static String rowGaps(int firstGap, int... remainingGaps) {
    StringBuilder rows = new StringBuilder("[]");
    appendRowGap(rows, firstGap);
    for (int gap : remainingGaps) {
      appendRowGap(rows, gap);
    }
    return rows.toString();
  }

  private static void appendRowGap(StringBuilder rows, int gap) {
    if (gap < 0) {
      throw new IllegalArgumentException("gaps must not be negative");
    }
    rows.append(gap).append("[]");
  }

  private MigLayoutConstraints() {}
}

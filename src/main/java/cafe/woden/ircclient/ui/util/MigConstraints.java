package cafe.woden.ircclient.ui.util;

import net.miginfocom.layout.CC;

/** Factory methods for MigLayout component constraints. */
public final class MigConstraints {

  private static final String TOP = "top";
  private static final String LEFT = "left";
  private static final String RIGHT = "right";
  private static final String CENTER = "center";
  private static final String OK = "ok";

  public static CC growX() {
    return new CC().growX();
  }

  public static CC grow() {
    return new CC().grow();
  }

  public static CC growXWrap() {
    return growX().wrap();
  }

  public static CC growXWrap(int gap) {
    return growX().wrap(Integer.toString(gap));
  }

  public static CC growXMinWidth(int width) {
    return growX().minWidth(Integer.toString(width));
  }

  public static CC growXMinWidthWrap(int width) {
    return growXMinWidth(width).wrap();
  }

  public static CC growXMinWidthHideModeWrap(int width, int hideMode) {
    return growXMinWidth(width).hideMode(hideMode).wrap();
  }

  public static CC growXMinWidthHeightWrap(int width, int height) {
    return growXMinWidth(width).height(exact(height)).wrap();
  }

  public static CC growXMinWidthHeightBoundsGapLeftWrap(
      int width, String heightBounds, int gapLeft) {
    return growXMinWidth(width).height(heightBounds).gapLeft(Integer.toString(gapLeft)).wrap();
  }

  public static CC growXMinWidthGapLeftWrap(int width, int gapLeft) {
    return growXMinWidth(width).gapLeft(Integer.toString(gapLeft)).wrap();
  }

  public static CC growXMinWidthSplit(int width, int parts) {
    return growXMinWidth(width).split(parts);
  }

  private static CC growXPushXMinWidth(int width) {
    return growXMinWidth(width).pushX();
  }

  private static CC growXPushXMinWidthWrap(int width) {
    return growXPushXMinWidth(width).wrap();
  }

  public static CC pushXGrowX() {
    return new CC().pushX().growX();
  }

  public static CC push() {
    return new CC().push();
  }

  public static CC growYPushY() {
    return new CC().growY().pushY();
  }

  public static CC growPush() {
    return new CC().grow().push();
  }

  public static CC growPushHeight(int height) {
    return growPush().height(exact(height));
  }

  public static CC growPushHeightWrap(int height) {
    return growPushHeight(height).wrap();
  }

  public static CC growPushMinHeight(int minHeight) {
    return growPush().minHeight(Integer.toString(minHeight));
  }

  private static CC growPushMinWidth(int width) {
    return growPush().minWidth(Integer.toString(width));
  }

  private static CC growPushMinWidthMinHeight(int width, int minHeight) {
    return growPushMinWidth(width).minHeight(Integer.toString(minHeight));
  }

  private static CC growPushMinWidthHeight(int width, int height) {
    return growPushMinWidth(width).height(exact(height));
  }

  private static CC growPushMinWidthHeightWrap(int width, int height) {
    return growPushMinWidthHeight(width, height).wrap();
  }

  private static CC growPushMinWidthMinHeightHideMode(int width, int minHeight, int hideMode) {
    return growPushMinWidthMinHeight(width, minHeight).hideMode(hideMode);
  }

  private static CC growPushMinWidthWrap(int width) {
    return growPushMinWidth(width).wrap();
  }

  public static CC spanX(int span) {
    return new CC().spanX(span);
  }

  public static CC spanXWrap(int span) {
    return spanX(span).wrap();
  }

  public static CC spanXWidthWrap(int span, int width) {
    return spanX(span).width(exact(width)).wrap();
  }

  public static CC spanXGrowX(int span) {
    return spanX(span).growX();
  }

  public static CC spanXGrowXPushXMinWidth(int span, int width) {
    return spanXGrowX(span).pushX().minWidth(Integer.toString(width));
  }

  private static CC spanXGrowXMinWidthPushY(int span, int width) {
    return spanXGrowX(span).minWidth(Integer.toString(width)).pushY();
  }

  public static CC spanXGrowXWrap(int span) {
    return spanXGrowX(span).wrap();
  }

  public static CC spanXGrowXGapLeftWrap(int span, int gapLeft) {
    return spanXGrowX(span).gapLeft(Integer.toString(gapLeft)).wrap();
  }

  public static CC spanXGrowXHideModeWrap(int span, int hideMode) {
    return spanXGrowX(span).hideMode(hideMode).wrap();
  }

  public static CC spanXGrowXMinWidthWrap(int span, int width) {
    return spanXGrowX(span).minWidth(Integer.toString(width)).wrap();
  }

  public static CC spanXGrowXMinWidthGapLeftWrap(int span, int width, int gapLeft) {
    return spanXGrowX(span)
        .minWidth(Integer.toString(width))
        .gapLeft(Integer.toString(gapLeft))
        .wrap();
  }

  public static CC spanXGrowXMinHeight(int span, int minHeight) {
    return spanXGrowX(span).minHeight(Integer.toString(minHeight));
  }

  private static CC spanXGrowXPushXMinWidthWrap(int span, int width) {
    return spanXGrowX(span).pushX().minWidth(Integer.toString(width)).wrap();
  }

  public static CC spanXGrowPushYMinHeight(int span, int minHeight) {
    return spanX(span).grow().pushY().minHeight(Integer.toString(minHeight));
  }

  private static CC spanXGrowPushMinWidth(int span, int width) {
    return spanX(span).grow().push().minWidth(Integer.toString(width));
  }

  public static CC spanXAlignXLeftWrap(int span) {
    return spanX(span).alignX(LEFT).wrap();
  }

  public static CC width(int width) {
    return new CC().width(exact(width));
  }

  public static CC widthWrap(int width) {
    return width(width).wrap();
  }

  public static CC widthHeight(int width, int height) {
    return width(width).height(exact(height));
  }

  public static CC growXHeightWrap(int height) {
    return growX().height(exact(height)).wrap();
  }

  public static CC growXGapTop(int gapTop) {
    return growX().gapTop(Integer.toString(gapTop));
  }

  public static CC widthHeightWrap(int width, int height) {
    return widthHeight(width, height).wrap();
  }

  public static CC widthHeightAlignRightWrap(int width, int height) {
    return widthHeight(width, height).alignX(RIGHT).wrap();
  }

  public static CC alignXRight() {
    return new CC().alignX(RIGHT);
  }

  public static CC alignXRightWrap() {
    return alignXRight().wrap();
  }

  public static CC alignXLeft() {
    return new CC().alignX(LEFT);
  }

  public static CC alignXLeftGrowXMinWidthGapLeft(int width, int gapLeft) {
    return alignXLeft()
        .growX()
        .minWidth(Integer.toString(width))
        .gapLeft(Integer.toString(gapLeft));
  }

  public static CC alignXCenter() {
    return new CC().alignX(CENTER);
  }

  public static CC alignCenter() {
    return new CC().alignX(CENTER).alignY(CENTER);
  }

  public static CC alignCenterMinWidth(int width) {
    return alignCenter().minWidth(Integer.toString(width));
  }

  public static CC alignYTop() {
    return new CC().alignY(TOP);
  }

  public static CC alignYCenter() {
    return new CC().alignY(CENTER);
  }

  public static CC tagOk() {
    return new CC().tag(OK);
  }

  public static CC wrap() {
    return new CC().wrap();
  }

  public static CC split(int parts) {
    return new CC().split(parts);
  }

  public static CC splitAlignXLeft(int parts) {
    return split(parts).alignX(LEFT);
  }

  public static CC gapLeft(int gapLeft) {
    return new CC().gapLeft(Integer.toString(gapLeft));
  }

  public static CC sizeGroup(String group) {
    return new CC().sizeGroup(group);
  }

  public static CC growXMinWidth0() {
    return growXMinWidth(0);
  }

  public static CC growXMinWidth0Wrap() {
    return growXMinWidthWrap(0);
  }

  public static CC growXPushXMinWidth0() {
    return growXPushXMinWidth(0);
  }

  public static CC growXPushXMinWidth0Wrap() {
    return growXPushXMinWidthWrap(0);
  }

  public static CC growPushMinWidth0() {
    return growPushMinWidth(0);
  }

  public static CC growPushMinWidth0Height(int height) {
    return growPushMinWidthHeight(0, height);
  }

  public static CC growPushMinWidth0HeightWrap(int height) {
    return growPushMinWidthHeightWrap(0, height);
  }

  public static CC growPushMinWidth0MinHeight(int minHeight) {
    return growPushMinWidthMinHeight(0, minHeight);
  }

  public static CC growPushMinWidth0MinHeightHideMode(int minHeight, int hideMode) {
    return growPushMinWidthMinHeightHideMode(0, minHeight, hideMode);
  }

  public static CC growPushMinWidth0Wrap() {
    return growPushMinWidthWrap(0);
  }

  public static CC span2GrowX() {
    return spanXGrowX(2);
  }

  public static CC span2GrowXWrap() {
    return spanXGrowXWrap(2);
  }

  public static CC span2GrowXMinWidth0Wrap() {
    return spanXGrowXMinWidthWrap(2, 0);
  }

  public static CC span2GrowXPushXMinWidth0Wrap() {
    return spanXGrowXPushXMinWidthWrap(2, 0);
  }

  public static CC span2GrowXMinWidth0PushY() {
    return spanXGrowXMinWidthPushY(2, 0);
  }

  public static CC span2GrowPushMinWidth0() {
    return spanXGrowPushMinWidth(2, 0);
  }

  private static String exact(int value) {
    return value + "!";
  }

  private MigConstraints() {}
}

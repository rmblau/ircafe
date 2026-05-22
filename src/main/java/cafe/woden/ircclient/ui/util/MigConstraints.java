package cafe.woden.ircclient.ui.util;

import net.miginfocom.layout.CC;

/** Factory methods for MigLayout component constraints. */
public final class MigConstraints {

  private static final String TOP = "top";
  private static final String RIGHT = "right";

  public static CC growX() {
    return new CC().growX();
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

  public static CC growXMinWidthGapLeftWrap(int width, int gapLeft) {
    return growXMinWidth(width).gapLeft(Integer.toString(gapLeft)).wrap();
  }

  public static CC growXPushXMinWidth(int width) {
    return growXMinWidth(width).pushX();
  }

  public static CC growXPushXMinWidthWrap(int width) {
    return growXPushXMinWidth(width).wrap();
  }

  public static CC pushXGrowX() {
    return new CC().pushX().growX();
  }

  public static CC growPush() {
    return new CC().grow().push();
  }

  public static CC growPushMinWidth(int width) {
    return growPush().minWidth(Integer.toString(width));
  }

  public static CC growPushMinWidthWrap(int width) {
    return growPushMinWidth(width).wrap();
  }

  public static CC spanX(int span) {
    return new CC().spanX(span);
  }

  public static CC spanXWrap(int span) {
    return spanX(span).wrap();
  }

  public static CC spanXGrowX(int span) {
    return spanX(span).growX();
  }

  public static CC spanXGrowXWrap(int span) {
    return spanXGrowX(span).wrap();
  }

  public static CC spanXGrowXMinWidthWrap(int span, int width) {
    return spanXGrowX(span).minWidth(Integer.toString(width)).wrap();
  }

  public static CC spanXGrowXPushXMinWidthWrap(int span, int width) {
    return spanXGrowX(span).pushX().minWidth(Integer.toString(width)).wrap();
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

  public static CC widthHeightWrap(int width, int height) {
    return widthHeight(width, height).wrap();
  }

  public static CC widthHeightAlignRightWrap(int width, int height) {
    return widthHeight(width, height).alignX(RIGHT).wrap();
  }

  public static CC alignYTop() {
    return new CC().alignY(TOP);
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

  private static String exact(int value) {
    return value + "!";
  }

  private MigConstraints() {}
}

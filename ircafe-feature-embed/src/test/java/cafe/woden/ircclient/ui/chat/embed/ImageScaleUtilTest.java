package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class ImageScaleUtilTest {

  @Test
  void scaleDownToWidthLeavesSmallOrInvalidRequestsUnchanged() {
    BufferedImage image = image(120, 80);

    assertDimensions(120, 80, ImageScaleUtil.scaleDownToWidth(image, 120));
    assertDimensions(120, 80, ImageScaleUtil.scaleDownToWidth(image, 0));
    assertNull(ImageScaleUtil.scaleDownToWidth(null, 80));
  }

  @Test
  void scaleDownToWidthPreservesAspectRatio() {
    BufferedImage scaled = ImageScaleUtil.scaleDownToWidth(image(200, 100), 50);

    assertEquals(50, scaled.getWidth());
    assertEquals(25, scaled.getHeight());
  }

  @Test
  void scaleDownToFitUsesTheTighterDimension() {
    BufferedImage widthLimited = ImageScaleUtil.scaleDownToFit(image(400, 200), 100, 400);
    assertEquals(100, widthLimited.getWidth());
    assertEquals(50, widthLimited.getHeight());

    BufferedImage heightLimited = ImageScaleUtil.scaleDownToFit(image(400, 200), 400, 50);
    assertEquals(100, heightLimited.getWidth());
    assertEquals(50, heightLimited.getHeight());
  }

  @Test
  void scaleDownToFitNeverUpscales() {
    BufferedImage image = image(80, 40);

    assertDimensions(80, 40, ImageScaleUtil.scaleDownToFit(image, 200, 200));
    assertDimensions(80, 40, ImageScaleUtil.scaleDownToFit(image, 0, 0));
  }

  private static void assertDimensions(int expectedWidth, int expectedHeight, BufferedImage image) {
    assertEquals(expectedWidth, image.getWidth());
    assertEquals(expectedHeight, image.getHeight());
  }

  private static BufferedImage image(int width, int height) {
    return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  }
}

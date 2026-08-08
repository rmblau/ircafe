package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImageDecodeUtilTest {

  @Test
  void detectsGifBySignatureOrExtension() {
    byte[] gifSignature = new byte[] {'G', 'I', 'F', '8', '9', 'a'};

    assertTrue(ImageDecodeUtil.looksLikeGif("https://example.test/image.bin", gifSignature));
    assertTrue(ImageDecodeUtil.looksLikeGif("https://example.test/image.GIF", null));
    assertFalse(ImageDecodeUtil.looksLikeGif("https://example.test/image.png", new byte[0]));
  }

  @Test
  void detectsWebpBySignatureOrExtension() {
    byte[] webpSignature = new byte[] {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};

    assertTrue(ImageDecodeUtil.looksLikeWebp("https://example.test/image.bin", webpSignature));
    assertTrue(ImageDecodeUtil.looksLikeWebp("https://example.test/image.WEBP", null));
    assertFalse(ImageDecodeUtil.looksLikeWebp("https://example.test/image.png", new byte[0]));
  }

  @Test
  void decodesStaticImageBytes() throws IOException {
    byte[] png = pngBytes();

    DecodedImage decoded = ImageDecodeUtil.decode("https://example.test/image.png", png);

    StaticImageDecoded staticImage = assertInstanceOf(StaticImageDecoded.class, decoded);
    assertTrue(staticImage.image().getWidth() > 0);
    assertTrue(staticImage.image().getHeight() > 0);
  }

  @Test
  void rejectsEmptyImageBytes() {
    IOException ex =
        assertThrows(
            IOException.class,
            () -> ImageDecodeUtil.decode("https://example.test/empty.png", new byte[0]));

    assertTrue(ex.getMessage().contains("Empty image bytes"));
  }

  private static byte[] pngBytes() throws IOException {
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(image, "png", out);
    return out.toByteArray();
  }
}

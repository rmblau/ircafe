package cafe.woden.ircclient.ui.chat.embed;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Root-independent HTML helpers shared by feature-owned link-preview resolvers. */
final class LinkPreviewHtmlSupport {

  private LinkPreviewHtmlSupport() {}

  static boolean looksLikeHtml(String contentType) {
    if (contentType == null) return false;
    String ct = contentType.toLowerCase();
    return ct.contains("text/html") || ct.contains("application/xhtml+xml");
  }

  static String readUpTo(InputStream in, int maxBytes) throws IOException {
    try (in) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(maxBytes, 16 * 1024));
      byte[] buf = new byte[8 * 1024];
      int remaining = maxBytes;
      while (remaining > 0) {
        int read = in.read(buf, 0, Math.min(buf.length, remaining));
        if (read < 0) break;
        out.write(buf, 0, read);
        remaining -= read;
      }
      return out.toString(StandardCharsets.UTF_8);
    }
  }

  static byte[] readUpToBytes(InputStream in, int maxBytes) {
    if (in == null || maxBytes <= 0) return new byte[0];
    try (in) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(maxBytes, 16 * 1024));
      byte[] buf = new byte[8 * 1024];
      int remaining = maxBytes;
      while (remaining > 0) {
        int n = in.read(buf, 0, Math.min(buf.length, remaining));
        if (n < 0) break;
        out.write(buf, 0, n);
        remaining -= n;
      }
      return out.toByteArray();
    } catch (IOException e) {
      return new byte[0];
    }
  }

  static byte[] readUpToBytes(String body, int maxBytes) {
    if (body == null || maxBytes <= 0) return new byte[0];
    byte[] all = body.getBytes(StandardCharsets.UTF_8);
    if (all.length <= maxBytes) return all;
    byte[] out = new byte[maxBytes];
    System.arraycopy(all, 0, out, 0, maxBytes);
    return out;
  }
}

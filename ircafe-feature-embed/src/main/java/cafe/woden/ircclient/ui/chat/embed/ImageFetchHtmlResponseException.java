package cafe.woden.ircclient.ui.chat.embed;

import java.io.IOException;

/** Signals that an image endpoint returned an HTML/block page instead of image bytes. */
public class ImageFetchHtmlResponseException extends IOException {

  private final String sampleText;
  private final int byteCount;

  public ImageFetchHtmlResponseException(String message, String sampleText, int byteCount) {
    super(message);
    this.sampleText = sampleText == null ? "" : sampleText;
    this.byteCount = byteCount;
  }

  public String sampleText() {
    return sampleText;
  }

  public int byteCount() {
    return byteCount;
  }
}

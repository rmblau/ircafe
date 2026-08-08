package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import java.util.Objects;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Root-owned Swing document boundary for applying resolved embed components to transcripts. */
@Component
@InterfaceLayer
@Lazy
public class EmbedDocumentApplicationService {

  private final ChatStyles styles;

  public EmbedDocumentApplicationService(ChatStyles styles) {
    this.styles = styles;
  }

  public record InsertResult(boolean inserted, int nextInsertAt) {
    static InsertResult failed(int insertAt) {
      return new InsertResult(false, Math.max(0, insertAt));
    }
  }

  public InsertResult insertComponent(
      StyledDocument doc, String rawUrl, java.awt.Component component, int insertAt) {
    if (doc == null || component == null) {
      return InsertResult.failed(insertAt);
    }

    String url = Objects.toString(rawUrl, "").trim();
    SimpleAttributeSet attributes = new SimpleAttributeSet(styles.message());
    attributes.addAttribute(ChatStyles.ATTR_URL, url);
    attributes.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_MESSAGE);
    StyleConstants.setComponent(attributes, component);

    int pos = Math.max(0, Math.min(insertAt, doc.getLength()));
    try {
      doc.insertString(pos, " ", attributes);
      pos += 1;
      doc.insertString(pos, "\n", styles.timestamp());
      pos += 1;
      return new InsertResult(true, pos);
    } catch (Exception ignored) {
      return InsertResult.failed(insertAt);
    }
  }
}

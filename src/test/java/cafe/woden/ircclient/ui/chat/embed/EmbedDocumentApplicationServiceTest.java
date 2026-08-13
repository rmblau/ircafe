package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import javax.swing.JLabel;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import org.junit.jupiter.api.Test;

class EmbedDocumentApplicationServiceTest {

  private final EmbedDocumentApplicationService service =
      new EmbedDocumentApplicationService(new ChatStyles(null));

  @Test
  void insertsComponentWithMessageUrlAttributesAndTimestampBreak() {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    JLabel component = new JLabel("preview");

    EmbedDocumentApplicationService.InsertResult result =
        service.insertComponent(doc, " https://example.test/image.png ", component, 50);

    assertTrue(result.inserted());
    assertEquals(2, result.nextInsertAt());
    assertEquals(2, doc.getLength());

    AttributeSet componentAttributes = doc.getCharacterElement(0).getAttributes();
    assertEquals(
        "https://example.test/image.png", componentAttributes.getAttribute(ChatStyles.ATTR_URL));
    assertEquals(ChatStyles.STYLE_MESSAGE, componentAttributes.getAttribute(ChatStyles.ATTR_STYLE));
    assertSame(component, StyleConstants.getComponent(componentAttributes));

    AttributeSet newlineAttributes = doc.getCharacterElement(1).getAttributes();
    assertEquals(ChatStyles.STYLE_TIMESTAMP, newlineAttributes.getAttribute(ChatStyles.ATTR_STYLE));
  }

  @Test
  void rejectsMissingDocumentOrComponentWithoutChangingInsertPosition() {
    DefaultStyledDocument doc = new DefaultStyledDocument();

    EmbedDocumentApplicationService.InsertResult missingComponent =
        service.insertComponent(doc, "https://example.test/image.png", null, 7);
    EmbedDocumentApplicationService.InsertResult missingDocument =
        service.insertComponent(null, "https://example.test/image.png", new JLabel("preview"), -3);

    assertFalse(missingComponent.inserted());
    assertEquals(7, missingComponent.nextInsertAt());
    assertFalse(missingDocument.inserted());
    assertEquals(0, missingDocument.nextInsertAt());
    assertEquals(0, doc.getLength());
  }
}

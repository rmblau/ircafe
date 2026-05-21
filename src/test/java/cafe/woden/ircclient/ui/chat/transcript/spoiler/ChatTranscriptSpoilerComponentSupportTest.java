package cafe.woden.ircclient.ui.chat.transcript.spoiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.fold.SpoilerMessageComponent;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;

class ChatTranscriptSpoilerComponentSupportTest {

  @Test
  void renderFromLabelAppendsDelimiterOnlyOnce() {
    TargetRef ref = new TargetRef("srv", "#chan");

    ChatTranscriptSpoilerComponentSupport.Context plainContext =
        new ChatTranscriptSpoilerComponentSupport.Context(null, null, (target, from) -> "Alice");
    ChatTranscriptSpoilerComponentSupport.Context colonContext =
        new ChatTranscriptSpoilerComponentSupport.Context(null, null, (target, from) -> "Alice:");
    ChatTranscriptSpoilerComponentSupport.Context blankContext =
        new ChatTranscriptSpoilerComponentSupport.Context(null, null, (target, from) -> "");

    assertEquals(
        "Alice: ",
        ChatTranscriptSpoilerComponentSupport.renderFromLabel(plainContext, ref, "alice"));
    assertEquals(
        "Alice: ",
        ChatTranscriptSpoilerComponentSupport.renderFromLabel(colonContext, ref, "alice"));
    assertEquals(
        "", ChatTranscriptSpoilerComponentSupport.renderFromLabel(blankContext, ref, "alice"));
  }

  @Test
  void createAppliesConfiguredFontAndNickColorToFromLabel() {
    UiSettings settings = mock(UiSettings.class);
    when(settings.chatFontFamily()).thenReturn("Monospaced");
    when(settings.chatFontSize()).thenReturn(17);
    UiSettingsBus settingsBus = mock(UiSettingsBus.class);
    when(settingsBus.get()).thenReturn(settings);

    NickColorService nickColors = mock(NickColorService.class);
    when(nickColors.enabled()).thenReturn(true);
    when(nickColors.colorForNick(eq("alice"), any(), any())).thenReturn(Color.RED);

    ChatTranscriptSpoilerComponentSupport.Context context =
        new ChatTranscriptSpoilerComponentSupport.Context(
            settingsBus, nickColors, (target, from) -> "Alice");

    SpoilerMessageComponent component =
        ChatTranscriptSpoilerComponentSupport.create(
            context, new TargetRef("srv", "#chan"), "alice", "[12:00] ");

    JLabel timestamp = findLabel(component, "[12:00] ");
    JLabel from = findLabel(component, "Alice: ");
    JLabel pill = findLabel(component, "soft ignored - click to reveal");

    assertNotNull(timestamp);
    assertNotNull(from);
    assertNotNull(pill);
    assertEquals(17, timestamp.getFont().getSize());
    assertEquals(17, from.getFont().getSize());
    assertTrue(from.getFont().isBold());
    assertEquals(Color.RED, from.getForeground());
    assertEquals(17, pill.getFont().getSize());
  }

  @Test
  void createLeavesFromColorUnchangedWhenNickColorsAreDisabled() {
    NickColorService nickColors = mock(NickColorService.class);
    when(nickColors.enabled()).thenReturn(false);

    ChatTranscriptSpoilerComponentSupport.Context context =
        new ChatTranscriptSpoilerComponentSupport.Context(
            null, nickColors, (target, from) -> "Alice");

    SpoilerMessageComponent component =
        ChatTranscriptSpoilerComponentSupport.create(
            context, new TargetRef("srv", "#chan"), "alice", "");

    JLabel from = findLabel(component, "Alice: ");
    assertNotNull(from);
    verify(nickColors, never()).colorForNick(eq("alice"), any(), any());
  }

  private static JLabel findLabel(Container root, String text) {
    for (JLabel label : findLabels(root)) {
      if (text.equals(label.getText())) {
        return label;
      }
    }
    return null;
  }

  private static List<JLabel> findLabels(Container root) {
    List<JLabel> labels = new ArrayList<>();
    if (root == null) {
      return labels;
    }
    for (Component child : root.getComponents()) {
      if (child instanceof JLabel label) {
        labels.add(label);
      }
      if (child instanceof Container nested) {
        labels.addAll(findLabels(nested));
      }
    }
    return labels;
  }
}

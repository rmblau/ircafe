package cafe.woden.ircclient.ui.settings.embeds;

import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.EmbedLoadPolicyDialog;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.awt.Window;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class EmbedsAndPreviewsPanelSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private EmbedsAndPreviewsPanelSupport() {}

  public static JButton buildAdvancedPolicyButton(
      Window owner,
      EmbedLoadPolicyDialog embedLoadPolicyDialog,
      AtomicReference<EmbedLoadPolicySnapshot> pendingEmbedLoadPolicy) {
    JButton advanced = new JButton(MESSAGES.text("preferences.embeds.advancedPolicy.button"));
    advanced.setToolTipText(
        MESSAGES.text("preferences.embeds.advancedPolicy.tooltip"));
    advanced.addActionListener(
        e -> {
          if (embedLoadPolicyDialog == null || pendingEmbedLoadPolicy == null) return;
          EmbedLoadPolicySnapshot current =
              pendingEmbedLoadPolicy.get() != null
                  ? pendingEmbedLoadPolicy.get()
                  : EmbedLoadPolicySnapshot.defaults();
          embedLoadPolicyDialog.open(owner, current).ifPresent(pendingEmbedLoadPolicy::set);
        });
    return advanced;
  }

  public static JPanel buildPanel(
      ImageEmbedControls image, LinkPreviewControls links, JButton advancedPolicyButton) {
    JPanel form =
        new JPanel(MigLayouts.twoColumnForm(12, 12, MigLayouts.rowGaps(10, 6, 10, 6, 10)));

    form.add(
        PreferencesUiSupport.tabTitle(MESSAGES.text("preferences.embeds.title")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    form.add(
        PreferencesUiSupport.sectionTitle(MESSAGES.text("preferences.embeds.section.inlineImages")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    form.add(
        new JLabel(MESSAGES.text("preferences.embeds.field.directImageLinks")),
        MigConstraints.alignYTop());
    form.add(image.panel, MigConstraints.growX());

    form.add(
        PreferencesUiSupport.sectionTitle(MESSAGES.text("preferences.embeds.section.linkPreviews")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    form.add(
        new JLabel(MESSAGES.text("preferences.embeds.field.openGraphCards")),
        MigConstraints.alignYTop());
    form.add(links.panel, MigConstraints.growX());

    form.add(
        PreferencesUiSupport.sectionTitle(MESSAGES.text("preferences.embeds.section.accessPolicy")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    form.add(
        new JLabel(MESSAGES.text("preferences.embeds.field.advancedMatchingRules")),
        MigConstraints.alignYTop());
    JPanel buttonRow = new JPanel(MigLayouts.insets0("[]", "[]"));
    buttonRow.setOpaque(false);
    if (advancedPolicyButton != null) {
      buttonRow.add(advancedPolicyButton);
    }
    form.add(buttonRow, MigConstraints.growX());

    return form;
  }
}

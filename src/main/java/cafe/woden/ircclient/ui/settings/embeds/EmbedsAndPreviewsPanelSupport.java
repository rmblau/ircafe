package cafe.woden.ircclient.ui.settings.embeds;

import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import cafe.woden.ircclient.ui.settings.EmbedLoadPolicyDialog;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import java.awt.Window;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public final class EmbedsAndPreviewsPanelSupport {
  private EmbedsAndPreviewsPanelSupport() {}

  public static JButton buildAdvancedPolicyButton(
      Window owner,
      EmbedLoadPolicyDialog embedLoadPolicyDialog,
      AtomicReference<EmbedLoadPolicySnapshot> pendingEmbedLoadPolicy) {
    JButton advanced = new JButton("Advanced Policy...");
    advanced.setToolTipText(
        "Open advanced allow/deny controls for embed/link loading by user, channel, URL/domain, and network.");
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
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_X_WRAP_2,
                MigLayoutConstraints.RIGHT_12_GROW_FILL,
                "[]10[]6[]10[]6[]10[]"));

    form.add(
        PreferencesUiSupport.tabTitle("Embeds & Previews"),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    form.add(
        PreferencesUiSupport.sectionTitle("Inline images"),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    form.add(new JLabel("Direct image links"), MigLayoutConstraints.ALIGN_Y_TOP);
    form.add(image.panel, MigLayoutConstraints.GROW_X);

    form.add(
        PreferencesUiSupport.sectionTitle("Link previews"),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    form.add(new JLabel("OpenGraph cards"), MigLayoutConstraints.ALIGN_Y_TOP);
    form.add(links.panel, MigLayoutConstraints.GROW_X);

    form.add(
        PreferencesUiSupport.sectionTitle("Access policy"),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    form.add(new JLabel("Advanced matching rules"), MigLayoutConstraints.ALIGN_Y_TOP);
    JPanel buttonRow = new JPanel(new MigLayout(MigLayoutConstraints.INSETS_0, "[]", "[]"));
    buttonRow.setOpaque(false);
    if (advancedPolicyButton != null) {
      buttonRow.add(advancedPolicyButton);
    }
    form.add(buttonRow, MigLayoutConstraints.GROW_X);

    return form;
  }
}

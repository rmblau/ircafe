package cafe.woden.ircclient.ui.settings.embeds;

import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import cafe.woden.ircclient.ui.settings.EmbedLoadPolicyDialog;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
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
                "insets 12, fillx, wrap 2", "[right]12[grow,fill]", "[]10[]6[]10[]6[]10[]"));

    form.add(PreferencesUiSupport.tabTitle("Embeds & Previews"), "span 2, growx, wmin 0, wrap");
    form.add(PreferencesUiSupport.sectionTitle("Inline images"), "span 2, growx, wmin 0, wrap");
    form.add(new JLabel("Direct image links"), "aligny top");
    form.add(image.panel, "growx");

    form.add(PreferencesUiSupport.sectionTitle("Link previews"), "span 2, growx, wmin 0, wrap");
    form.add(new JLabel("OpenGraph cards"), "aligny top");
    form.add(links.panel, "growx");

    form.add(PreferencesUiSupport.sectionTitle("Access policy"), "span 2, growx, wmin 0, wrap");
    form.add(new JLabel("Advanced matching rules"), "aligny top");
    JPanel buttonRow = new JPanel(new MigLayout("insets 0", "[]", "[]"));
    buttonRow.setOpaque(false);
    if (advancedPolicyButton != null) {
      buttonRow.add(advancedPolicyButton);
    }
    form.add(buttonRow, "growx");

    return form;
  }
}

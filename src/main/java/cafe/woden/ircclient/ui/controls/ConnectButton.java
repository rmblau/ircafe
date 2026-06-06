package cafe.woden.ircclient.ui.controls;

import cafe.woden.ircclient.ui.localization.UiMessages;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.PublishProcessor;
import javax.swing.JButton;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@InterfaceLayer
@Lazy
public class ConnectButton extends JButton {
  private final FlowableProcessor<Object> clicks = PublishProcessor.create().toSerialized();

  public ConnectButton() {
    super(UiMessages.bundledDefaults().text("common.button.connect"));
    addActionListener(e -> clicks.onNext(new Object()));
  }

  public Flowable<Object> onClick() {
    return clicks.onBackpressureLatest();
  }
}

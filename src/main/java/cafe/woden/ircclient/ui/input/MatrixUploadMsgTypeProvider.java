package cafe.woden.ircclient.ui.input;

import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** ServiceLoader-backed contribution point for Matrix upload msgtype inference. */
@InterfaceLayer
public interface MatrixUploadMsgTypeProvider {

  /** Returns extension-to-msgtype rules contributed by this provider. */
  List<MatrixUploadMsgTypeRule> uploadMsgTypeRules();
}

package cafe.woden.ircclient.dcc.api;

import io.reactivex.rxjava3.core.Flowable;
import java.util.List;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Read-only contract exported for querying per-server DCC transfer state. */
@PrimaryPort
@ApplicationLayer
public interface DccTransferQueryPort {

  Flowable<DccTransferChange> changes();

  List<DccTransferEntry> listAll(String serverId);
}

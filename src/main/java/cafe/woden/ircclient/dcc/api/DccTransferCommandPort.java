package cafe.woden.ircclient.dcc.api;

import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Mutable contract exported for recording and clearing per-server DCC transfer state. */
@PrimaryPort
@ApplicationLayer
public interface DccTransferCommandPort {

  void upsert(
      String serverId,
      String entryId,
      String nick,
      String kind,
      String status,
      String detail,
      Integer progressPercent,
      DccActionHint actionHint);

  void upsert(
      String serverId,
      String entryId,
      String nick,
      String kind,
      String status,
      String detail,
      String localPath,
      Integer progressPercent,
      DccActionHint actionHint);

  void remove(String serverId, String entryId);

  void clearServer(String serverId);
}

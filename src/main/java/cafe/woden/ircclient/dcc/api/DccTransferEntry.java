package cafe.woden.ircclient.dcc.api;

import java.time.Instant;

/** Immutable DTO describing one DCC transfer/chat row rendered by the UI. */
public record DccTransferEntry(
    String entryId,
    String serverId,
    String nick,
    String kind,
    String status,
    String detail,
    String localPath,
    Integer progressPercent,
    DccActionHint actionHint,
    Instant updatedAt) {}

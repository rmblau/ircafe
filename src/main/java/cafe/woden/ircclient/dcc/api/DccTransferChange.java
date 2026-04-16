package cafe.woden.ircclient.dcc.api;

/** Reactive change event emitted when per-server DCC state mutates. */
public record DccTransferChange(String serverId) {}

package cafe.woden.ircclient.config.runtime.server;

import java.util.Objects;

/** Pure normalization helpers for persisted per-server identity settings. */
final class RuntimeConfigServerIdentityCodec {

  private RuntimeConfigServerIdentityCodec() {}

  static String normalizeNick(Object nick) {
    return Objects.toString(nick, "").trim();
  }
}

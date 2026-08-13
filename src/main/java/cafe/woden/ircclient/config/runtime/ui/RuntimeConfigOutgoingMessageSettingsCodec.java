package cafe.woden.ircclient.config.runtime.ui;

import java.util.Objects;

/** Pure normalization helpers for persisted outgoing message presentation settings. */
final class RuntimeConfigOutgoingMessageSettingsCodec {

  private RuntimeConfigOutgoingMessageSettingsCodec() {}

  static String normalizeClientLineColor(String hex) {
    return Objects.toString(hex, "").trim();
  }
}

package cafe.woden.ircclient.notify.api.sound;

import java.util.Objects;

/** Captured failure from a custom sound playback provider attempt. */
public record CustomSoundPlaybackProviderFailure(String providerClassName, Exception exception) {

  public CustomSoundPlaybackProviderFailure {
    providerClassName = Objects.toString(providerClassName, "").trim();
  }
}

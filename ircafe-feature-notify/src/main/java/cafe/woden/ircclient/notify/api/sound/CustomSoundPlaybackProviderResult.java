package cafe.woden.ircclient.notify.api.sound;

import java.util.List;
import java.util.Objects;

/** Result of trying custom sound playback providers. */
public record CustomSoundPlaybackProviderResult(
    boolean handled, boolean handledWhileFresh, List<CustomSoundPlaybackProviderFailure> failures) {

  public CustomSoundPlaybackProviderResult {
    if (!handled) {
      handledWhileFresh = false;
    }
    failures =
        Objects.requireNonNullElse(failures, List.<CustomSoundPlaybackProviderFailure>of()).stream()
            .filter(Objects::nonNull)
            .toList();
  }

  public static CustomSoundPlaybackProviderResult unhandled(
      List<CustomSoundPlaybackProviderFailure> failures) {
    return new CustomSoundPlaybackProviderResult(false, false, failures);
  }

  public static CustomSoundPlaybackProviderResult handled(
      boolean handledWhileFresh, List<CustomSoundPlaybackProviderFailure> failures) {
    return new CustomSoundPlaybackProviderResult(true, handledWhileFresh, failures);
  }
}

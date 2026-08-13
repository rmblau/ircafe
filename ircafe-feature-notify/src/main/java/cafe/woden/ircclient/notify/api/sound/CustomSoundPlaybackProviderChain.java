package cafe.woden.ircclient.notify.api.sound;

import cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/** Feature-owned execution chain for custom sound playback providers. */
public final class CustomSoundPlaybackProviderChain {
  private static final BooleanSupplier NEVER_STALE = () -> false;

  private CustomSoundPlaybackProviderChain() {}

  public static CustomSoundPlaybackProviderResult play(
      Path path,
      List<? extends CustomSoundPlaybackProvider> providers,
      BooleanSupplier stalePlayback) {
    if (path == null || providers == null || providers.isEmpty()) {
      return CustomSoundPlaybackProviderResult.unhandled(List.of());
    }

    BooleanSupplier stale = stalePlayback != null ? stalePlayback : NEVER_STALE;
    ArrayList<CustomSoundPlaybackProviderFailure> failures = new ArrayList<>();
    for (CustomSoundPlaybackProvider provider : providers) {
      if (provider == null || stale.getAsBoolean()) continue;
      try {
        if (provider.playCustomSound(path)) {
          return CustomSoundPlaybackProviderResult.handled(!stale.getAsBoolean(), failures);
        }
      } catch (Exception e) {
        failures.add(new CustomSoundPlaybackProviderFailure(provider.getClass().getName(), e));
      }
    }

    return CustomSoundPlaybackProviderResult.unhandled(failures);
  }
}

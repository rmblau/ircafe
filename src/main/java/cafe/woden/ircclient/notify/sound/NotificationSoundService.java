package cafe.woden.ircclient.notify.sound;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.execution.ExecutorConfig;
import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.notify.api.NotificationSoundPort;
import cafe.woden.ircclient.notify.api.sound.CustomSoundPlaybackProviderChain;
import cafe.woden.ircclient.notify.api.sound.CustomSoundPlaybackProviderFailure;
import cafe.woden.ircclient.notify.api.sound.CustomSoundPlaybackProviderResult;
import cafe.woden.ircclient.notify.api.sound.CustomSoundPluginProviders;
import cafe.woden.ircclient.notify.api.sound.NotificationSoundClipPlayback;
import cafe.woden.ircclient.notify.api.sound.NotificationSoundPathResolver;
import cafe.woden.ircclient.notify.api.sound.NotificationSoundPlaybackPlan;
import cafe.woden.ircclient.notify.api.sound.NotificationSoundPlaybackPlanner;
import cafe.woden.ircclient.notify.api.sound.NotificationSoundPlaybackPolicy;
import cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider;
import jakarta.annotation.PreDestroy;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@ApplicationLayer
public class NotificationSoundService implements NotificationSoundPort {

  private static final Logger log = LoggerFactory.getLogger(NotificationSoundService.class);

  private final ExecutorService executor;

  private final NotificationSoundSettingsBus settingsBus;
  private final RuntimeConfigPathPort runtimeConfig;
  private final List<CustomSoundPlaybackProvider> customPlaybackProviders;
  private final PropertyChangeListener settingsListener;

  private final AtomicReference<Instant> lastPlayed = new AtomicReference<>(Instant.EPOCH);
  private final AtomicLong previewRequestSeq = new AtomicLong(0L);

  /** Global sound enable toggle (Phase 2: defaults to enabled, UI/persistence later). */
  private volatile boolean enabled = true;

  /** Single globally selected sound (Phase 2). */
  private volatile BuiltInSound selectedSound = BuiltInSound.NOTIF_1;

  /** If true, play a user-provided sound file from the runtime config directory. */
  private volatile boolean useCustom = false;

  /** Resolved absolute path for the custom sound file (when enabled). */
  private volatile Path customSoundPath;

  public NotificationSoundService(
      NotificationSoundSettingsBus settingsBus,
      RuntimeConfigPathPort runtimeConfig,
      @Qualifier(ExecutorConfig.NOTIFICATION_SOUND_EXECUTOR) ExecutorService executor) {
    this(settingsBus, runtimeConfig, executor, (InstalledPluginsPort) null);
  }

  @Autowired
  public NotificationSoundService(
      NotificationSoundSettingsBus settingsBus,
      RuntimeConfigPathPort runtimeConfig,
      @Qualifier(ExecutorConfig.NOTIFICATION_SOUND_EXECUTOR) ExecutorService executor,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(settingsBus, runtimeConfig, executor, resolveInstalledPlugins(installedPluginsProvider));
  }

  NotificationSoundService(
      NotificationSoundSettingsBus settingsBus,
      RuntimeConfigPathPort runtimeConfig,
      ExecutorService executor,
      InstalledPluginsPort installedPlugins) {
    this.settingsBus = settingsBus;
    this.runtimeConfig = runtimeConfig;
    this.executor = executor;
    this.customPlaybackProviders = CustomSoundPluginProviders.playbackProviders(installedPlugins);

    NotificationSoundSettings seed = settingsBus != null ? settingsBus.get() : null;
    applySettings(seed);

    this.settingsListener =
        evt -> {
          if (evt == null) return;
          if (!NotificationSoundSettingsBus.PROP_NOTIFICATION_SOUND_SETTINGS.equals(
              evt.getPropertyName())) return;
          Object v = evt.getNewValue();
          if (v instanceof NotificationSoundSettings s) {
            applySettings(s);
          }
        };

    if (settingsBus != null) {
      settingsBus.addListener(settingsListener);
    }
  }

  /** Play the currently selected built-in notification sound. */
  public void play() {
    Path path = customSoundPath;
    NotificationSoundPlaybackPlan plan =
        NotificationSoundPlaybackPlanner.planSelected(
            enabled, useCustom, fileExists(path), resourcePath(selectedSound));
    playPlan(plan, path, false, 0L);
  }

  /** Play a one-off sound override for a specific notification event. */
  public void playOverride(String soundId, boolean useCustom, String customPath) {
    Path overridePath = useCustom ? resolveCustomPath(customPath) : null;
    BuiltInSound override = BuiltInSound.fromId(soundId);
    NotificationSoundPlaybackPlan plan =
        NotificationSoundPlaybackPlanner.planOverride(
            enabled, useCustom, fileExists(overridePath), resourcePath(override));
    playPlan(plan, overridePath, false, 0L);
  }

  /** Play the given sound for preview/testing, even if sounds are disabled. */
  public void preview(BuiltInSound sound) {
    NotificationSoundPlaybackPlan plan =
        NotificationSoundPlaybackPlanner.planBuiltInPreview(resourcePath(sound));
    if (plan.skipPlayback()) return;
    long seq = previewRequestSeq.incrementAndGet();
    playPlan(plan, null, true, seq);
  }

  /** Play the configured custom file (if any) for preview/testing. */
  public void previewCustom() {
    Path p = this.customSoundPath;
    NotificationSoundPlaybackPlan plan =
        NotificationSoundPlaybackPlanner.planCustomPreview(fileExists(p));
    if (plan.skipPlayback()) return;
    long seq = previewRequestSeq.incrementAndGet();
    playPlan(plan, p, true, seq);
  }

  /** Play a specific custom file (relative to the runtime config directory) for preview/testing. */
  public void previewCustom(String relativePath) {
    Path p = resolveCustomPath(relativePath);
    NotificationSoundPlaybackPlan plan =
        NotificationSoundPlaybackPlanner.planCustomPreview(fileExists(p));
    if (plan.skipPlayback()) return;
    long seq = previewRequestSeq.incrementAndGet();
    playPlan(plan, p, true, seq);
  }

  private void applySettings(NotificationSoundSettings s) {
    if (s == null) {
      this.enabled = true;
      this.selectedSound = BuiltInSound.NOTIF_1;
      this.useCustom = false;
      this.customSoundPath = null;
      return;
    }
    this.enabled = s.enabled();
    this.selectedSound = BuiltInSound.fromId(s.soundId());

    this.useCustom = s.useCustom();
    this.customSoundPath = resolveCustomPath(s.customPath());
  }

  private Path resolveCustomPath(String relativePath) {
    try {
      Path cfg = runtimeConfig != null ? runtimeConfig.runtimeConfigPath() : null;
      return NotificationSoundPathResolver.resolveCustomSoundPath(cfg, relativePath);
    } catch (Exception e) {
      return null;
    }
  }

  private void playPlan(
      NotificationSoundPlaybackPlan plan, Path customPath, boolean bypassLimiter, long previewSeq) {
    if (plan == null || plan.skipPlayback()) {
      return;
    }
    if (plan.usesCustomFile()) {
      playFile(customPath, bypassLimiter, previewSeq);
      return;
    }
    if (plan.usesBuiltInResource()) {
      playResource(plan.resourcePath(), bypassLimiter, previewSeq);
    }
  }

  private static String resourcePath(BuiltInSound sound) {
    return sound == null ? null : sound.resourcePath();
  }

  private static boolean fileExists(Path path) {
    return path != null && Files.exists(path);
  }

  private void playResource(String resourcePath, boolean bypassLimiter, long previewSeq) {
    executor.submit(
        () -> {
          if (!shouldStartPlayback(bypassLimiter, previewSeq)) {
            return;
          }

          try {
            URL resource = getClass().getClassLoader().getResource(resourcePath);

            if (resource == null) {
              log.debug("Sound resource not found: {}", resourcePath);
              return;
            }

            try (AudioInputStream originalStream =
                AudioSystem.getAudioInputStream(new BufferedInputStream(resource.openStream()))) {

              if (isStalePreview(previewSeq)) {
                return;
              }
              NotificationSoundClipPlayback.play(originalStream);
              lastPlayed.set(Instant.now());
            }

          } catch (Exception e) {
            // Don't let audio failures crash or spam logs; debug is enough.
            log.debug("Failed to play notification sound: {}", resourcePath, e);
          }
        });
  }

  private void playFile(Path path, boolean bypassLimiter, long previewSeq) {
    executor.submit(
        () -> {
          if (!shouldStartPlayback(bypassLimiter, previewSeq)) {
            return;
          }

          if (path == null || !Files.exists(path)) {
            return;
          }

          if (tryPluginPlayback(path, previewSeq)) {
            return;
          }

          try (AudioInputStream originalStream = AudioSystem.getAudioInputStream(path.toFile())) {

            if (isStalePreview(previewSeq)) {
              return;
            }
            NotificationSoundClipPlayback.play(originalStream);
            lastPlayed.set(Instant.now());

          } catch (Exception e) {
            log.debug("Failed to play custom notification sound: {}", path, e);
          }
        });
  }

  private boolean tryPluginPlayback(Path path, long previewSeq) {
    CustomSoundPlaybackProviderResult result =
        CustomSoundPlaybackProviderChain.play(
            path, customPlaybackProviders, () -> isStalePreview(previewSeq));
    for (CustomSoundPlaybackProviderFailure failure : result.failures()) {
      log.debug(
          "Custom sound playback provider failed: {}",
          failure.providerClassName(),
          failure.exception());
    }
    if (result.handled()) {
      if (result.handledWhileFresh()) {
        lastPlayed.set(Instant.now());
      }
      return true;
    }
    return false;
  }

  private static InstalledPluginsPort resolveInstalledPlugins(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    return installedPluginsProvider == null ? null : installedPluginsProvider.getIfAvailable();
  }

  private boolean isStalePreview(long previewSeq) {
    return NotificationSoundPlaybackPolicy.isStalePreview(previewSeq, previewRequestSeq.get());
  }

  private boolean shouldStartPlayback(boolean bypassLimiter, long previewSeq) {
    return NotificationSoundPlaybackPolicy.shouldStartPlayback(
        previewSeq, previewRequestSeq.get(), bypassLimiter, lastPlayed.get(), Instant.now());
  }

  @PreDestroy
  public void shutdown() {
    try {
      if (settingsBus != null && settingsListener != null) {
        settingsBus.removeListener(settingsListener);
      }
    } catch (Exception ignored) {
    }
  }
}

package cafe.woden.ircclient.notify.api.sound;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/** Feature-owned Java Sound clip fallback playback. */
public final class NotificationSoundClipPlayback {
  private NotificationSoundClipPlayback() {}

  public static void play(AudioInputStream originalStream) throws Exception {
    AudioInputStream decodedStream = originalStream;
    AudioFormat baseFormat = originalStream.getFormat();
    if (requiresDecode(baseFormat)) {
      decodedStream = AudioSystem.getAudioInputStream(decodedFormatFor(baseFormat), originalStream);
    }

    try (AudioInputStream toPlay = decodedStream) {
      Clip clip = AudioSystem.getClip();
      CountDownLatch finished = new CountDownLatch(1);
      LineListener listener =
          event -> {
            if (event == null) return;
            LineEvent.Type type = event.getType();
            if (type == LineEvent.Type.STOP || type == LineEvent.Type.CLOSE) {
              finished.countDown();
            }
          };

      try {
        clip.addLineListener(listener);
        clip.open(toPlay);
        clip.setFramePosition(0);
        clip.start();

        long waitMs = NotificationSoundPlaybackPolicy.clipWaitMillis(clip.getMicrosecondLength());
        try {
          finished.await(waitMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }
      } finally {
        closeClip(clip, listener);
      }
    }
  }

  static boolean requiresDecode(AudioFormat format) {
    return format != null
        && (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED
            || format.getSampleSizeInBits() != 16);
  }

  static AudioFormat decodedFormatFor(AudioFormat baseFormat) {
    return new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        baseFormat.getSampleRate(),
        16,
        baseFormat.getChannels(),
        baseFormat.getChannels() * 2,
        baseFormat.getSampleRate(),
        false);
  }

  private static void closeClip(Clip clip, LineListener listener) {
    try {
      clip.removeLineListener(listener);
    } catch (Exception ignored) {
    }
    try {
      if (clip.isRunning()) clip.stop();
    } catch (Exception ignored) {
    }
    try {
      if (clip.isOpen()) clip.close();
    } catch (Exception ignored) {
    }
  }
}

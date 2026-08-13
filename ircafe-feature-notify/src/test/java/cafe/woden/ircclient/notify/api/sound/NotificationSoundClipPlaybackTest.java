package cafe.woden.ircclient.notify.api.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.sound.sampled.AudioFormat;
import org.junit.jupiter.api.Test;

class NotificationSoundClipPlaybackTest {

  @Test
  void acceptsSignedSixteenBitPcmWithoutDecode() {
    AudioFormat format =
        new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44_100f, 16, 2, 4, 44_100f, false);

    assertFalse(NotificationSoundClipPlayback.requiresDecode(format));
  }

  @Test
  void decodesUnsignedOrNonSixteenBitAudio() {
    AudioFormat unsigned =
        new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 44_100f, 16, 2, 4, 44_100f, false);
    AudioFormat eightBit =
        new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44_100f, 8, 2, 2, 44_100f, false);

    assertTrue(NotificationSoundClipPlayback.requiresDecode(unsigned));
    assertTrue(NotificationSoundClipPlayback.requiresDecode(eightBit));
  }

  @Test
  void buildsLittleEndianSignedSixteenBitDecodeFormat() {
    AudioFormat base =
        new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 22_050f, 8, 1, 1, 22_050f, true);

    AudioFormat decoded = NotificationSoundClipPlayback.decodedFormatFor(base);

    assertEquals(AudioFormat.Encoding.PCM_SIGNED, decoded.getEncoding());
    assertEquals(22_050f, decoded.getSampleRate());
    assertEquals(16, decoded.getSampleSizeInBits());
    assertEquals(1, decoded.getChannels());
    assertEquals(2, decoded.getFrameSize());
    assertEquals(22_050f, decoded.getFrameRate());
    assertFalse(decoded.isBigEndian());
  }
}

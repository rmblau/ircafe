package cafe.woden.ircclient.irc.ircv3;

import java.util.concurrent.atomic.AtomicBoolean;

/** One-shot compatible/incompatible observations for IRCafe's expected WHOX reply schema. */
public final class Ircv3WhoxSchemaTracker {
  private final AtomicBoolean compatible = new AtomicBoolean(true);
  private final AtomicBoolean compatibleEmitted = new AtomicBoolean(false);
  private final AtomicBoolean incompatibleEmitted = new AtomicBoolean(false);

  public boolean observeCompatible() {
    if (!compatibleEmitted.compareAndSet(false, true)) return false;
    compatible.set(true);
    return true;
  }

  public boolean observeIncompatible() {
    if (!incompatibleEmitted.compareAndSet(false, true)) return false;
    compatible.set(false);
    return true;
  }

  public boolean compatible() {
    return compatible.get();
  }
}

package cafe.woden.ircclient.irc.pircbotx.capability;

import cafe.woden.ircclient.irc.ircv3.Ircv3CapabilityRequestBatchSession;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.CapHandler;
import org.pircbotx.exception.CAPException;

/**
 * Requests multiple optional IRCv3 capabilities in one CAP REQ line.
 *
 * <p>The transport-independent offer matching and pending-resolution state live in {@link
 * Ircv3CapabilityRequestBatchSession}; this class only adapts that policy to PircBotX.
 */
public final class BatchedEnableCapHandler implements CapHandler {

  private final Ircv3CapabilityRequestBatchSession session;

  public BatchedEnableCapHandler(List<String> desiredCaps) {
    this.session = new Ircv3CapabilityRequestBatchSession(desiredCaps);
  }

  @Override
  public boolean handleLS(PircBotX bot, ImmutableList<String> serverCaps) throws CAPException {
    Ircv3CapabilityRequestBatchSession.LsDecision decision = session.observeLs(serverCaps);
    if (!decision.capabilitiesToRequest().isEmpty()) {
      bot.sendCAP().request(decision.capabilitiesToRequest().toArray(new String[0]));
    }
    return decision.finished();
  }

  @Override
  public boolean handleACK(PircBotX bot, ImmutableList<String> caps) throws CAPException {
    return session.resolve(caps);
  }

  @Override
  public boolean handleNAK(PircBotX bot, ImmutableList<String> caps) throws CAPException {
    return session.resolve(caps);
  }

  @Override
  public boolean handleUnknown(PircBotX bot, String line) throws CAPException {
    return false;
  }

  public boolean isPending(String capability) {
    return session.isPending(capability);
  }

  @Override
  public String toString() {
    return "BatchedEnableCapHandler(desiredCaps=" + session.desiredCapabilities() + ")";
  }
}

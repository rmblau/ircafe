package cafe.woden.ircclient.irc.pircbotx.capability;

import static cafe.woden.ircclient.util.Ircv3CapabilityNames.SASL;

import cafe.woden.ircclient.irc.ircv3.Ircv3SaslException;
import cafe.woden.ircclient.irc.ircv3.Ircv3SaslRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3SaslSession;
import cafe.woden.ircclient.irc.ircv3.Ircv3SaslSessionUpdate;
import com.google.common.collect.ImmutableList;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.CapHandler;
import org.pircbotx.exception.CAPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** PircBotX transport adapter for the focused IRCv3 SASL session policy. */
public final class MultiSaslCapHandler implements CapHandler {

  private static final Logger log = LoggerFactory.getLogger(MultiSaslCapHandler.class);

  private final Ircv3SaslSession session;
  private final Ircv3SaslRuntimeSupport runtimeSupport;

  public MultiSaslCapHandler(
      String username,
      String secret,
      String mechanism,
      boolean disconnectOnFailure,
      Ircv3SaslRuntimeSupport runtimeSupport) {
    this.session = new Ircv3SaslSession(username, secret, mechanism, disconnectOnFailure);
    this.runtimeSupport = java.util.Objects.requireNonNull(runtimeSupport, "runtimeSupport");
  }

  @Override
  public boolean handleLS(PircBotX bot, ImmutableList<String> serverCaps) throws CAPException {
    return apply(bot, session.onCapabilityList(runtimeSupport.capabilityList(serverCaps)));
  }

  @Override
  public boolean handleACK(PircBotX bot, ImmutableList<String> caps) throws CAPException {
    return apply(bot, session.onCapabilityAck(runtimeSupport.capabilityAck(caps)));
  }

  @Override
  public boolean handleNAK(PircBotX bot, ImmutableList<String> caps) throws CAPException {
    return apply(bot, session.onCapabilityNak(runtimeSupport.capabilityNak(caps)));
  }

  @Override
  public boolean handleUnknown(PircBotX bot, String line) throws CAPException {
    try {
      return apply(bot, session.onParsedLine(runtimeSupport.serverMessage(line)));
    } catch (Ircv3SaslException error) {
      throw toCapException(error);
    }
  }

  private boolean apply(PircBotX bot, Ircv3SaslSessionUpdate update) throws CAPException {
    if (update.requestCapability()) {
      bot.sendCAP().request(SASL);
      log.debug("[SASL] Requested capability sasl");
    }
    if (update.startedMechanism() != null) {
      log.info("[SASL] Starting SASL mechanism {}", update.startedMechanism());
    }
    if (update.successNumeric() != null) {
      log.info("[SASL] Authentication successful ({}).", update.successNumeric());
    }

    Ircv3SaslSessionUpdate.Failure failure = update.failure();
    if (failure != null) {
      log.warn("[SASL] {} (disconnectOnFailure={})", failure.reason(), failure.disconnect());
      if (failure.disconnect()) {
        throw new CAPException(CAPException.Reason.SASL_FAILED, failure.reason());
      }
    }

    for (String rawLine : update.rawLines()) {
      if (failure != null) {
        try {
          bot.sendRaw().rawLine(rawLine);
        } catch (Exception ignored) {
        }
      } else {
        bot.sendRaw().rawLine(rawLine);
      }
    }
    return update.complete();
  }

  private static CAPException toCapException(Ircv3SaslException error) {
    CAPException.Reason reason =
        switch (error.reason()) {
          case SASL_FAILED -> CAPException.Reason.SASL_FAILED;
          case UNSUPPORTED_MECHANISM -> CAPException.Reason.UNSUPPORTED_CAPABILITY;
          case OTHER -> CAPException.Reason.OTHER;
        };
    return new CAPException(reason, error.getMessage(), error);
  }
}

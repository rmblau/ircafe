package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import java.util.Objects;
import java.util.Optional;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Validates runtime-provider account-tag observations before identity-state tracking. */
@Component
@InfrastructureLayer
public final class Ircv3AccountTagRuntimeSupport {

  private static final int MAX_NICK_LENGTH = 512;
  private static final int MAX_ACCOUNT_LENGTH = 512;

  private final Ircv3InboundTagSignalRuntimeCatalog inboundTagCatalog;

  @Autowired
  public Ircv3AccountTagRuntimeSupport(
      Ircv3InboundTagSignalRuntimeCatalog inboundTagCatalog) {
    this.inboundTagCatalog = Objects.requireNonNull(inboundTagCatalog, "inboundTagCatalog");
  }

  public Optional<Observation> observe(Ircv3InboundTagRequest request) {
    if (request == null || !request.tags().containsKey("account")) {
      return Optional.empty();
    }
    String requestedNick = normalizeToken(request.sourceNick(), MAX_NICK_LENGTH);
    if (requestedNick.isEmpty()) {
      return Optional.empty();
    }

    Observation accepted = null;
    for (Ircv3InboundTagSignal signal :
        inboundTagCatalog.parse(Ircv3InboundTagOperation.ACCOUNT_TAG, request)) {
      if (signal.type() != Ircv3InboundTagSignalType.ACCOUNT_TAG) {
        continue;
      }
      String nick = normalizeToken(signal.primaryValue(), MAX_NICK_LENGTH);
      String rawAccount = normalizeAccount(signal.secondaryValue());
      if (!requestedNick.equals(nick) || rawAccount == null || accepted != null) {
        return Optional.empty();
      }
      accepted = new Observation(nick, rawAccount);
    }
    return Optional.ofNullable(accepted);
  }

  private static String normalizeToken(String raw, int maxLength) {
    String value = Objects.toString(raw, "").trim();
    if (value.isEmpty()
        || value.length() > maxLength
        || containsControl(value)
        || value.chars().anyMatch(Character::isWhitespace)) {
      return "";
    }
    return value;
  }

  private static String normalizeAccount(String raw) {
    String account = Objects.toString(raw, "").trim();
    if (account.length() > MAX_ACCOUNT_LENGTH || containsControl(account)) {
      return null;
    }
    return account;
  }

  private static boolean containsControl(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (Character.isISOControl(value.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  public record Observation(String nick, String rawAccount) {
    public Observation {
      nick = Objects.requireNonNull(nick, "nick");
      rawAccount = Objects.requireNonNull(rawAccount, "rawAccount");
    }
  }
}

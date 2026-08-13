package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Built-in runtime provider for transport-neutral IRC identity numerics. */
@AutoService(Ircv3InboundCommandSignalProvider.class)
public final class Ircv3UserIdentityRuntimeProvider implements Ircv3InboundCommandSignalProvider {

  private static final String PROVIDER_ID = "user-identity";
  private static final String IRCAFE_WHOX_TOKEN = "1";

  @Override
  public String providerId() {
    return PROVIDER_ID;
  }

  @Override
  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
    return Set.of(
        Ircv3InboundCommandOperation.USERHOST,
        Ircv3InboundCommandOperation.WHOIS_AWAY,
        Ircv3InboundCommandOperation.WHOIS_ACCOUNT,
        Ircv3InboundCommandOperation.WHOIS_END,
        Ircv3InboundCommandOperation.WHOIS_USER,
        Ircv3InboundCommandOperation.WHO,
        Ircv3InboundCommandOperation.WHOX,
        Ircv3InboundCommandOperation.ISUPPORT_WHOX);
  }

  @Override
  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (operation == null || request == null) {
      return List.of();
    }
    return switch (operation) {
      case USERHOST -> parseUserhost(request.rawLine());
      case WHOIS_AWAY -> parseWhoisAway(request.rawLine());
      case WHOIS_ACCOUNT -> parseWhoisAccount(request.rawLine());
      case WHOIS_END -> parseWhoisEnd(request.rawLine());
      case WHOIS_USER -> parseWhoisUser(request.rawLine());
      case WHO -> parseWho(request.rawLine());
      case WHOX -> parseWhox(request.rawLine());
      case ISUPPORT_WHOX -> parseIsupportWhox(request.rawLine());
      default -> List.of();
    };
  }

  private static List<Ircv3InboundCommandSignal> parseIsupportWhox(String rawLine) {
    Boolean supported = Ircv3WhoUserhostParser.parseRpl005IsupportWhoxSupport(rawLine);
    return supported == null
        ? List.of()
        : List.of(new Ircv3InboundCommandSignal.WhoxSupportObserved(supported));
  }

  private static List<Ircv3InboundCommandSignal> parseUserhost(String rawLine) {
    List<Ircv3WhoUserhostParser.UserhostEntry> entries =
        Ircv3WhoUserhostParser.parseRpl302Userhost(rawLine);
    if (entries == null || entries.isEmpty()) {
      return List.of();
    }
    ArrayList<Ircv3InboundCommandSignal> signals = new ArrayList<>(entries.size() * 2);
    for (Ircv3WhoUserhostParser.UserhostEntry entry : entries) {
      if (entry == null) {
        continue;
      }
      signals.add(new Ircv3InboundCommandSignal.HostmaskObserved(entry.nick(), entry.hostmask()));
      if (entry.awayState() == Ircv3WhoUserhostParser.AwayState.AWAY) {
        signals.add(new Ircv3InboundCommandSignal.UserAwayObserved(entry.nick(), true, null));
      } else if (entry.awayState() == Ircv3WhoUserhostParser.AwayState.HERE) {
        signals.add(new Ircv3InboundCommandSignal.UserAwayObserved(entry.nick(), false, null));
      }
    }
    return List.copyOf(signals);
  }

  private static List<Ircv3InboundCommandSignal> parseWhoisAway(String rawLine) {
    Ircv3WhoisParser.ParsedWhoisAway away = Ircv3WhoisParser.parseRpl301WhoisAway(rawLine);
    if (away == null) {
      return List.of();
    }
    return List.of(
        new Ircv3InboundCommandSignal.UserAwayObserved(away.nick(), true, away.message()));
  }

  private static List<Ircv3InboundCommandSignal> parseWhoisAccount(String rawLine) {
    Ircv3WhoisParser.ParsedWhoisAccount account = Ircv3WhoisParser.parseRpl330WhoisAccount(rawLine);
    if (account == null) {
      return List.of();
    }
    return List.of(
        new Ircv3InboundCommandSignal.AccountObserved(
            account.nick(), Ircv3InboundCommandSignal.AccountState.LOGGED_IN, account.account()));
  }

  private static List<Ircv3InboundCommandSignal> parseWhoisEnd(String rawLine) {
    String nick = Ircv3WhoisParser.parseRpl318EndOfWhoisNick(rawLine);
    if (nick == null || nick.isBlank()) {
      return List.of();
    }
    return List.of(new Ircv3InboundCommandSignal.WhoisEndedObserved(nick));
  }

  private static List<Ircv3InboundCommandSignal> parseWhoisUser(String rawLine) {
    Ircv3WhoisParser.ParsedWhoisUser user = Ircv3WhoisParser.parseRpl311WhoisUser(rawLine);
    if (user == null) {
      user = Ircv3WhoisParser.parseRpl314WhowasUser(rawLine);
    }
    if (user == null) {
      return List.of();
    }
    return List.of(
        new Ircv3InboundCommandSignal.HostmaskObserved(
            user.nick(), user.nick() + "!" + user.user() + "@" + user.host()));
  }

  private static List<Ircv3InboundCommandSignal> parseWho(String rawLine) {
    Ircv3WhoUserhostParser.ParsedWhoReply who = Ircv3WhoUserhostParser.parseRpl352WhoReply(rawLine);
    if (who == null) {
      return List.of();
    }
    ArrayList<Ircv3InboundCommandSignal> signals = new ArrayList<>(2);
    signals.add(
        new Ircv3InboundCommandSignal.ChannelHostmaskObserved(
            who.channel(), who.nick(), who.nick() + "!" + who.user() + "@" + who.host()));
    addAwayFromFlags(signals, who.nick(), who.flags());
    return List.copyOf(signals);
  }

  private static List<Ircv3InboundCommandSignal> parseWhox(String rawLine) {
    Ircv3WhoUserhostParser.ParsedWhoxTcuhnaf strict =
        Ircv3WhoUserhostParser.parseRpl354WhoxTcuhnaf(rawLine, IRCAFE_WHOX_TOKEN);
    if (strict != null) {
      ArrayList<Ircv3InboundCommandSignal> signals = new ArrayList<>(4);
      signals.add(new Ircv3InboundCommandSignal.WhoxSchemaObserved(true, "strict-parse-ok"));
      signals.add(
          new Ircv3InboundCommandSignal.ChannelHostmaskObserved(
              strict.channel(),
              strict.nick(),
              strict.nick() + "!" + strict.user() + "@" + strict.host()));
      addAwayFromFlags(signals, strict.nick(), strict.flags());
      addAccount(signals, strict.nick(), strict.account());
      return List.copyOf(signals);
    }

    ArrayList<Ircv3InboundCommandSignal> signals = new ArrayList<>(2);
    if (Ircv3WhoUserhostParser.seemsRpl354WhoxWithToken(rawLine, IRCAFE_WHOX_TOKEN)) {
      signals.add(new Ircv3InboundCommandSignal.WhoxSchemaObserved(false, "strict-parse-failed"));
    }
    Ircv3WhoUserhostParser.ParsedWhoxReply fallback =
        Ircv3WhoUserhostParser.parseRpl354WhoxReply(rawLine);
    if (fallback != null) {
      signals.add(
          new Ircv3InboundCommandSignal.ChannelHostmaskObserved(
              fallback.channel(),
              fallback.nick(),
              fallback.nick() + "!" + fallback.user() + "@" + fallback.host()));
    }
    return List.copyOf(signals);
  }

  private static void addAwayFromFlags(
      List<Ircv3InboundCommandSignal> signals, String nick, String flags) {
    if (flags == null) {
      return;
    }
    if (flags.indexOf('G') >= 0) {
      signals.add(new Ircv3InboundCommandSignal.UserAwayObserved(nick, true, null));
    } else if (flags.indexOf('H') >= 0) {
      signals.add(new Ircv3InboundCommandSignal.UserAwayObserved(nick, false, null));
    }
  }

  private static void addAccount(
      List<Ircv3InboundCommandSignal> signals, String nick, String account) {
    if (account == null) {
      return;
    }
    Ircv3InboundCommandSignal.AccountState state =
        "*".equals(account) || "0".equals(account)
            ? Ircv3InboundCommandSignal.AccountState.LOGGED_OUT
            : Ircv3InboundCommandSignal.AccountState.LOGGED_IN;
    signals.add(new Ircv3InboundCommandSignal.AccountObserved(nick, state, account));
  }
}

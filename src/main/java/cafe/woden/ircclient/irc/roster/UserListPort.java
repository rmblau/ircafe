package cafe.woden.ircclient.irc.roster;

import cafe.woden.ircclient.irc.IrcEvent.AccountState;
import cafe.woden.ircclient.irc.IrcEvent.AwayState;
import cafe.woden.ircclient.irc.IrcEvent.NickInfo;
import java.util.List;
import java.util.Set;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Exported contract for reading and mutating cached IRC channel rosters. */
@PrimaryPort
@ApplicationLayer
public interface UserListPort {

  String getLearnedHostmask(String serverId, String nick);

  String getLearnedRealName(String serverId, String nick);

  List<NickInfo> get(String serverId, String channel);

  Set<String> getServerNicks(String serverId);

  boolean isNickPresentOnServer(String serverId, String nick);

  Set<String> channelsContainingNick(String serverId, String nick);

  Set<String> getLowerNickSet(String serverId, String channel);

  void put(String serverId, String channel, List<NickInfo> nicks);

  void clear(String serverId, String channel);

  void clearServer(String serverId);

  boolean updateHostmask(String serverId, String channel, String nick, String hostmask);

  Set<String> updateHostmaskAcrossChannels(String serverId, String nick, String hostmask);

  boolean updateAwayState(String serverId, String channel, String nick, AwayState awayState);

  boolean updateAwayState(
      String serverId, String channel, String nick, AwayState awayState, String awayMessage);

  Set<String> updateAwayStateAcrossChannels(String serverId, String nick, AwayState awayState);

  Set<String> updateAwayStateAcrossChannels(
      String serverId, String nick, AwayState awayState, String awayMessage);

  Set<String> updateAccountAcrossChannels(
      String serverId, String nick, AccountState accountState, String accountName);

  Set<String> updateRealNameAcrossChannels(String serverId, String nick, String realName);
}

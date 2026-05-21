package cafe.woden.ircclient.irc.backend;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Shared validation messages for backend transport adapters. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IrcBackendValidationMessages {

  public static final String SERVER_ID_BLANK = "server id is blank";
}

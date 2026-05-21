package cafe.woden.ircclient.net;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Common HTTP header names used by the lightweight HTTP clients. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpHeaderNames {

  public static final String ACCEPT = "Accept";
  public static final String ACCEPT_ENCODING = "Accept-Encoding";
  public static final String ACCEPT_LANGUAGE = "Accept-Language";
  public static final String AUTHORIZATION = "Authorization";
  public static final String CONTENT_LENGTH = "Content-Length";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String REFERER = "Referer";
  public static final String USER_AGENT = "User-Agent";
  public static final String X_GITHUB_API_VERSION = "X-GitHub-Api-Version";
}

package cafe.woden.ircclient.translation;

import java.util.Objects;

/** Minimal HTTP response data needed by translation backends. */
public record MessageTranslationHttpResponse(int statusCode, String body) {

  public MessageTranslationHttpResponse {
    body = Objects.toString(body, "");
  }
}

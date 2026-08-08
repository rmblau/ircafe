package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import java.util.List;
import java.util.Objects;

/** Result of attempting a normalized link-preview request against a resolver chain. */
public record LinkPreviewResolutionResult(
    LinkPreview preview, List<LinkPreviewResolverFailure> failures) {

  public LinkPreviewResolutionResult {
    failures = List.copyOf(Objects.requireNonNullElse(failures, List.of()));
  }

  public static LinkPreviewResolutionResult matched(
      LinkPreview preview, List<LinkPreviewResolverFailure> failures) {
    return new LinkPreviewResolutionResult(Objects.requireNonNull(preview, "preview"), failures);
  }

  public static LinkPreviewResolutionResult noMatch(List<LinkPreviewResolverFailure> failures) {
    return new LinkPreviewResolutionResult(null, failures);
  }

  public boolean matched() {
    return preview != null;
  }
}

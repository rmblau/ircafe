package cafe.woden.ircclient.app.commands;

import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Adapts feature-owned {@code /filter} parsing to the root command model. */
@Component
@ApplicationLayer
public class FilterCommandParser {

  private static final FilterCommandSpecParser FILTER_COMMAND_SPEC_PARSER =
      new FilterCommandSpecParser();
  private static final FilterCommandAdapter FILTER_COMMAND_ADAPTER = new FilterCommandAdapter();

  public FilterCommand parse(String raw) {
    return FILTER_COMMAND_ADAPTER.toRoot(FILTER_COMMAND_SPEC_PARSER.parse(raw));
  }
}

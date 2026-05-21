package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.model.UserCommandAlias;
import java.util.List;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Mutable contract exported for reading and updating user-command aliases. */
@PrimaryPort
@ApplicationLayer
public interface UserCommandAliasesPort {

  List<UserCommandAlias> get();

  void set(List<UserCommandAlias> next);

  boolean unknownCommandAsRawEnabled();

  void setUnknownCommandAsRawEnabled(boolean enabled);
}

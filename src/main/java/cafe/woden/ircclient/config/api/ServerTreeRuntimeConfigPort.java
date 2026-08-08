package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Aggregated runtime-config contract used by the server-tree dockable assembly. */
@SecondaryPort
@ApplicationLayer
public interface ServerTreeRuntimeConfigPort
    extends ApplicationRootVisibilityConfigPort,
        IrcSessionRuntimeConfigPort,
        SelectedTargetRuntimeConfigPort,
        ServerAutoConnectRuntimeConfigPort,
        ServerTreeBuiltInVisibilityConfigPort,
        ServerTreeChannelStateConfigPort,
        ServerTreeLayoutConfigPort,
        UiSettingsRuntimeConfigPort {}

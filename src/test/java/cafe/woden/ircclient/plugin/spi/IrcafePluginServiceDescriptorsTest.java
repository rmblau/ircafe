package cafe.woden.ircclient.plugin.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
import java.util.List;
import org.junit.jupiter.api.Test;

class IrcafePluginServiceDescriptorsTest {

  @Test
  void buildsDescriptorPathFromServiceType() {
    assertEquals(
        "META-INF/services/" + BackendNamedCommandHandler.class.getName(),
        IrcafePluginServiceDescriptors.serviceDescriptorPath(BackendNamedCommandHandler.class));
  }

  @Test
  void buildsDescriptorPathFromServiceTypeName() {
    assertEquals(
        "META-INF/services/cafe.woden.ircclient.example.spi.ExampleProvider",
        IrcafePluginServiceDescriptors.serviceDescriptorPath(
            " cafe.woden.ircclient.example.spi.ExampleProvider "));
  }

  @Test
  void buildsProviderConfigurationContents() {
    assertEquals(
        "plugin.example.FirstProvider"
            + System.lineSeparator()
            + "plugin.example.SecondProvider"
            + System.lineSeparator(),
        IrcafePluginServiceDescriptors.serviceDescriptorContent(
            List.of(" plugin.example.FirstProvider ", "plugin.example.SecondProvider")));
  }

  @Test
  void rejectsBlankDescriptorParts() {
    assertThrows(
        IllegalArgumentException.class,
        () -> IrcafePluginServiceDescriptors.serviceDescriptorPath(" "));
    assertThrows(
        IllegalArgumentException.class,
        () -> IrcafePluginServiceDescriptors.serviceDescriptorContent(List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> IrcafePluginServiceDescriptors.serviceDescriptorContent("plugin.example.Ok", " "));
  }
}

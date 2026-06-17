package cafe.woden.ircclient.app.outbound.backend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.BackendDescriptorCatalog;
import java.util.List;
import org.junit.jupiter.api.Test;

class OutboundBackendFeatureRegistryTest {
  private static final BackendDescriptorCatalog BACKEND_DESCRIPTORS =
      BackendDescriptorCatalog.builtIns();

  @Test
  void adapterDefaultsToNoFeaturesWhenBackendHasNoAdapter() {
    OutboundBackendFeatureRegistry registry =
        cafe.woden.ircclient.app.outbound.TestBackendSupport.outboundBackendFeatureRegistry(
            List.of(new MatrixOutboundBackendFeatureAdapter()));

    OutboundBackendFeatureAdapter ircAdapter = registry.adapterFor("irc");
    assertFalse(ircAdapter.supportsSemanticUpload());
    assertFalse(ircAdapter.supportsQuasselCoreCommands());
  }

  @Test
  void matrixAndQuasselAdaptersExposeBackendSpecificFeatures() {
    OutboundBackendFeatureRegistry registry =
        cafe.woden.ircclient.app.outbound.TestBackendSupport.outboundBackendFeatureRegistry(
            List.of(
                new MatrixOutboundBackendFeatureAdapter(),
                new QuasselOutboundBackendFeatureAdapter()));

    assertTrue(registry.adapterFor("matrix").supportsSemanticUpload());
    assertFalse(registry.adapterFor("matrix").supportsQuasselCoreCommands());

    assertFalse(registry.adapterFor("quassel-core").supportsSemanticUpload());
    assertTrue(registry.adapterFor("quassel-core").supportsQuasselCoreCommands());
  }

  @Test
  void duplicateBackendAdaptersFailFast() {
    OutboundBackendFeatureAdapter first =
        new OutboundBackendFeatureAdapter() {
          @Override
          public String backendId() {
            return BACKEND_DESCRIPTORS.idFor(IrcProperties.Server.Backend.MATRIX);
          }
        };
    OutboundBackendFeatureAdapter second =
        new OutboundBackendFeatureAdapter() {
          @Override
          public String backendId() {
            return BACKEND_DESCRIPTORS.idFor(IrcProperties.Server.Backend.MATRIX);
          }
        };

    assertThrows(
        IllegalStateException.class,
        () ->
            cafe.woden.ircclient.app.outbound.TestBackendSupport.outboundBackendFeatureRegistry(
                List.of(first, second)));
  }

  @Test
  void supportsCustomBackendIds() {
    OutboundBackendFeatureAdapter pluginAdapter =
        new OutboundBackendFeatureAdapter() {
          @Override
          public String backendId() {
            return "plugin";
          }

          @Override
          public boolean supportsSemanticUpload() {
            return true;
          }
        };

    OutboundBackendFeatureRegistry registry =
        cafe.woden.ircclient.app.outbound.TestBackendSupport.outboundBackendFeatureRegistry(
            List.of(pluginAdapter));

    assertTrue(registry.adapterFor("plugin").supportsSemanticUpload());
  }
}

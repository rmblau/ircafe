package cafe.woden.ircclient.config.plugins;

/** Test-only access to package-private installed-plugin lifecycle cleanup. */
public final class InstalledPluginServicesTestSupport {

  private InstalledPluginServicesTestSupport() {}

  public static void shutdown(InstalledPluginServices installedPluginServices) {
    if (installedPluginServices != null) {
      installedPluginServices.shutdown();
    }
  }
}

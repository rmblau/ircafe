package cafe.woden.ircclient.plugin.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Authoring helpers for Java service-provider configuration files used by IRCafe plugin jars. */
public final class IrcafePluginServiceDescriptors {

  /** Directory inside a plugin jar that contains Java service-provider configuration files. */
  public static final String SERVICE_DESCRIPTOR_DIRECTORY = "META-INF/services";

  /** Returns the plugin jar entry path for the given SPI service contract. */
  public static String serviceDescriptorPath(Class<?> serviceType) {
    Objects.requireNonNull(serviceType, "serviceType");
    return serviceDescriptorPath(serviceType.getName());
  }

  /** Returns the plugin jar entry path for the given SPI service contract name. */
  public static String serviceDescriptorPath(String serviceTypeName) {
    return SERVICE_DESCRIPTOR_DIRECTORY + "/" + requiredText(serviceTypeName, "serviceTypeName");
  }

  /** Builds service-provider configuration file contents with one provider class name per line. */
  public static String serviceDescriptorContent(
      String providerClassName, String... additionalProviderClassNames) {
    ArrayList<String> providerClassNames = new ArrayList<>();
    providerClassNames.add(providerClassName);
    providerClassNames.addAll(
        Arrays.asList(Objects.requireNonNullElse(additionalProviderClassNames, new String[0])));
    return serviceDescriptorContent(providerClassNames);
  }

  /** Builds service-provider configuration file contents with one provider class name per line. */
  public static String serviceDescriptorContent(Collection<String> providerClassNames) {
    ArrayList<String> normalizedProviderClassNames = new ArrayList<>();
    for (String providerClassName :
        Objects.requireNonNullElse(providerClassNames, List.<String>of())) {
      normalizedProviderClassNames.add(requiredText(providerClassName, "providerClassName"));
    }
    if (normalizedProviderClassNames.isEmpty()) {
      throw new IllegalArgumentException("[ircafe] providerClassNames must not be empty");
    }
    return String.join(System.lineSeparator(), normalizedProviderClassNames)
        + System.lineSeparator();
  }

  private static String requiredText(String value, String name) {
    String normalized = Objects.toString(value, "").trim();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("[ircafe] " + name + " must not be blank");
    }
    return normalized;
  }

  private IrcafePluginServiceDescriptors() {}
}

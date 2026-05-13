package cafe.woden.ircclient.config;

/** Test fixture builder for {@link PushyProperties}. */
public final class PushyPropertiesTestFixtures {

  private PushyPropertiesTestFixtures() {}

  public static Builder builder() {
    return new Builder();
  }

  public static PushyProperties disabled() {
    return builder().enabled(false).build();
  }

  public static final class Builder {
    private Boolean enabled = false;
    private String endpoint;
    private String apiKey;
    private String deviceToken;
    private String topic;
    private String titlePrefix;
    private Integer connectTimeoutSeconds;
    private Integer readTimeoutSeconds;

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder endpoint(String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public Builder deviceToken(String deviceToken) {
      this.deviceToken = deviceToken;
      return this;
    }

    public Builder topic(String topic) {
      this.topic = topic;
      return this;
    }

    public Builder titlePrefix(String titlePrefix) {
      this.titlePrefix = titlePrefix;
      return this;
    }

    public Builder connectTimeoutSeconds(Integer connectTimeoutSeconds) {
      this.connectTimeoutSeconds = connectTimeoutSeconds;
      return this;
    }

    public Builder readTimeoutSeconds(Integer readTimeoutSeconds) {
      this.readTimeoutSeconds = readTimeoutSeconds;
      return this;
    }

    public PushyProperties build() {
      return new PushyProperties(
          enabled,
          endpoint,
          apiKey,
          deviceToken,
          topic,
          titlePrefix,
          connectTimeoutSeconds,
          readTimeoutSeconds);
    }
  }
}

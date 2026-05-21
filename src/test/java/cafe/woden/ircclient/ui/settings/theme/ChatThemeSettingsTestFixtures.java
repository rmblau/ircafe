package cafe.woden.ircclient.ui.settings.theme;

public final class ChatThemeSettingsTestFixtures {
  private ChatThemeSettingsTestFixtures() {}

  public static ChatThemeSettings defaults() {
    return builder().build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ChatThemeSettings.Preset preset = ChatThemeSettings.Preset.DEFAULT;
    private String timestampColor;
    private String systemColor;
    private String mentionBgColor;
    private int mentionStrength = 35;
    private String messageColor;
    private String noticeColor;
    private String actionColor;
    private String errorColor;
    private String presenceColor;

    private Builder() {}

    public Builder preset(ChatThemeSettings.Preset preset) {
      this.preset = preset;
      return this;
    }

    public Builder timestampColor(String timestampColor) {
      this.timestampColor = timestampColor;
      return this;
    }

    public Builder systemColor(String systemColor) {
      this.systemColor = systemColor;
      return this;
    }

    public Builder mentionBgColor(String mentionBgColor) {
      this.mentionBgColor = mentionBgColor;
      return this;
    }

    public Builder mentionStrength(int mentionStrength) {
      this.mentionStrength = mentionStrength;
      return this;
    }

    public Builder messageColor(String messageColor) {
      this.messageColor = messageColor;
      return this;
    }

    public Builder noticeColor(String noticeColor) {
      this.noticeColor = noticeColor;
      return this;
    }

    public Builder actionColor(String actionColor) {
      this.actionColor = actionColor;
      return this;
    }

    public Builder errorColor(String errorColor) {
      this.errorColor = errorColor;
      return this;
    }

    public Builder presenceColor(String presenceColor) {
      this.presenceColor = presenceColor;
      return this;
    }

    public ChatThemeSettings build() {
      return new ChatThemeSettings(
          preset,
          timestampColor,
          systemColor,
          mentionBgColor,
          mentionStrength,
          messageColor,
          noticeColor,
          actionColor,
          errorColor,
          presenceColor);
    }
  }
}

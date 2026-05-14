package cafe.woden.ircclient.ui.settings.startup;

public record LaunchGcOption(String id, String label) {
  @Override
  public String toString() {
    return label;
  }
}

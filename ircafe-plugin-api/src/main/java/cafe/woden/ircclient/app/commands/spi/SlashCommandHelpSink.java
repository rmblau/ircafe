package cafe.woden.ircclient.app.commands.spi;

/** Output sink for plugin-provided slash-command help lines. */
public interface SlashCommandHelpSink {

  SlashCommandTargetView target();

  void appendLine(String line);
}

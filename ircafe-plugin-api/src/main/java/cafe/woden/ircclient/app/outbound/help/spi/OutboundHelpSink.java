package cafe.woden.ircclient.app.outbound.help.spi;

/** Output sink for plugin-provided outbound command help lines. */
public interface OutboundHelpSink {

  OutboundHelpTargetView target();

  void appendLine(String line);
}

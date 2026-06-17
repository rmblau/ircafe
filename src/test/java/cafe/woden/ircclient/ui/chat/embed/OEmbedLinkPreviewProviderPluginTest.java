package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OEmbedLinkPreviewProviderPluginTest {

  @TempDir Path tempDir;

  @Test
  void loadsOEmbedProvidersFromPluginDirectoryJar() throws Exception {
    try (JsonServer server = JsonServer.start()) {
      Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
      Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
      writePluginJar(pluginDir.resolve("plugin-oembed-provider.jar"), server.port());
      RuntimeConfigPathPort runtimeConfigPathPort =
          () -> runtimeConfigDirectory.resolve("ircafe.yml");
      InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

      LinkPreviewResolver resolver =
          LinkPreviewResolverConfig.oEmbedLinkPreviewResolver(installedPlugins);
      LinkPreviewFetchService service = new LinkPreviewFetchService(null, List.of(resolver));

      LinkPreview preview =
          service.fetch("server-a", "https://plugin-oembed.example/item/42").blockingGet();

      assertEquals("Plugin oEmbed title", preview.title());
      assertEquals("by Plugin Author", preview.description());
      assertEquals("Plugin oEmbed", preview.siteName());
      assertEquals("https://cdn.example/plugin-thumb.png", preview.imageUrl());
      assertEquals(1, preview.mediaCount());
      assertTrue(installedPlugins.pluginProblems().isEmpty());
      assertTrue(server.servedRequest());
    }
  }

  private void writePluginJar(Path jarPath, int port) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginOEmbedProvider";
    String providerSource =
        """
        package cafe.woden.ircclient.testplugins;

        import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider;
        import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedResponseFields;
        import java.net.URI;
        import java.net.URLEncoder;
        import java.nio.charset.StandardCharsets;

        public final class PluginOEmbedProvider implements OEmbedLinkPreviewProvider {
          @Override
          public String id() {
            return "plugin-oembed";
          }

          @Override
          public boolean matches(URI uri) {
            return uri != null && "plugin-oembed.example".equals(uri.getHost());
          }

          @Override
          public URI endpointFor(URI uri, String originalUrl) {
            String encoded = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);
            return URI.create("http://127.0.0.1:PORT/oembed?url=" + encoded);
          }

          @Override
          public String defaultSiteName() {
            return "Plugin fallback";
          }

          @Override
          public String titleFallback(OEmbedResponseFields fields) {
            return "Plugin fallback title";
          }
        }
        """
            .replace("PORT", Integer.toString(port));
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        OEmbedLinkPreviewProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-oembed-provider", "1.0.0"));
  }

  private static final class JsonServer implements AutoCloseable {
    private final ServerSocket socket;
    private final Thread thread;
    private volatile boolean servedRequest;

    private JsonServer(ServerSocket socket) {
      this.socket = socket;
      this.thread = new Thread(this::serveOneRequest, "oembed-provider-plugin-test-server");
      this.thread.setDaemon(true);
    }

    static JsonServer start() throws IOException {
      ServerSocket socket = new ServerSocket();
      socket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
      JsonServer server = new JsonServer(socket);
      server.thread.start();
      return server;
    }

    int port() {
      return socket.getLocalPort();
    }

    boolean servedRequest() {
      return servedRequest;
    }

    private void serveOneRequest() {
      try (Socket client = socket.accept();
          BufferedReader reader =
              new BufferedReader(
                  new InputStreamReader(client.getInputStream(), StandardCharsets.US_ASCII));
          var output = client.getOutputStream()) {
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
          // Drain request headers so the client can receive the response cleanly.
        }
        servedRequest = true;
        byte[] body =
            """
            {"title":"Plugin oEmbed title","author_name":"Plugin Author","provider_name":"Plugin oEmbed","thumbnail_url":"https://cdn.example/plugin-thumb.png"}
            """
                .strip()
                .getBytes(StandardCharsets.UTF_8);
        byte[] headers =
            ("HTTP/1.1 200 OK\r\n"
                    + "Content-Type: application/json\r\n"
                    + "Content-Length: "
                    + body.length
                    + "\r\n"
                    + "Connection: close\r\n\r\n")
                .getBytes(StandardCharsets.US_ASCII);
        output.write(headers);
        output.write(body);
        output.flush();
      } catch (SocketException ignored) {
        // The socket may be closed during test cleanup.
      } catch (IOException ignored) {
      }
    }

    @Override
    public void close() throws IOException {
      socket.close();
    }
  }
}

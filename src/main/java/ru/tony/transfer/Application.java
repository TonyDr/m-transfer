package ru.tony.transfer;


import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.bridge.SLF4JBridgeHandler;
import ru.tony.transfer.config.JerseyConfiguration;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Application {

    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    public static void main(String[] args) {
        new EmbeddedServer().start();

    }

    public static class EmbeddedServer {

        void start() {
            URI baseUri = UriBuilder.fromUri("http://localhost").port(9998).build();
            ResourceConfig config = new JerseyConfiguration();
            Server server = JettyHttpContainerFactory.createServer(baseUri, config);

            try {
                server.start();
                server.join();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                server.destroy();
            }
        }

    }
}

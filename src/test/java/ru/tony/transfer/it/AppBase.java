package ru.tony.transfer.it;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import ru.tony.transfer.config.JerseyConfiguration;

import javax.ws.rs.core.Application;

public abstract class AppBase extends JerseyTest {


    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        // enable(TestProperties.DUMP_ENTITY);
        return new JerseyConfiguration();
    }

    @Override
    protected void configureClient(ClientConfig clientConfig) {
        clientConfig.connectorProvider(new JettyConnectorProvider());
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new JettyTestContainerFactory();
    }
}

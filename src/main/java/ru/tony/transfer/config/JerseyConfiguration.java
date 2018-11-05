package ru.tony.transfer.config;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import ru.tony.transfer.db.ConnectionManager;
import ru.tony.transfer.db.DbConnection;
import ru.tony.transfer.repository.impl.AccountRepositoryImpl;
import ru.tony.transfer.service.AccountService;
import ru.tony.transfer.service.impl.AccountServiceImpl;

public class JerseyConfiguration extends ResourceConfig {

    private AccountService accountService;

    public JerseyConfiguration() {
        initServices();
        packages("ru.tony.transfer.resource");
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(accountService).to(AccountService.class);
            }
        });
    }

    private void initServices() {
        accountService = new AccountServiceImpl(new AccountRepositoryImpl(),
                new ConnectionManager(DbConnection.getDataSource()));
    }
}
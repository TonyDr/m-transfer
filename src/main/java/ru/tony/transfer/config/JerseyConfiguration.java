package ru.tony.transfer.config;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import ru.tony.transfer.db.DBWorkManager;
import ru.tony.transfer.db.DbConnection;
import ru.tony.transfer.db.DbConnectionManager;
import ru.tony.transfer.repository.impl.AccountRepositoryImpl;
import ru.tony.transfer.repository.impl.AccountTransactionRepositoryImpl;
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
        DbConnectionManager cm = new DbConnectionManager(DbConnection.getDataSource());
        DBWorkManager workManager = new DBWorkManager(cm);
        accountService = new AccountServiceImpl(new AccountRepositoryImpl(cm),
                workManager, new AccountTransactionRepositoryImpl(cm));
    }
}

package dev.iamkavindu.nuledger.tenant;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;

public class TenantRlsJpaTransactionManager extends JpaTransactionManager {

    public TenantRlsJpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);
        applyTenantRls();
    }

    private void applyTenantRls() {
        if (!TenantContext.isBound()) {
            return;
        }

        var entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(getEntityManagerFactory());
        if (entityManager == null) {
            return;
        }

        var tenantId = TenantContext.requireTenantId();
        entityManager.unwrap(Session.class).doWork(connection -> {
            try (var preparedStatement = connection.prepareStatement("SELECT set_config('app.tenant_id', ?, true)")) {
                preparedStatement.setString(1, tenantId);
                preparedStatement.execute();
            }
        });
    }
}

package dev.iamkavindu.nuledger.config;

import java.util.Map;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateTenantConfig implements HibernatePropertiesCustomizer {

    private final CurrentTenantIdentifierResolver<String> tenantResolver;

    public HibernateTenantConfig(CurrentTenantIdentifierResolver<String> tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);
    }
}

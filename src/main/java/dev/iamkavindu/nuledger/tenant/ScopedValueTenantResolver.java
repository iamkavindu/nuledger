package dev.iamkavindu.nuledger.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class ScopedValueTenantResolver implements CurrentTenantIdentifierResolver<String> {

    public static final String BOOTSTRAP_TENANT = "BOOTSTRAP";

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getTenantId().orElse(BOOTSTRAP_TENANT);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}

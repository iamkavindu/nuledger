package dev.iamkavindu.nuledger.tenant;

import java.util.Optional;

public class TenantContext {

    private static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();

    private TenantContext() {}

    public static String requireTenantId() {
        if (!TENANT_ID.isBound()) {
            throw new IllegalStateException("Tenant ID is not bound");
        }
        return TENANT_ID.get();
    }

    public static void runWithTenant(String tenantId, Runnable action) {
        ScopedValue.where(TENANT_ID, tenantId).run(action);
    }

    public static boolean isBound() {
        return TENANT_ID.isBound();
    }

    public static Optional<String> getTenantId() {
        return isBound() ? Optional.of(TENANT_ID.get()) : Optional.empty();
    }
}

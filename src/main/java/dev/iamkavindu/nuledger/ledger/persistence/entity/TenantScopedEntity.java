package dev.iamkavindu.nuledger.ledger.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.TenantId;

@Getter
@MappedSuperclass
public abstract class TenantScopedEntity {
    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 64)
    private String tenantId;
}

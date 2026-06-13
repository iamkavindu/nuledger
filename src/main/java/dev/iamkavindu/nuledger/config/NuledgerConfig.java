package dev.iamkavindu.nuledger.config;

import dev.iamkavindu.nuledger.tenant.JwtTenantFilter;
import dev.iamkavindu.nuledger.tenant.TenantRlsJpaTransactionManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class NuledgerConfig {

    @Bean
    JwtTenantFilter jwtTenantFilter(@Value("${nuledger.security.tenant-claim}") String tenantClaim) {
        return new JwtTenantFilter(tenantClaim);
    }

    @Bean
    PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new TenantRlsJpaTransactionManager(entityManagerFactory);
    }
}

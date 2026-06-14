package dev.iamkavindu.nuledger.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtTenantFilter extends OncePerRequestFilter {

    private final String tenantClaim;

    public JwtTenantFilter(@Value("${nuledger.security.tenant-claim}") String tenantClaim) {
        this.tenantClaim = tenantClaim;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            filterChain.doFilter(request, response);
            return;
        }

        var tenantId = jwtAuth.getToken().getClaimAsString(tenantClaim);
        if (tenantId == null || tenantId.isBlank()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing tenant claim: " + tenantClaim);
            return;
        }

        TenantContext.runWithTenant(tenantId.trim(), () -> {
            try {
                filterChain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

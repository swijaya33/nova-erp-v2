package com.novaerp.common.tenant;

/**
 * ThreadLocal-based tenant context for multi-tenancy.
 * Tenant ID is extracted from JWT claims by a filter and stored here.
 * All entities with tenantId field auto-inject from this context in BaseEntity.onCreate().
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static void setCurrentTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be blank");
        }
        CURRENT_TENANT.set(tenantId);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    /** Filter that extracts tenant from JWT and sets it in TenantContext */
    public static class TenantResolverFilter implements jakarta.servlet.Filter {

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request,
                             jakarta.servlet.ServletResponse response,
                             jakarta.servlet.FilterChain chain)
                throws java.io.IOException, jakarta.servlet.ServletException {
            try {
                // Extract tenant from Authorization header (JWT Bearer token)
                if (request instanceof jakarta.servlet.http.HttpServletRequest httpReq) {
                    String authHeader = httpReq.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        // TODO: Decode JWT and extract tenant claim
                        // For now, use a default — real implementation goes in Auth service
                        String tenantId = resolveTenantFromToken(token);
                        if (tenantId != null) {
                            TenantContext.setCurrentTenant(tenantId);
                        }
                    }
                }
                chain.doFilter(request, response);
            } finally {
                TenantContext.clear();
            }
        }

        private String resolveTenantFromToken(String token) {
            // Placeholder — real JWT decoding happens in Auth service or a shared JWT utility
            return null;
        }
    }
}

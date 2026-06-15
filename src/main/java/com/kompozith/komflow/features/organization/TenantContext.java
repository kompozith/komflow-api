package com.kompozith.komflow.features.organization;

/**
 * Stockage ThreadLocal de l'organisation courante (tenant context).
 * <p>
 * Alimenté par {@link com.kompozith.komflow.configuration.security.JwtTokenFilter}
 * après validation du JWT. Effacé systématiquement dans un bloc {@code finally}
 * pour éviter les fuites entre requêtes (pool de threads).
 * <p>
 * Usage :
 * <pre>
 *   Long orgId = TenantContext.getOrganizationId();
 *   // dans un Repository : WHERE organization_id = :orgId
 * </pre>
 */
public final class TenantContext {

  private static final ThreadLocal<Long> ORGANIZATION_ID = new ThreadLocal<>();

  private TenantContext() {}

  public static void setOrganizationId(Long id) {
    ORGANIZATION_ID.set(id);
  }

  public static Long getOrganizationId() {
    return ORGANIZATION_ID.get();
  }

  /** À appeler dans un {@code finally} après chaque requête. */
  public static void clear() {
    ORGANIZATION_ID.remove();
  }
}

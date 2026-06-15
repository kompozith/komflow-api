package com.kompozith.komflow.features.billing.port;

import java.time.Instant;

/**
 * Port hexagonal pour les opérations de paiement / facturation.
 *
 * Chaque opérateur de paiement (Stripe, Paddle, LemonSqueezy, PayPal, etc.)
 * implémente cette interface. L'orchestrateur {@link BillingOperatorStack}
 * choisit l'adaptateur actif selon la configuration, sans jamais coupler
 * le domaine à un provider particulier.
 *
 * <pre>
 * Implémenter un nouveau provider :
 *   1. Créer une classe @Service implémentant BillingProviderPort
 *   2. Définir providerCode() → ex. "PADDLE"
 *   3. Renseigner app.billing.provider-type=PADDLE dans application.yml
 *   4. Aucun autre fichier à modifier
 * </pre>
 */
public interface BillingProviderPort {

  // ── Identité du provider ─────────────────────────────────────────────────

  /** Code technique unique : "STUB", "STRIPE", "PADDLE", "LEMONSQUEEZY"… */
  String providerCode();

  // ── Commandes ────────────────────────────────────────────────────────────

  /**
   * Crée une session de paiement (checkout) et retourne l'URL de redirection.
   * @param cmd paramètres du checkout
   * @return résultat contenant l'URL et l'ID de session externe
   */
  CheckoutResult createCheckoutSession(CheckoutCommand cmd);

  /**
   * Crée un portail client (gestion abonnement) et retourne l'URL.
   * @param orgId identifiant organisation
   * @param returnUrl URL de retour après visite du portail
   * @return URL du portail
   */
  String createPortalSession(Long orgId, String returnUrl);

  /**
   * Vérifie la signature d'un webhook entrant et retourne l'événement parsé.
   * Lève {@link WebhookVerificationException} si la signature est invalide.
   *
   * @param payload  corps brut de la requête (bytes)
   * @param headers  en-têtes HTTP nécessaires à la vérification
   * @return événement vérifié et normalisé
   */
  BillingEvent verifyAndParseWebhook(byte[] payload, java.util.Map<String, String> headers);

  // ── Records de commande / résultat ───────────────────────────────────────

  record CheckoutCommand(
      Long   organizationId,
      String planCode,
      String customerEmail,
      String successUrl,
      String cancelUrl,
      /** Durée d'essai en jours (0 = pas d'essai) */
      int    trialDays
  ) {}

  record CheckoutResult(
      /** URL vers laquelle rediriger le navigateur */
      String checkoutUrl,
      /** ID de session/order côté provider */
      String externalSessionId
  ) {}

  // ── Événement normalisé (provider-agnostic) ──────────────────────────────

  enum BillingEventType {
    SUBSCRIPTION_ACTIVATED,
    SUBSCRIPTION_RENEWED,
    SUBSCRIPTION_CANCELED,
    SUBSCRIPTION_PAST_DUE,
    PAYMENT_SUCCEEDED,
    PAYMENT_FAILED,
    TRIAL_WILL_END,
    UNKNOWN
  }

  record BillingEvent(
      BillingEventType type,
      Long             organizationId,
      String           planCode,
      Instant          periodEnd,
      /** Montant en centimes (peut être null pour certains événements) */
      Long             amountCents,
      /** ID externe de l'événement chez le provider */
      String           externalEventId
  ) {}

  // ── Exception de vérification webhook ───────────────────────────────────

  class WebhookVerificationException extends RuntimeException {
    public WebhookVerificationException(String message) { super(message); }
    public WebhookVerificationException(String message, Throwable cause) { super(message, cause); }
  }
}

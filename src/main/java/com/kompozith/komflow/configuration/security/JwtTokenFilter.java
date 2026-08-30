package com.kompozith.komflow.configuration.security;

import com.kompozith.komflow.exception.AccessDeniedException;
import com.kompozith.komflow.exception.JwtAuthenticationException;
import com.kompozith.komflow.features.organization.TenantContext;
import com.kompozith.komflow.features.organization.entity.Organization;
import com.kompozith.komflow.features.organization.entity.OrganizationMember.MemberStatus;
import com.kompozith.komflow.features.organization.repository.OrganizationMemberRepository;
import com.kompozith.komflow.features.organization.repository.OrganizationRepository;
import com.kompozith.komflow.features.personnel.dto.UserDetailsInterfaceDto;
import com.kompozith.komflow.features.personnel.repository.UserRepository;
import com.kompozith.komflow.util.ErrorResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final String WORKSPACE_SLUG_HEADER = "X-Workspace-Slug";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final ErrorResponseWriter errorResponseWriter;

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {
        try {
            String token = resolveToken(request);

            if (token != null && jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Alimentation du TenantContext (multi-tenancy)
                Long orgId = jwtUtil.extractOrganizationId(token);
                if (orgId != null) {
                    TenantContext.setOrganizationId(orgId);
                }

                // La présence d'un slug d'espace dans l'URL (relayé par le frontend via ce
                // header) l'emporte sur la claim du JWT : c'est ce que l'utilisateur regarde
                // activement. On vérifie systématiquement l'appartenance active avant de
                // faire confiance à cette valeur, pour empêcher l'accès aux données d'un
                // autre espace par simple changement de header. Une résolution invalide
                // stoppe immédiatement la requête (403) plutôt que de laisser retomber sur
                // le TenantContext du JWT, qui pourrait pointer vers un autre espace que
                // celui affiché à l'écran.
                String workspaceSlug = request.getHeader(WORKSPACE_SLUG_HEADER);
                if (StringUtils.hasText(workspaceSlug)) {
                    try {
                        resolveOrganizationFromSlug(workspaceSlug, username);
                    } catch (AccessDeniedException ex) {
                        TenantContext.clear();
                        errorResponseWriter.writeErrorResponse(response, HttpStatus.FORBIDDEN, ex);
                        return;
                    } catch (Exception ex) {
                        log.error("Failed to resolve workspace slug '{}' for user '{}'", workspaceSlug, username, ex);
                        throw ex;
                    }
                }
            }
        } catch (JwtAuthenticationException ex) {
            request.setAttribute("jwtError", ex);
        } catch (Exception ex) {
            request.setAttribute("authError", ex);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = jwtUtil.extractTokenFromHeader(header);

        if (token != null) {
            return token;
        }

        if (isSseCampaignEventsRequest(request)) {
            String queryToken = request.getParameter("token");
            if (queryToken != null && !queryToken.isBlank()) {
                return queryToken;
            }
        }

        return null;
    }

    private boolean isSseCampaignEventsRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        // Existing: /campaigns/{id}/events
        if (uri.contains("/campaigns/") && uri.endsWith("/events")) {
            return true;
        }
        // New: /events/{id}/registration-stats/stream
        return uri.matches(".*/events/\\d+/registration-stats/stream");
    }

    /**
     * Résout le slug d'espace envoyé par le frontend vers l'organisation correspondante,
     * vérifie que l'utilisateur authentifié en est membre actif, puis alimente le
     * TenantContext avec cet id (prioritaire sur la claim organizationId du JWT).
     * Lève AccessDeniedException si le slug est inconnu ou si l'utilisateur n'est pas
     * membre actif — jamais silencieusement ignoré, pour ne pas laisser une requête
     * retomber sur l'organisation du JWT alors que l'UI affiche un autre espace.
     */
    private void resolveOrganizationFromSlug(String workspaceSlug, String username) {
        Organization organization = organizationRepository.findBySlug(workspaceSlug)
                .orElseThrow(() -> new AccessDeniedException("Unknown workspace: " + workspaceSlug));

        UserDetailsInterfaceDto currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("Unknown user: " + username));
        Long userId = Long.valueOf(currentUser.getId());

        boolean isActiveMember = organizationMemberRepository
                .findByOrganizationIdAndUserId(organization.getId(), userId)
                .filter(member -> member.getStatus() == MemberStatus.ACTIVE)
                .isPresent();

        if (!isActiveMember) {
            throw new AccessDeniedException("Not a member of workspace: " + workspaceSlug);
        }

        TenantContext.setOrganizationId(organization.getId());
    }
}

package com.kompozith.komflow.configuration.security;

import com.kompozith.komflow.exception.JwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

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
            }
        } catch (JwtAuthenticationException ex) {
            request.setAttribute("jwtError", ex);
        } catch (Exception ex) {
            request.setAttribute("authError", ex);
        }

        filterChain.doFilter(request, response);
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
        return uri != null && uri.contains("/campaigns/") && uri.endsWith("/events");
    }
}

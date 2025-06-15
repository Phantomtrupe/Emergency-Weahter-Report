package com.github.phantomtrupe.commons.JWT;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

public class JwtServerSecurityContextRepository implements ServerSecurityContextRepository {
    private static final String BEARER_PREFIX = "Bearer ";
    private final ReactiveAuthenticationManager authenticationManager;

    public JwtServerSecurityContextRepository(ReactiveAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        // no-op, stateless
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String authToken = authHeader.substring(BEARER_PREFIX.length());
            Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(authToken, authToken);
            return authenticationManager.authenticate(auth)
                    .map(SecurityContextImpl::new);
        }
        return Mono.empty();
    }
}

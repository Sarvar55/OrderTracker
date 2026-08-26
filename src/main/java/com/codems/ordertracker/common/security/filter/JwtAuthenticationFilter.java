package com.codems.ordertracker.common.security.filter;

import com.codems.ordertracker.common.security.service.JwtService;
import com.codems.ordertracker.common.security.model.SecurityUser;
import com.codems.ordertracker.domain.base.RecordStatus;
import com.codems.ordertracker.domain.user.entity.User;
import com.codems.ordertracker.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Qualifier("publicPaths")
    private final List<String> publicPaths;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());
        if (!jwtService.isValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractSubject(token);
        userRepository.findByEmail(email).ifPresent(this::setAuthentication);
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(User user) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }
        if (!user.isEnabled()
                || !user.isAccountNonLocked()
                || user.getStatus() != RecordStatus.ACTIVE) {
            return;
        }

        SecurityUser principal = SecurityUser.from(user);
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return publicPaths.stream().anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }
}

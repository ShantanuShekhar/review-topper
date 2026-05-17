package com.reviewtopper.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Parses optional Bearer JWT and sets the security context. Skipped entirely
 * for OPTIONS and all {@link PublicApiRequestMatchers public} routes so
 * unauthenticated browser calls (incl. CORS preflight) are not touched.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final RequestMatcher PUBLIC_API = new OrRequestMatcher(PublicApiRequestMatchers.all());

	private final JwtTokenProvider jwtTokenProvider;
	private final CustomUserDetailsService userDetailsService;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
			return true;
		}
		return PUBLIC_API.matches(request);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}

		if (request.getServletPath().startsWith("/api/auth")) {
			filterChain.doFilter(request, response);
			return;
		}

		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		String token = header.substring(7).trim();
		if (token.isEmpty()) {
			filterChain.doFilter(request, response);
			return;
		}
		try {
			String email = jwtTokenProvider.parse(token).getSubject();
			if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails details = userDetailsService.loadUserByUsername(email);
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(details, null,
						details.getAuthorities());
				auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		} catch (Exception ignored) {
			SecurityContextHolder.clearContext();
		}
		filterChain.doFilter(request, response);
	}
}

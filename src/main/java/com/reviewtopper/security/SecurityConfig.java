package com.reviewtopper.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviewtopper.dto.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomUserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final ObjectMapper objectMapper;
	private final CorsConfigurationSource corsConfigurationSource;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource))
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/", "/actuator/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login",
								"/api/auth/forgot-password", "/api/auth/reset-password")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/api/auth/test", "/api/subscription-plans",
								"/api/subscription-plans/**")
						.permitAll().requestMatchers(HttpMethod.GET, "/api/plans", "/api/plans/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/stats", "/api/stats/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/feedback").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/generate-comments", "/api/submit-feedback").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/redirect/**").permitAll().requestMatchers("/api/admin/**")
						.hasRole("ADMIN").requestMatchers("/api/**").authenticated().anyRequest().denyAll())
				.exceptionHandling(ex -> ex.accessDeniedHandler((request, response,
						accessDeniedException) -> writeSecurityError(objectMapper, request, response)))
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	/**
	 * Anonymous or unauthenticated calls to secured paths would yield opaque Spring
	 * defaults. Return JSON shaped like
	 * {@link com.reviewtopper.dto.error.ApiErrorResponse}: 401 when missing
	 * JWT/session, 403 when authenticated but denied (permission).
	 */
	private static void writeSecurityError(ObjectMapper objectMapper, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean missingOrAnonymous = authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken;

		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		int status;
		ApiErrorResponse body;
		if (missingOrAnonymous) {
			status = HttpServletResponse.SC_UNAUTHORIZED;
			body = ApiErrorResponse.builder().timestamp(Instant.now()).status(status).error("UNAUTHORIZED").message(
					"Authentication required. Use Authorization: Bearer <token> from POST /api/auth/login or /api/auth/register.")
					.path(request.getRequestURI()).build();
		} else {
			status = HttpServletResponse.SC_FORBIDDEN;
			body = ApiErrorResponse.builder().timestamp(Instant.now()).status(status).error("FORBIDDEN")
					.message("Access denied").path(request.getRequestURI()).build();
		}
		response.setStatus(status);
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}

	@Bean
	AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(passwordEncoder);
		provider.setUserDetailsService(userDetailsService);
		return provider;
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}
}

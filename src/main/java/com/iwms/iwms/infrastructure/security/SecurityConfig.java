package com.iwms.iwms.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.iwms.iwms.domain.repository.UserInfoRepository;
import com.iwms.iwms.domain.repository.SupabaseUserRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final UserInfoRepository userInfoRepository;
    private final SupabaseUserRepository supabaseUserRepository;

    public SecurityConfig(UserInfoRepository userInfoRepository, SupabaseUserRepository supabaseUserRepository) {
        this.userInfoRepository = userInfoRepository;
        this.supabaseUserRepository = supabaseUserRepository;
    }

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Value("${app.security.jwt-debug-log:false}")
    private boolean jwtDebugEnabled;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/users/register").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth -> oauth
                .bearerTokenResolver(bearerTokenResolver())
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(new DatabaseEnrichedJwtAuthenticationConverter(userInfoRepository, supabaseUserRepository))
                )
            );

        // Optional debug filter (enabled via property)
        http.addFilterBefore(new JwtDebugLoggingFilter((CookieBearerTokenResolver) bearerTokenResolver(), jwtDebugEnabled),
            UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        // Supabase uses cookies in some flows; read from "sb-access-token" if header is absent
        return new CookieBearerTokenResolver("sb-access-token", new DefaultBearerTokenResolver());
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        if (jwkSetUri == null || jwkSetUri.isBlank()) {
            throw new IllegalStateException("JWK Set URI must be set: spring.security.oauth2.resourceserver.jwt.jwk-set-uri");
        }

        log.info("Configuring JwtDecoder: JWK Set URI {} (alg=ES256)", jwkSetUri);
        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withJwkSetUri(jwkSetUri)
            .jwsAlgorithm(SignatureAlgorithm.ES256)
            .build();

        // Add issuer validation when provided (recommended for OAuth2/OIDC)
        if (issuerUri != null && !issuerUri.isBlank()) {
            OAuth2TokenValidator<Jwt> defaultWithIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultWithIssuer));
            log.info("Enabled issuer validation: {}", issuerUri);
        }

        return decoder;
    }
}



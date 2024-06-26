package com.dbserver.votacaoBackend.infra.security;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private SecurityFilter securityFilter;
    private static final String AUTORIDADE_ADMIN = "ADMIN";

    public SecurityConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csfr -> csfr.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(HttpMethod.POST, "/pauta").hasAuthority(AUTORIDADE_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/pauta/usuarioLogado", "/pauta/detalhes/**").hasAuthority(AUTORIDADE_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/pauta/ativas").authenticated()

                        .requestMatchers(HttpMethod.GET, "/usuario/usuarioLogado").authenticated()
                        .requestMatchers(HttpMethod.POST, "/usuario").hasAuthority(AUTORIDADE_ADMIN)
                        
                        .requestMatchers(HttpMethod.POST, "/votacao/abrir").hasAuthority(AUTORIDADE_ADMIN)

                        .requestMatchers(HttpMethod.PATCH, "/votacao/votoInterno").authenticated()

                        .anyRequest().permitAll())
                .cors(cors -> cors.configurationSource(this.corsConfigurationSource()))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEnconder() {
        return new BCryptPasswordEncoder();
    }
}
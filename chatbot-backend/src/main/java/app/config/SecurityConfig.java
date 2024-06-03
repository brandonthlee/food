package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.exception.UserForbiddenException;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 1. Disable CSRF
        // When using SSR (Server-Side Rendering), it's recommended to enable CSRF protection.
        // However, if the frontend is separated, it's better to disable it.
        // When testing with Postman, CSRF must be disabled to prevent errors.
        http.csrf(AbstractHttpConfigurer::disable); // Access through Postman required!!

        // 2. Deny iframe embedding
        http.headers(headers ->
                headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        // 3. Reset CORS configuration
        http.cors(cors ->
                cors.configurationSource(configurationSource()));

        // 4. Make jSessionId disappear from the response (Will make it stateless with JWT)
        http.sessionManagement(management ->
                management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 5. Disable form-based login and UsernamePasswordAuthenticationFilter
        http.formLogin(AbstractHttpConfigurer::disable);

        // 6. Disable HttpBasicAuthenticationFilter to prevent login authentication popup (using username, password in headers)
        http.httpBasic(AbstractHttpConfigurer::disable);

        // 7. Apply custom filters - Custom security filter manager (replace filters with an internal class)
        http.apply(new CustomSecurityFilterManager());

        // 8. Handle authentication failures
        http.exceptionHandling(handling ->
                handling.authenticationEntryPoint(((request, response, authException) -> {
                    var e = new UserForbiddenException("Authentication failed!");
                    response.setStatus(e.status().value());
                    response.setContentType("application/json; charset=utf-8");
                    ObjectMapper objMapper = new ObjectMapper();
                    String responseBody = objMapper.writeValueAsString(e.body());
                    response.getWriter().println(responseBody);
                })));

        // 9. Handle authorization failures
        http.exceptionHandling(handling ->
                handling.accessDeniedHandler(((request, response, accessDeniedException) -> {
                    var e = new UserForbiddenException("Access denied!");
                    response.setStatus(e.status().value());
                    response.setContentType("application/json; charset=utf-8");
                    ObjectMapper om = new ObjectMapper();
                    String responseBody = om.writeValueAsString(e.body());
                    response.getWriter().println(responseBody);
                })));

        // 10. Configure authentication and authorization filters
        http.authorizeHttpRequests(authorize ->
                authorize.requestMatchers("/api/chatrooms/**", "/api/logout").hasRole("USER")
                        .requestMatchers("/api/email-verifications/**", "/api/users/**").hasAnyRole("PENDING", "USER")
                        .requestMatchers("/api/validate/**", "/api/help/**").permitAll()
                        .anyRequest().permitAll()
        );

        return http.build();
    }

    public CorsConfigurationSource configurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*"); // GET, POST, PUT, DELETE (Allow Javascript requests)
        configuration.setAllowedOrigins(Configs.CORS);
        configuration.setAllowCredentials(true); // Allow cookie requests from clients
        configuration.addExposedHeader("Authorization"); // Expose Authorization header
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

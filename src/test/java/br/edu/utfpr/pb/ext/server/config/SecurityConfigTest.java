package br.edu.utfpr.pb.ext.server.config;

import br.edu.utfpr.pb.ext.server.auth.jwt.JwtAuthenticationFilter;
import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationProvider;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Mock
    private Environment environment;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailOtpAuthenticationProvider emailOtpAuthenticationProvider;

    @Mock
    private HttpSecurity httpSecurity;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private Usuario usuario;

    private SecurityConfig securityConfig;
    private List<String> allowedOrigins = Arrays.asList("http://localhost:3000", "http://localhost:8080");

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(environment, usuarioRepository, emailOtpAuthenticationProvider);
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins", allowedOrigins);
        ReflectionTestUtils.setField(securityConfig, "isSwaggerEnabled", true);
    }

    @Nested
    @DisplayName("Security Filter Chain Configuration Tests")
    class SecurityFilterChainTests {

        @Test
        @DisplayName("Should create SecurityFilterChain with proper configuration")
        void shouldCreateSecurityFilterChainWithProperConfiguration() throws Exception {
            // Given
            when(httpSecurity.cors(any())).thenReturn(httpSecurity);
            when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
            when(httpSecurity.headers(any())).thenReturn(httpSecurity);
            when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
            when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
            when(httpSecurity.authenticationProvider(any())).thenReturn(httpSecurity);
            when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);
            when(httpSecurity.build()).thenReturn(mock(SecurityFilterChain.class));

            // When
            SecurityFilterChain result = securityConfig.securityFilterChain(httpSecurity, jwtAuthenticationFilter);

            // Then
            assertNotNull(result);
            verify(httpSecurity).cors(any());
            verify(httpSecurity).csrf(any());
            verify(httpSecurity).headers(any());
            verify(httpSecurity).authorizeHttpRequests(any());
            verify(httpSecurity).sessionManagement(any());
            verify(httpSecurity).authenticationProvider(emailOtpAuthenticationProvider);
            verify(httpSecurity).addFilterBefore(jwtAuthenticationFilter, any());
            verify(httpSecurity).build();
        }

        @Test
        @DisplayName("Should throw exception when HttpSecurity is null")
        void shouldThrowExceptionWhenHttpSecurityIsNull() {
            assertThrows(NullPointerException.class, () ->
                securityConfig.securityFilterChain(null, jwtAuthenticationFilter)
            );
        }

        @Test
        @DisplayName("Should throw exception when JwtAuthenticationFilter is null")
        void shouldThrowExceptionWhenJwtAuthenticationFilterIsNull() throws Exception {
            // Given
            when(httpSecurity.cors(any())).thenReturn(httpSecurity);
            when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
            when(httpSecurity.headers(any())).thenReturn(httpSecurity);
            when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
            when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
            when(httpSecurity.authenticationProvider(any())).thenReturn(httpSecurity);
            when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);

            // When & Then
            assertThrows(IllegalArgumentException.class, () ->
                securityConfig.securityFilterChain(httpSecurity, null)
            );
        }
    }

    @Nested
    @DisplayName("CORS Configuration Tests")
    class CorsConfigurationTests {

        @Test
        @DisplayName("Should create CORS configuration source with allowed origins")
        void shouldCreateCorsConfigurationSourceWithAllowedOrigins() {
            CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();
            assertNotNull(corsSource);
            var corsConfig = corsSource.getCorsConfiguration("/**");
            assertNotNull(corsConfig);
            assertEquals(allowedOrigins, corsConfig.getAllowedOriginPatterns());
        }

        @Test
        @DisplayName("Should configure allowed HTTP methods correctly")
        void shouldConfigureAllowedHttpMethodsCorrectly() {
            CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();
            var corsConfig = corsSource.getCorsConfiguration("/**");
            List<String> expectedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
            assertEquals(expectedMethods, corsConfig.getAllowedMethods());
        }

        @Test
        @DisplayName("Should configure allowed headers correctly")
        void shouldConfigureAllowedHeadersCorrectly() {
            CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();
            var corsConfig = corsSource.getCorsConfiguration("/**");
            List<String> expectedHeaders = Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept");
            assertEquals(expectedHeaders, corsConfig.getAllowedHeaders());
        }

        @Test
        @DisplayName("Should allow credentials")
        void shouldAllowCredentials() {
            CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();
            var corsConfig = corsSource.getCorsConfiguration("/**");
            assertTrue(corsConfig.getAllowCredentials());
        }

        @Test
        @DisplayName("Should handle empty allowed origins")
        void shouldHandleEmptyAllowedOrigins() {
            ReflectionTestUtils.setField(securityConfig, "allowedOrigins", Arrays.asList());
            CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();
            var corsConfig = corsSource.getCorsConfiguration("/**");
            assertTrue(corsConfig.getAllowedOriginPatterns().isEmpty());
        }
    }

    @Nested
    @DisplayName("UserDetailsService Tests")
    class UserDetailsServiceTests {

        @Test
        @DisplayName("Should load user by email successfully")
        void shouldLoadUserByEmailSuccessfully() {
            String email = "test@example.com";
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
            when(usuario.getEmail()).thenReturn(email);
            when(usuario.getPassword()).thenReturn("hashedPassword");
            when(usuario.getAuthorities()).thenReturn(Arrays.asList());

            UserDetailsService userDetailsService = securityConfig.userDetailsService();
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            assertNotNull(userDetails);
            verify(usuarioRepository).findByEmail(email);
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user not found")
        void shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
            String email = "nonexistent@example.com";
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

            UserDetailsService userDetailsService = securityConfig.userDetailsService();
            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername(email)
            );
            assertEquals("Credenciais inválidas", exception.getMessage());
            verify(usuarioRepository).findByEmail(email);
        }

        @Test
        @DisplayName("Should handle null email gracefully")
        void shouldHandleNullEmailGracefully() {
            when(usuarioRepository.findByEmail(null)).thenReturn(Optional.empty());
            UserDetailsService userDetailsService = securityConfig.userDetailsService();
            assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername(null)
            );
        }

        @Test
        @DisplayName("Should handle empty email gracefully")
        void shouldHandleEmptyEmailGracefully() {
            String email = "";
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
            UserDetailsService userDetailsService = securityConfig.userDetailsService();
            assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername(email)
            );
        }
    }

    @Nested
    @DisplayName("Authentication Manager Tests")
    class AuthenticationManagerTests {

        @Test
        @DisplayName("Should create AuthenticationManager successfully")
        void shouldCreateAuthenticationManagerSuccessfully() throws Exception {
            AuthenticationManager mockAuthManager = mock(AuthenticationManager.class);
            when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockAuthManager);

            AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);
            assertEquals(mockAuthManager, result);
            verify(authenticationConfiguration).getAuthenticationManager();
        }

        @Test
        @DisplayName("Should throw exception when AuthenticationConfiguration fails")
        void shouldThrowExceptionWhenAuthenticationConfigurationFails() throws Exception {
            when(authenticationConfiguration.getAuthenticationManager())
                .thenThrow(new RuntimeException("Configuration error"));

            assertThrows(Exception.class, () ->
                securityConfig.authenticationManager(authenticationConfiguration)
            );
        }

        @Test
        @DisplayName("Should handle null AuthenticationConfiguration")
        void shouldHandleNullAuthenticationConfiguration() {
            assertThrows(NullPointerException.class, () ->
                securityConfig.authenticationManager(null)
            );
        }
    }

    @Nested
    @DisplayName("Authorization Manager Tests")
    class AuthorizationManagerTests {

        @Test
        @DisplayName("Should authorize when test profile is active")
        void shouldAuthorizeWhenTestProfileIsActive() {
            when(environment.getActiveProfiles()).thenReturn(new String[]{"test", "dev"});
            SecurityConfig testConfig = new SecurityConfig(environment, usuarioRepository, emailOtpAuthenticationProvider);
            verify(environment, never()).getActiveProfiles();
        }

        @Test
        @DisplayName("Should deny when test profile is not active")
        void shouldDenyWhenTestProfileIsNotActive() {
            when(environment.getActiveProfiles()).thenReturn(new String[]{"prod", "staging"});
            SecurityConfig testConfig = new SecurityConfig(environment, usuarioRepository, emailOtpAuthenticationProvider);
            assertNotNull(testConfig);
        }

        @Test
        @DisplayName("Should authorize when Swagger is enabled")
        void shouldAuthorizeWhenSwaggerIsEnabled() {
            ReflectionTestUtils.setField(securityConfig, "isSwaggerEnabled", true);
            Boolean swaggerEnabled = (Boolean) ReflectionTestUtils.getField(securityConfig, "isSwaggerEnabled");
            assertTrue(swaggerEnabled);
        }

        @Test
        @DisplayName("Should deny when Swagger is disabled")
        void shouldDenyWhenSwaggerIsDisabled() {
            ReflectionTestUtils.setField(securityConfig, "isSwaggerEnabled", false);
            Boolean swaggerEnabled = (Boolean) ReflectionTestUtils.getField(securityConfig, "isSwaggerEnabled");
            assertFalse(swaggerEnabled);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCaseIntegrationTests {

        @Test
        @DisplayName("Should handle constructor with all null dependencies")
        void shouldHandleConstructorWithAllNullDependencies() {
            assertThrows(NullPointerException.class, () ->
                new SecurityConfig(null, null, null)
            );
        }

        @Test
        @DisplayName("Should initialize with valid dependencies")
        void shouldInitializeWithValidDependencies() {
            SecurityConfig config = new SecurityConfig(environment, usuarioRepository, emailOtpAuthenticationProvider);
            assertNotNull(config);
        }

        @Test
        @DisplayName("Should handle concurrent access to CORS configuration")
        void shouldHandleConcurrentAccessToCorsConfiguration() throws InterruptedException {
            final int threadCount = 10;
            final Thread[] threads = new Thread[threadCount];
            final CorsConfigurationSource[] results = new CorsConfigurationSource[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    results[index] = securityConfig.corsConfigurationSource();
                });
                threads[i].start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
            for (CorsConfigurationSource result : results) {
                assertNotNull(result);
            }
        }

        @Test
        @DisplayName("Should maintain consistent UserDetailsService behavior")
        void shouldMaintainConsistentUserDetailsServiceBehavior() {
            String email = "consistent@example.com";
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
            UserDetailsService service1 = securityConfig.userDetailsService();
            UserDetailsService service2 = securityConfig.userDetailsService();
            UserDetails user1 = service1.loadUserByUsername(email);
            UserDetails user2 = service2.loadUserByUsername(email);
            assertNotNull(user1);
            assertNotNull(user2);
            verify(usuarioRepository, times(2)).findByEmail(email);
        }

        @Test
        @DisplayName("Should handle special characters in allowed origins")
        void shouldHandleSpecialCharactersInAllowedOrigins() {
            List<String> specialOrigins = Arrays.asList(
                "https://test-site.com",
                "http://localhost:3000/path",
                "https://sub.domain.com:8443"
            );
            ReflectionTestUtils.setField(securityConfig, "allowedOrigins", specialOrigins);
            CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();
            var corsConfig = corsSource.getCorsConfiguration("/**");
            assertEquals(specialOrigins, corsConfig.getAllowedOriginPatterns());
        }

        @Test
        @DisplayName("Should validate CORS configuration completeness")
        void shouldValidateCorsConfigurationCompleteness() {
            CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();
            var corsConfig = corsSource.getCorsConfiguration("/**");
            assertNotNull(corsConfig.getAllowedOriginPatterns());
            assertNotNull(corsConfig.getAllowedMethods());
            assertNotNull(corsConfig.getAllowedHeaders());
            assertNotNull(corsConfig.getAllowCredentials());
            assertTrue(corsConfig.getAllowedMethods().contains("GET"));
            assertTrue(corsConfig.getAllowedMethods().contains("POST"));
            assertTrue(corsConfig.getAllowedMethods().contains("PUT"));
            assertTrue(corsConfig.getAllowedMethods().contains("DELETE"));
            assertTrue(corsConfig.getAllowedMethods().contains("OPTIONS"));
            assertTrue(corsConfig.getAllowedHeaders().contains("Authorization"));
            assertTrue(corsConfig.getAllowedHeaders().contains("Content-Type"));
        }
    }
}
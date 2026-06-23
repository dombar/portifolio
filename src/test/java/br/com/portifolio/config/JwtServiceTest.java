package br.com.portifolio.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-with-at-least-32-bytes!");
        properties.setExpiration(Duration.ofHours(1));
        jwtService = new JwtService(properties);
    }

    @Test
    @DisplayName("should generate token with username and validate it")
    void shouldGenerateAndValidateToken() {
        String token = jwtService.generateToken("admin");

        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    @DisplayName("should reject malformed token")
    void shouldRejectInvalidToken() {
        assertThat(jwtService.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("should reject token signed with different secret")
    void shouldRejectTokenSignedWithDifferentSecret() {
        JwtProperties otherProperties = new JwtProperties();
        otherProperties.setSecret("another-secret-key-with-32-chars-min!");
        otherProperties.setExpiration(Duration.ofHours(1));

        String token = new JwtService(otherProperties).generateToken("admin");

        assertThat(jwtService.validateToken(token)).isFalse();
    }
}

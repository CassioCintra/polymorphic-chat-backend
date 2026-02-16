package io.cassio.polymorphic.auth.infrastructure.jwt;

import io.cassio.polymorphic.auth.application.port.TokenVerifier;
import io.cassio.polymorphic.auth.domain.exception.TokenException;
import io.cassio.polymorphic.auth.infrastructure.config.JwtProperties;
import io.cassio.polymorphic.auth.infrastructure.config.TokenProperties;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {
    private static final String SECRET_1 = "OPGUxztUJ0+mj5rWoolwe+bxQ7glv3GpLyIrC4PIEMU=";
    private static final String SECRET_2 = "4FZ9xqN7J0s3c1G9yJk0vPZg2oGm+Qy3o0nQYHq4y5E=";

    private JwtTokenService newService(String secretBase64, long accessTtl, long refreshTtl) {
        JwtProperties jwtProps = new JwtProperties(secretBase64);

        TokenProperties tokenProps = new TokenProperties(
                new TokenProperties.Access(accessTtl),
                new TokenProperties.Refresh(refreshTtl)
        );

        return new JwtTokenService(jwtProps, tokenProps);
    }

    @Test
    void shouldIssueAndVerifyAccessToken() {
        JwtTokenService service = newService(SECRET_1, 60, 120);

        String jwt = service.issueAccess("uuid-1", "jti-1");

        var verified = service.verify(jwt);
        assertThat(verified.uuid()).isEqualTo("uuid-1");
        assertThat(verified.jti()).isEqualTo("jti-1");
        assertThat(verified.type()).isEqualTo(TokenVerifier.TokenType.ACCESS);
    }

    @Test
    void shouldIssueAndVerifyRefreshToken() {
        JwtTokenService service = newService(SECRET_1, 60, 120);

        String jwt = service.issueRefresh("uuid-1", "jti-2");

        var verified = service.verify(jwt);
        assertThat(verified.uuid()).isEqualTo("uuid-1");
        assertThat(verified.jti()).isEqualTo("jti-2");
        assertThat(verified.type()).isEqualTo(TokenVerifier.TokenType.REFRESH);
    }

    @Test
    void shouldFailVerificationWithDifferentSecret() {
        JwtTokenService issuer = newService(SECRET_1, 60, 120);
        JwtTokenService verifierOtherKey = newService(SECRET_2, 60, 120);

        String jwt = issuer.issueAccess("uuid-1", "jti-1");

        assertThatThrownBy(() -> verifierOtherKey.verify(jwt))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void shouldFailWhenTypeClaimIsMissing() {
        JwtTokenService service = newService(SECRET_1, 60, 120);

        String jwtWithoutType = io.jsonwebtoken.Jwts.builder()
                .subject("uuid-1")
                .id("jti-1")
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(
                        io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                                io.jsonwebtoken.io.Decoders.BASE64.decode(SECRET_1)
                        )
                )
                .compact();

        assertThatThrownBy(() -> service.verify(jwtWithoutType))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("Missing type claim");
    }

    @Test
    void shouldFailWhenTypeClaimIsInvalid() {
        JwtTokenService service = newService(SECRET_1, 60, 120);

        String jwtInvalidType = io.jsonwebtoken.Jwts.builder()
                .subject("uuid-1")
                .id("jti-1")
                .claim("type", "whatever")
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(
                        io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                                io.jsonwebtoken.io.Decoders.BASE64.decode(SECRET_1)
                        )
                )
                .compact();

        assertThatThrownBy(() -> service.verify(jwtInvalidType))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("Invalid type claim");
    }
}
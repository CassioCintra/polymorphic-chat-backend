package io.cassio.polymorphic.auth.infrastructure.jwt;

import io.cassio.polymorphic.auth.application.port.TokenIssuer;
import io.cassio.polymorphic.auth.application.port.TokenVerifier;
import io.cassio.polymorphic.auth.domain.exception.TokenException;
import io.cassio.polymorphic.auth.infrastructure.config.JwtProperties;
import io.cassio.polymorphic.auth.infrastructure.config.TokenProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenService implements TokenIssuer, TokenVerifier {

    private final SecretKey key;
    private final long accessTtl;
    private final long refreshTtl;

    public JwtTokenService(JwtProperties jwtProps, TokenProperties tokenProps) {
        this.accessTtl = tokenProps.access().expiration();
        this.refreshTtl = tokenProps.refresh().expiration();

        byte[] keyBytes = Decoders.BASE64.decode(jwtProps.secret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String issueAccess(String uuid, String jti) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(uuid)
                .id(jti)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtl)))
                .signWith(key)
                .compact();
    }

    @Override
    public String issueRefresh(String uuid, String jti) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(uuid)
                .id(jti)
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtl)))
                .signWith(key)
                .compact();
    }

    @Override
    public TokenVerified verify(String jwt) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        String subject = claims.getSubject();
        String jti = claims.getId();

        String type = claims.get("type", String.class);
        if (type == null) throw TokenException.missingClaim();

        TokenType tokenType = getTokenType(type);

        return new TokenVerified(subject, jti, tokenType);
    }

    private TokenType getTokenType(String type) {
        return switch (type.toLowerCase()) {
            case "access" -> TokenType.ACCESS;
            case "refresh" -> TokenType.REFRESH;
            default -> throw TokenException.invalidClaim(type);
        };
    }
}

package io.cassio.polymorphic.auth.domain.exception;

public class TokenException extends RuntimeException {
    private final String code;

    private TokenException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() { return code; }

    public static TokenException invalidTokenType() {
        return new TokenException("AUTH_INVALID_TOKEN_TYPE", "Invalid token type");
    }

    public static TokenException refreshTokenRevoked() {
        return new TokenException("AUTH_REFRESH_REVOKED", "Refresh token revoked");
    }

    public static TokenException invalidRefreshTtl() {
        return new TokenException("AUTH_REFRESH_TTL_INVALID", "refreshTTL must be > 0");
    }

    public static TokenException invalidClaim(String type) {
        return new TokenException("AUTH_INVALID_CLAIM", "Invalid type claim: " + type);
    }

    public static TokenException missingClaim() {
        return new TokenException("AUTH_MISSING_CLAIM", "Missing type claim");
    }
}

package io.cassio.polymorphic.auth.application.port;

public interface TokenVerifier {

    TokenVerified verify(String tokenJwt);

    enum TokenType{ ACCESS, REFRESH }

    record TokenVerified(
        String uuid,
        String jti,
        TokenType type
    ){}
}

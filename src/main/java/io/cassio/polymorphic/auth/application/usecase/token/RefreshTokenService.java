package io.cassio.polymorphic.auth.application.usecase.token;

import io.cassio.polymorphic.auth.application.port.RefreshTokenStore;
import io.cassio.polymorphic.auth.application.port.TokenIssuer;
import io.cassio.polymorphic.auth.application.port.TokenVerifier;
import io.cassio.polymorphic.auth.domain.exception.TokenException;
import io.cassio.polymorphic.auth.infrastructure.config.TokenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final TokenVerifier tokenVerifier;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenProperties tokenProperties;

    public record Result(String access, String refresh) {}

    public Mono<Result> execute(String refreshJwt) {
        Long refreshTtl = tokenProperties.refresh().expiration();

        return Mono.fromSupplier(() -> tokenVerifier.verify(refreshJwt))
                .flatMap(token -> {
                    if (token.type() != TokenVerifier.TokenType.REFRESH) {
                        return Mono.error(TokenException.invalidTokenType());
                    }
                    return refreshTokenStore.exists(token.jti())
                            .flatMap(exists -> exists
                                    ? Mono.just(token)
                                    : Mono.error(TokenException.refreshTokenRevoked()));
                })
                .flatMap(verified -> {
                    String newAccessJti = UUID.randomUUID().toString();
                    String newRefreshJti = UUID.randomUUID().toString();

                    String newAccess = tokenIssuer.issueAccess(verified.uuid(), newAccessJti);
                    String newRefresh = tokenIssuer.issueRefresh(verified.uuid(), newRefreshJti);

                    return refreshTokenStore.revoke(verified.jti())
                            .then(refreshTokenStore.store(newRefreshJti, verified.uuid(), refreshTtl))
                            .thenReturn(new Result(newAccess, newRefresh));
                });
    }
}



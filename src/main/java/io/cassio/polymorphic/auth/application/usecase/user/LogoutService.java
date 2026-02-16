package io.cassio.polymorphic.auth.application.usecase.user;

import io.cassio.polymorphic.auth.application.port.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshTokenStore refreshTokenStore;

    public Mono<Void> device(String refreshJti) {
        return refreshTokenStore.revoke(refreshJti);
    }

    public Mono<Void> all(String subjectUuid) {
        return refreshTokenStore.revokeAll(subjectUuid);
    }
}


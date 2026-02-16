package io.cassio.polymorphic.auth.application.usecase.user;

import io.cassio.polymorphic.auth.application.port.RefreshTokenStore;
import io.cassio.polymorphic.auth.application.port.TokenIssuer;
import io.cassio.polymorphic.auth.application.port.UserAuthQuery;
import io.cassio.polymorphic.auth.domain.exception.CredentialException;
import io.cassio.polymorphic.auth.domain.model.LoginCommand;
import io.cassio.polymorphic.auth.domain.model.LoginResult;
import io.cassio.polymorphic.auth.infrastructure.config.TokenProperties;
import io.cassio.polymorphic.user.application.port.PasswordHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserAuthQuery userAuthQuery;
    private final PasswordHashService passwordHashService;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenProperties tokenProperties;

    public Mono<LoginResult> execute(LoginCommand command) {
        return userAuthQuery.findByLogin(command.login())
                .switchIfEmpty(Mono.error(CredentialException.invalidCredentials()))
                .flatMap(user -> {
                    boolean ok = passwordHashService.matches(command.password(), user.hashedPassword());
                    if (!ok) return Mono.error(CredentialException.invalidCredentials());
                    String accessJti = UUID.randomUUID().toString();
                    String refreshJti = UUID.randomUUID().toString();

                    String accessToken = tokenIssuer.issueAccess(user.uuid(), accessJti);
                    String refreshToken = tokenIssuer.issueRefresh(user.uuid(), refreshJti);

                    return refreshTokenStore
                            .store(refreshJti, user.uuid(), tokenProperties.refresh().expiration())
                            .thenReturn(
                                    new LoginResult(
                                            accessToken,
                                            tokenProperties.access().expiration(),
                                            refreshToken,
                                            tokenProperties.refresh().expiration()
                                    )
                            );
                }
        );
    }

}

package io.cassio.polymorphic.user.infrastructure.bcrypt;

import io.cassio.polymorphic.user.application.port.PasswordHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BCryptPasswordHashService implements PasswordHashService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public String hash(String rawPassword) {
        return bCryptPasswordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, hashedPassword);
    }
}
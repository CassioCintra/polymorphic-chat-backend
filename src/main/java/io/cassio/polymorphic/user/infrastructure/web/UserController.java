package io.cassio.polymorphic.user.infrastructure.web;

import io.cassio.polymorphic.user.application.usecase.RegisterUser;
import io.cassio.polymorphic.user.infrastructure.web.dto.RegisterUserRequest;
import io.cassio.polymorphic.user.infrastructure.web.dto.RegisterUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUser registerUser;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RegisterUserResponse> registerUser(@RequestBody @Valid RegisterUserRequest request){
        log.info("Request Received: POST - /api/user/register");
        return registerUser.execute(request.toRegisterUserCommand())
                .map(RegisterUserResponse::from)
                .doOnNext(resp ->
                        log.info("Request response:\n{}", resp)
                );

    }
}

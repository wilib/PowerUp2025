package co.com.pragma.api;

import co.com.pragma.api.dto.UserRequestDTO;
import co.com.pragma.api.exception.GlobalErrorHandler;
import co.com.pragma.model.user.User;
import co.com.pragma.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final UserUseCase userUseCase;
    private final GlobalErrorHandler errorHandler;

    public Mono<ServerResponse> save(ServerRequest serverRequest) {
        log.info("[POST /api/v1/usuarios] - Petición recibida");
        return serverRequest
                .bodyToMono(UserRequestDTO.class)
                .map(this::mapDtoToDomain)
                .flatMap(userUseCase::createUser)
                .flatMap(userCreated -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(userCreated)
                )
                .onErrorResume(ex -> errorHandler.handle(ex, serverRequest));
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        boolean onlyIds = request.queryParam("fields")
                .map(v -> v.equalsIgnoreCase("ids")).orElse(false);
        if (onlyIds) {
            return userUseCase.list()
                    .map(User::getUserId)
                    .collectList()
                    .flatMap(ids -> ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ids));
        }
        return userUseCase.list()
                .collectList()
                .flatMap(users -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(users));
    }

    private User mapDtoToDomain(UserRequestDTO dto) {
        return new User(
                null,
                dto.getName(),
                dto.getLastName(),
                dto.getBirthDate(),
                dto.getAddress(),
                dto.getPhoneNumber(),
                dto.getEmail(),
                dto.getBaseSalary(),
                dto.getIdentityDocument(),
                dto.getRole() != null ? new co.com.pragma.model.role.Role(dto.getRole().getRoleId(), null) : null
        );
    }
}

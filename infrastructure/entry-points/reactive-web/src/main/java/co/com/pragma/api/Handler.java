package co.com.pragma.api;

import co.com.pragma.api.dto.UserRequestDTO;
import co.com.pragma.model.user.User;
import co.com.pragma.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {
    private final UserUseCase userUseCase;

    public Mono<ServerResponse> save(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(UserRequestDTO.class)
                .map(this::mapDtoToDomain)
                .flatMap(userUseCase::createUser)
                .flatMap(userCreated -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(userCreated)
                )
                .onErrorResume(ex -> {
                    boolean debug = isDebugEnabled(serverRequest);
                    String path = serverRequest.path();
                    // Domain validation vs domain-level conflicts
                    if (ex instanceof IllegalArgumentException) {
                        String msg = ex.getMessage() == null ? "Validation error" : ex.getMessage();
                        int status = (msg.contains("already registered")) ? 409 : 400;
                        return ServerResponse.status(status).contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(errorBody(status, msg, path, ex, serverRequest, debug));
                    }
                    // SQLState/errorCode based mapping for R2DBC exceptions
                    if (ex instanceof io.r2dbc.spi.R2dbcException re) {
                        String state = re.getSqlState();
                        int code = re.getErrorCode();
                        if ("23000".equals(state) || code == 1062) {
                            return ServerResponse.status(409).contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(errorBody(409, "Duplicate key (email or identityDocument)", path, ex, serverRequest, debug));
                        }
                    }
                    // Data integrity violations (unique, not null, FK) -> 409
                    if (ex instanceof org.springframework.dao.DataIntegrityViolationException
                            || ex instanceof io.r2dbc.spi.R2dbcDataIntegrityViolationException) {
                        return ServerResponse.status(409).contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(errorBody(409, "Duplicate or data integrity violation (email or identityDocument)", path, ex, serverRequest, debug));
                    }
                    String msg = ex.getMessage() != null ? ex.getMessage() : "Unexpected error";
                    // Duplicate keys from MySQL often mention 'Duplicate' or constraint name
                    if (msg.toLowerCase().contains("duplicate") || msg.toLowerCase().contains("unique")
                            || msg.toLowerCase().contains("constraint") || msg.toLowerCase().contains("key")) {
                        return ServerResponse.status(409).contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(errorBody(409, "Duplicate key (email or identityDocument)", path, ex, serverRequest, debug));
                    }
                    return ServerResponse.status(500).contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(errorBody(500, "Internal Server Error", path, ex, serverRequest, debug));
                })
                .doOnError(ex -> {
                    // Log exception to console for quick diagnosis in dev
                    System.err.println("[POST /api/v1/usuarios] Error: " + ex.getClass().getName() + " - " + ex.getMessage());
                });
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        String id = request.pathVariable("id");
        return userUseCase.getById(id)
                .flatMap(user -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(user))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        boolean onlyIds = request.queryParam("fields")
                .map(v -> v.equalsIgnoreCase("ids")).orElse(false);
        if (onlyIds) {
            return userUseCase.list()
                    .map(User::getUserId)
                    .collectList()
                    .flatMap(ids -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(ids));
        }
        return userUseCase.list()
                .collectList()
                .flatMap(users -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(users));
    }

    private co.com.pragma.model.user.User mapDtoToDomain(UserRequestDTO dto) {
        co.com.pragma.model.user.User u = new co.com.pragma.model.user.User(
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
        u.setName(dto.getName());
        u.setLastName(dto.getLastName());
        u.setEmail(dto.getEmail());
        u.setBaseSalary(dto.getBaseSalary());
        u.setBirthDate(dto.getBirthDate());
        return u;
    }

    private java.util.Map<String, Object> errorBody(int status, String message, String path, Throwable ex, ServerRequest request, boolean debug) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("timestamp", java.time.OffsetDateTime.now().toString());
        map.put("status", status);
        map.put("error", message);
        map.put("path", path);
        if (debug && ex != null) {
            map.put("exceptionClass", ex.getClass().getName());
            map.put("causeMessage", ex.getMessage());
            // Add SQL diagnostics when available
            if (ex instanceof io.r2dbc.spi.R2dbcException re) {
                if (re.getSqlState() != null) map.put("sqlState", re.getSqlState());
                map.put("errorCode", re.getErrorCode());
            }
            try {
                String requestId = request.exchange().getRequest().getId();
                if (requestId != null) map.put("requestId", requestId);
            } catch (Throwable ignored) {}
        }
        return map;
    }

    private boolean isDebugEnabled(ServerRequest request) {
        // Enable detailed errors if active profile contains 'dev' or env DEBUG_ERRORS=true
        String debugEnv = System.getenv("DEBUG_ERRORS");
        if (debugEnv != null && ("true".equalsIgnoreCase(debugEnv) || "1".equals(debugEnv))) return true;
        String profiles = System.getProperty("spring.profiles.active", "");
        if (profiles.toLowerCase().contains("dev")) return true;
        // Also allow checking request attribute set by frameworks
        Object attr = request.attributes().get("profile");
        return attr != null && attr.toString().toLowerCase().contains("dev");
    }
}

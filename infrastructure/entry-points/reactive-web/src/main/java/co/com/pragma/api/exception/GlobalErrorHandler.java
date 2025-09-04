package co.com.pragma.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Order(-2)
public class GlobalErrorHandler {

    public Mono<ServerResponse> handle(Throwable ex, ServerRequest request) {
        boolean debug = isDebugEnabled(request);
        String path = request.path();

        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String message = ex.getMessage() != null ? ex.getMessage() : "Unexpected error";

        // ---- Clasificación de errores ----
        if (ex instanceof IllegalArgumentException) {
            message = ex.getMessage() == null ? "Validation error" : ex.getMessage();
            status = (message.contains("already registered")) ? 409 : 400;
        } else if (ex instanceof io.r2dbc.spi.R2dbcException re) {
            String state = re.getSqlState();
            int code = re.getErrorCode();
            if ("23000".equals(state) || code == 1062) {
                status = 409;
                message = "Duplicate key (email or identityDocument)";
            }
        } else if (ex instanceof org.springframework.dao.DataIntegrityViolationException
                || ex instanceof io.r2dbc.spi.R2dbcDataIntegrityViolationException) {
            status = 409;
            message = "Duplicate or data integrity violation (email or identityDocument)";
        } else if (message.toLowerCase().contains("duplicate")
                || message.toLowerCase().contains("unique")
                || message.toLowerCase().contains("constraint")
                || message.toLowerCase().contains("key")) {
            status = 409;
            message = "Duplicate key (email or identityDocument)";
        }

        log.error("Error en petición [{}]: {}", path, message, ex);

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorBody(status, message, path, ex, request, debug));
    }

    private Map<String, Object> errorBody(int status, String message, String path, Throwable ex,
                                          ServerRequest request, boolean debug) {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", OffsetDateTime.now().toString());
        map.put("status", status);
        map.put("error", message);
        map.put("path", path);

        if (debug && ex != null) {
            map.put("exceptionClass", ex.getClass().getName());
            map.put("causeMessage", ex.getMessage());
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
        String debugEnv = System.getenv("DEBUG_ERRORS");
        if (debugEnv != null && ("true".equalsIgnoreCase(debugEnv) || "1".equals(debugEnv))) return true;
        String profiles = System.getProperty("spring.profiles.active", "");
        if (profiles.toLowerCase().contains("dev")) return true;
        Object attr = request.attributes().get("profile");
        return attr != null && attr.toString().toLowerCase().contains("dev");
    }
}

package co.com.pragma.api;

import co.com.pragma.model.user.User;
import co.com.pragma.usecase.user.UserUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @Mock
    private UserUseCase userUseCase;

    @Test
    void testGetUserById_NotFound() {
        Mockito.when(userUseCase.getById("123")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/usuarios/{id}", "123")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testCreateUser_Success() {
        User input = User.builder()
                .name("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        User created = input.toBuilder().userId("u-1").build();

        Mockito.when(userUseCase.createUser(Mockito.any(User.class))).thenReturn(Mono.just(created));

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("{" +
                        "\"name\":\"John\"," +
                        "\"lastName\":\"Doe\"," +
                        "\"email\":\"john.doe@example.com\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo("u-1")
                .jsonPath("$.name").isEqualTo("John");
    }
}

package co.com.pragma.r2dbc;

import co.com.pragma.model.user.User;
import co.com.pragma.r2dbc.entity.UserEntity;
import co.com.pragma.r2dbc.mapper.UserEntityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyReactiveRepositoryAdapterTest {
    // TODO: change four you own tests

    @InjectMocks
    MyReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    MyReactiveRepository repository;

    @Mock
    UserEntityMapper mapper;

    @Test
    void mustFindValueById() {
        UserEntity entity = UserEntity.builder().userId("1").name("John").build();
        User domain = User.builder().userId("1").name("John").build();
        when(repository.findById("1")).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Mono<User> result = repositoryAdapter.findById("1");

        StepVerifier.create(result)
                .expectNextMatches(value -> value.getUserId().equals("1"))
                .verifyComplete();
    }

    @Test
    void mustFindAllValues() {
        UserEntity entity = UserEntity.builder().userId("1").name("John").build();
        User domain = User.builder().userId("1").name("John").build();
        when(repository.findAll()).thenReturn(Flux.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Flux<User> result = repositoryAdapter.findAll();

        StepVerifier.create(result)
                .expectNextMatches(value -> value.getUserId().equals("1"))
                .verifyComplete();
    }

    @Test
    void mustSaveValue() {
        User input = User.builder().name("John").build();
        UserEntity data = UserEntity.builder().name("John").build();
        UserEntity saved = UserEntity.builder().userId("1").name("John").build();
        User expected = User.builder().userId("1").name("John").build();

        when(mapper.toEntity(any(User.class))).thenReturn(data);
        when(repository.save(data)).thenReturn(Mono.just(saved));
        when(mapper.toDomain(saved)).thenReturn(expected);

        Mono<User> result = repositoryAdapter.save(input);

        StepVerifier.create(result)
                .expectNextMatches(value -> value.getUserId().equals("1"))
                .verifyComplete();
    }
}

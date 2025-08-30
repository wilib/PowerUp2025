package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> save(User user);
    Mono<User> findByEmail(String email);
    Mono<User> findByIdentityDocument(String identityDocument);
    Mono<User> findById(String id);
    Flux<User> findAll();
}

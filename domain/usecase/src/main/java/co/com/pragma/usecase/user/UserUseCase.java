package co.com.pragma.usecase.user;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class UserUseCase {
    private final UserRepository userRepository;

    public Mono<User> createUser(User user) {
        return validateUser(user)
                .flatMap(valid ->
                        userRepository.findByEmail(valid.getEmail())
                                .flatMap(existing -> Mono.<User>error(new IllegalArgumentException("Email already registered")))
                                .switchIfEmpty(
                                        userRepository.findByIdentityDocument(valid.getIdentityDocument())
                                                .flatMap(existing -> Mono.<User>error(new IllegalArgumentException("Identity document already registered")))
                                )
                                .switchIfEmpty(Mono.defer(() -> {
                                    // Ensure userId is present since DB does not auto-generate it
                                    if (valid.getUserId() == null || valid.getUserId().isBlank()) {
                                        valid.setUserId(java.util.UUID.randomUUID().toString());
                                    }
                                    return userRepository.save(valid);
                                }))
                );
    }

    public Mono<User> getById(String id) {
        return userRepository.findById(id);
    }

    public Flux<User> list() {
        return userRepository.findAll();
    }

    private Mono<User> validateUser(User user) {
        if (user.getName() == null || user.getName().isEmpty()
                || user.getLastName() == null || user.getLastName().isEmpty()
                || user.getEmail() == null || user.getEmail().isEmpty()
                || user.getBaseSalary() == null
                || user.getIdentityDocument() == null || user.getIdentityDocument().isBlank()) {
            return Mono.error(new IllegalArgumentException("Required fields empty"));
        }

        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return Mono.error(new IllegalArgumentException("Invalid email format"));
        }

        if (user.getBaseSalary().compareTo(BigDecimal.ZERO) < 0 || user.getBaseSalary().compareTo(new BigDecimal("15000000")) > 0) {
            return Mono.error(new IllegalArgumentException("Salary out of range"));
        }

        return Mono.just(user);
    }
}

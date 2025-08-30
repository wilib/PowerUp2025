package co.com.pragma.r2dbc;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.r2dbc.entity.UserEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import co.com.pragma.r2dbc.mapper.UserEntityMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        String,
        MyReactiveRepository
        > implements UserRepository {

    private final MyReactiveRepository reactiveRepository;
    private final UserEntityMapper mapperEntity;

    public MyReactiveRepositoryAdapter(MyReactiveRepository reactiveRepository, UserEntityMapper mapperEntity) {
        super(reactiveRepository, null, null);
        this.reactiveRepository = reactiveRepository;
        this.mapperEntity = mapperEntity;
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return reactiveRepository.findByEmail(email)
                .map(mapperEntity::toDomain);
    }

    @Override
    public Mono<User> findByIdentityDocument(String identityDocument) {
        return reactiveRepository.findByIdentityDocument(identityDocument)
                .map(mapperEntity::toDomain);
    }

    @Override
    public Mono<User> findById(String id) {
        return reactiveRepository.findById(id)
                .map(mapperEntity::toDomain);
    }

    @Override
    public Flux<User> findAll() {
        return reactiveRepository.findAll().map(mapperEntity::toDomain);
    }

    @Override
    public Mono<User> save(User user) {
        UserEntity data = mapperEntity.toEntity(user);
        // Ensure the entity is treated as new so R2DBC performs INSERT, not UPDATE
        if (data != null) {
            data.setNew(true);
        }

        return reactiveRepository.save(data).map(mapperEntity::toDomain);
    }
}

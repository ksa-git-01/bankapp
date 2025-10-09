package ru.yandex.practicum.accounts.repository;

import org.springframework.data.repository.CrudRepository;
import ru.yandex.practicum.accounts.model.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

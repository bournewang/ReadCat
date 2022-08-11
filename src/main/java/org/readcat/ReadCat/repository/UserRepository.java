package org.readcat.ReadCat.repository;

import org.readcat.ReadCat.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByEmail(String email) ;
    Boolean existsByEmail(String email);
}

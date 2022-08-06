package org.readcat.ReadCat.repository;

import org.readcat.ReadCat.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {
    public List<User> findByEmail(String email) ;
}

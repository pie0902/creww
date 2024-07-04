package org.example.creww.user.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.example.creww.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u.username FROM User u WHERE u.id = :id")
    Optional<String> findUsernameById(@Param("id") Long id);
    List<User> findByEmailContaining(String email);

    @Query("SELECT u.id FROM User u WHERE u.id IN :userIds")
    List<Long> findAllUserIdsByIdIn(@Param("userIds") List<Long> userIds);

}

package com.exotech.kindmap.repository;

import com.exotech.kindmap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface UserRepo extends JpaRepository<User, String> {

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.subscribedGridIds")
    List<User> findAllWithSubscriptions();

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.subscribedGridIds " +
            "WHERE u.userId = :userId")
    Optional<User> findByIdWithSubscriptions(@Param("userId") String userId);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.subscribedGridIds " +
            "JOIN u.subscribedGridIds g " +
            "WHERE g.gridId = :gridId")
    List<User> findByGridIdWithSubscriptions(@Param("gridId") String gridId);

    long count();
}

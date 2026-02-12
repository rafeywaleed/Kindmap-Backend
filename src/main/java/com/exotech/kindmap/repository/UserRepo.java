package com.exotech.kindmap.repository;

import com.exotech.kindmap.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, String> {

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.subscribedGridIds")
    List<User> findAllWithSubscriptions();

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.subscribedGridIds")
    Page<User> findAllWithSubscriptions(Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.subscribedGridIds " +
            "WHERE u.userId = :userId")
    Optional<User> findByIdWithSubscriptions(@Param("userId") String userId);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.subscribedGridIds " +
            "JOIN u.subscribedGridIds g " +
            "WHERE g.gridId = :gridId")
    List<User> findByGridIdWithSubscriptions(@Param("gridId") String gridId);

    @Query("SELECT DISTINCT u FROM User u " +
            "WHERE LOWER(u.userId) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.token IS NOT NULL AND u.token != ''")
    long countUsersWithTokens();

    @Query("SELECT COUNT(u) FROM User u WHERE u.token IS NULL OR u.token = ''")
    long countUsersWithoutToken();

    @Query("SELECT u FROM User u ORDER BY u.joinedDate DESC")
    List<User> findRecentUsers(Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM users WHERE joined_date >= NOW() - INTERVAL '1 day'", nativeQuery = true)
    long countUsersJoinedLast24Hours();

    @Query(value = "SELECT COUNT(*) FROM users WHERE joined_date >= NOW() - INTERVAL '7 days'", nativeQuery = true)
    long countUsersJoinedLast7Days();

    @Query(value = "SELECT pg_size_pretty(pg_total_relation_size('users'))", nativeQuery = true)
    String getTableSize();

    @Query(value = "SELECT pg_size_pretty(pg_total_relation_size('user_grid_subscriptions'))", nativeQuery = true)
    String getSubscriptionTableSize();

    long count();
}
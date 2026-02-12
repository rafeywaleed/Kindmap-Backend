package com.exotech.kindmap.repository;

import com.exotech.kindmap.model.Grid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GridRepo extends JpaRepository<Grid, String> {

    @Query("SELECT DISTINCT g FROM Grid g " +
            "LEFT JOIN FETCH g.pins " +
            "LEFT JOIN FETCH g.users")
    List<Grid> findAllWithPinsAndUsers();

    @Query("SELECT DISTINCT g FROM Grid g " +
            "LEFT JOIN FETCH g.pins " +
            "LEFT JOIN FETCH g.users")
    Page<Grid> findAllWithPinsAndUsers(Pageable pageable);

    @Query("SELECT DISTINCT g FROM Grid g " +
            "LEFT JOIN FETCH g.pins " +
            "LEFT JOIN FETCH g.users " +
            "WHERE g.gridId = :gridId")
    Optional<Grid> findByIdWithPinsAndUsers(@Param("gridId") String gridId);

    @Query("SELECT DISTINCT g FROM Grid g " +
            "LEFT JOIN FETCH g.pins " +
            "WHERE g.gridId = :gridId")
    Optional<Grid> findByIdWithPins(@Param("gridId") String gridId);

    @Query("SELECT DISTINCT g FROM Grid g " +
            "LEFT JOIN FETCH g.users " +
            "WHERE g.gridId = :gridId")
    Optional<Grid> findByIdWithUsers(@Param("gridId") String gridId);


    @Query("SELECT DISTINCT g FROM Grid g " +
            "WHERE LOWER(g.gridId) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Grid> searchGrids(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT g) FROM Grid g JOIN g.pins p")
    long countGridsWithPins();

    @Query("SELECT COUNT(DISTINCT g) FROM Grid g JOIN g.users u")
    long countGridsWithUsers();

    @Query("SELECT COUNT(g) FROM Grid g WHERE g.pins IS EMPTY")
    long countGridsWithoutPins();

    @Query("SELECT COUNT(g) FROM Grid g WHERE g.users IS EMPTY")
    long countGridsWithoutUsers();

    @Query(value = "SELECT pg_size_pretty(pg_total_relation_size('grids'))", nativeQuery = true)
    String getTableSize();

    long count();
}
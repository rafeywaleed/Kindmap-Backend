package com.exotech.kindmap.repository;

import com.exotech.kindmap.model.Grid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GridRepo extends JpaRepository<Grid,String> {

    @Query("SELECT DISTINCT g FROM Grid g " +
            "LEFT JOIN FETCH g.pins " +
            "LEFT JOIN FETCH g.users")
    List<Grid> findAllWithPinsAndUsers();

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

    long count();

}

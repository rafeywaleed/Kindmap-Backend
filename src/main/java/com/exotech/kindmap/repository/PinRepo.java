package com.exotech.kindmap.repository;


import com.exotech.kindmap.model.Pin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PinRepo extends JpaRepository<Pin, String> {

    @Query("SELECT DISTINCT p FROM Pin p " +
            "LEFT JOIN FETCH p.grid")
    List<Pin> findAllWithGrid();

    @Query("SELECT DISTINCT p FROM Pin p " +
            "LEFT JOIN FETCH p.grid " +
            "WHERE p.pinId = :pinId")
    Optional<Pin> findByIdWithGrid(@Param("pinId") String pinId);

    @Query("SELECT DISTINCT p FROM Pin p " +
            "LEFT JOIN FETCH p.grid " +
            "WHERE p.grid.gridId = :gridId")
    List<Pin> findByGridIdWithGrid(@Param("gridId") String gridId);

    long count();
}

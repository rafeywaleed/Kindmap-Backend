package com.exotech.kindmap.repository;

import com.exotech.kindmap.model.Pin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            "LEFT JOIN FETCH p.grid")
    Page<Pin> findAllWithGrid(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Pin p " +
            "LEFT JOIN FETCH p.grid " +
            "WHERE p.pinId = :pinId")
    Optional<Pin> findByIdWithGrid(@Param("pinId") String pinId);

    @Query("SELECT DISTINCT p FROM Pin p " +
            "LEFT JOIN FETCH p.grid " +
            "WHERE p.grid.gridId = :gridId")
    List<Pin> findByGridIdWithGrid(@Param("gridId") String gridId);

    @Query("SELECT p FROM Pin p WHERE p.createdBy = :userId ORDER BY p.createdAt DESC")
    List<Pin> findByCreatedBy(@Param("userId") String userId);

    @Query("SELECT DISTINCT p FROM Pin p " +
            "WHERE LOWER(p.pinId) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.details) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.note) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.createdBy) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Pin> searchPins(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Pin p WHERE p.imageBase64 IS NULL OR p.imageBase64 = ''")
    long countPinsWithoutImage();

    @Query("SELECT COUNT(p) FROM Pin p WHERE p.details = '(none)' OR p.details IS NULL")
    long countPinsWithoutDetails();

    @Query(value = "SELECT COUNT(*) FROM pins WHERE created_at >= NOW() - INTERVAL '1 day'", nativeQuery = true)
    long countPinsLast24Hours();

    @Query(value = "SELECT COUNT(*) FROM pins WHERE created_at >= NOW() - INTERVAL '7 days'", nativeQuery = true)
    long countPinsLast7Days();

    @Query(value = "SELECT COUNT(*) FROM pins WHERE created_at >= NOW() - INTERVAL '30 days'", nativeQuery = true)
    long countPinsLast30Days();

    @Query("SELECT p FROM Pin p ORDER BY p.createdAt DESC")
    List<Pin> findRecentPins(Pageable pageable);

    @Query(value = "SELECT pg_size_pretty(pg_total_relation_size('pins'))", nativeQuery = true)
    String getTableSize();

    long count();
}
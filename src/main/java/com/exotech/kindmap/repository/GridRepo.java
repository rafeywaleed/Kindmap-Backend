package com.exotech.kindmap.repository;

import com.exotech.kindmap.model.Grid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GridRepo extends JpaRepository<Grid,String> {
}

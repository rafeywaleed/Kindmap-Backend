package com.exotech.kindmap.repository;


import com.exotech.kindmap.model.Pin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PinRepo extends JpaRepository<Pin, String> {
}

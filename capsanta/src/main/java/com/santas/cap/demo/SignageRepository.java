package com.santas.cap.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface SignageRepository extends JpaRepository<Signage, Long>{
    Collection<Signage> getByDocId(String docId);
}

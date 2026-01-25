package com.motioncad.server.repository;

import com.motioncad.server.domain.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {
    List<Part> findAllByCategory(String category);

    List<Part> findAllByPromptIsNotNull();
}

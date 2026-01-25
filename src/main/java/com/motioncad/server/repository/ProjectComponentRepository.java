package com.motioncad.server.repository;

import com.motioncad.server.domain.ProjectComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectComponentRepository extends JpaRepository<ProjectComponent, Long> {
    List<ProjectComponent> findAllByProjectId(Long projectId);

    void deleteAllByProjectId(Long projectId);
}

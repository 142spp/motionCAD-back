package com.motioncad.server.repository;

import com.motioncad.server.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByOrderByCreatedAtDesc();

    List<Project> findAllByUserId(Long userId);

    List<Project> findAllByIsPublicTrueOrderByUpdatedAtDesc();

    List<Project> findAllByIsPublicTrueOrderByLikesCountDesc();
}

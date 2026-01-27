package com.motioncad.server.repository;

import com.motioncad.server.domain.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartRepository extends JpaRepository<Part, Long>, JpaSpecificationExecutor<Part> {
    List<Part> findAllByCategory(String category);

    List<Part> findAllByTypeAndIsPublicTrueOrderByUpdatedAtDesc(com.motioncad.server.domain.PartType type);

    List<Part> findAllByTypeAndIsPublicTrueOrderByLikesCountDesc(com.motioncad.server.domain.PartType type);

    java.util.Optional<Part> findByFileHash(String fileHash);

    boolean existsByFileHash(String fileHash);
}

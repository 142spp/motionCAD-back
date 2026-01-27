package com.motioncad.server.repository;

import com.motioncad.server.domain.TransferFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferFileRepository extends JpaRepository<TransferFile, Long> {

    Optional<TransferFile> findTopByOrderByCreatedAtDesc();

    Optional<TransferFile> findByFileId(String fileId);
}

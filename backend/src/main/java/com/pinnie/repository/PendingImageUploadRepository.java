package com.pinnie.repository;

import com.pinnie.model.PendingImageUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PendingImageUploadRepository extends JpaRepository<PendingImageUpload, UUID> {
}

package com.reviewtopper.repository;

import com.reviewtopper.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    List<Workspace> findAllByActiveTrueOrderByCreatedAtDesc();

    Optional<Workspace> findByIdAndActiveTrue(Long id);

    Optional<Workspace> findBySlugIgnoreCaseAndActiveTrue(String slug);

    boolean existsBySlugIgnoreCaseAndActiveTrue(String slug);

    boolean existsBySlugIgnoreCaseAndIdNotAndActiveTrue(String slug, Long id);

    List<Workspace> findByOwnerIdAndActiveTrueOrderByCreatedAtDesc(Long ownerId);

    long countByOwnerIdAndActiveTrue(Long ownerId);

    long countByActiveTrue();
}

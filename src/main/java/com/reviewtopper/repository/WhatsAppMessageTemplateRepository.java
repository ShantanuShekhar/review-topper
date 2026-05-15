package com.reviewtopper.repository;

import com.reviewtopper.entity.WhatsAppMessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WhatsAppMessageTemplateRepository extends JpaRepository<WhatsAppMessageTemplate, Long> {

    List<WhatsAppMessageTemplate> findByTemplateKeyAndActiveTrueOrderByWorkspace_IdAsc(String templateKey);

    @Query("""
            SELECT t FROM WhatsAppMessageTemplate t
            WHERE t.templateKey = :key AND t.active = true
              AND (t.workspace IS NULL OR t.workspace.id = :workspaceId)
            ORDER BY CASE WHEN (t.workspace IS NOT NULL) THEN 0 ELSE 1 END
            """)
    List<WhatsAppMessageTemplate> findApplicable(@Param("key") String key, @Param("workspaceId") Long workspaceId);

    default Optional<WhatsAppMessageTemplate> resolveBest(String key, Long workspaceId) {
        List<WhatsAppMessageTemplate> list = findApplicable(key, workspaceId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}

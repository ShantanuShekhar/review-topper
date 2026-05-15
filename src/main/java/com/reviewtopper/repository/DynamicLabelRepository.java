package com.reviewtopper.repository;

import com.reviewtopper.entity.DynamicLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DynamicLabelRepository extends JpaRepository<DynamicLabel, Long> {

    List<DynamicLabel> findByWorkspaceIsNullAndLocaleOrderByCategoryAscLabelKeyAsc(String locale);

    List<DynamicLabel> findByWorkspaceIdAndLocaleOrderByCategoryAscLabelKeyAsc(Long workspaceId, String locale);
}

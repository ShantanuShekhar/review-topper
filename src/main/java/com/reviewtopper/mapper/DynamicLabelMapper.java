package com.reviewtopper.mapper;

import com.reviewtopper.dto.label.DynamicLabelResponse;
import com.reviewtopper.entity.DynamicLabel;
import org.springframework.stereotype.Component;

@Component
public class DynamicLabelMapper {

    public DynamicLabelResponse toResponse(DynamicLabel l) {
        return new DynamicLabelResponse(
                l.getId(),
                l.getWorkspace() != null ? l.getWorkspace().getId() : null,
                l.getLabelKey(),
                l.getLabelValue(),
                l.getCategory(),
                l.getLocale());
    }
}

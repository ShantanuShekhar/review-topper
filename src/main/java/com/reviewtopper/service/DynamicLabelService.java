package com.reviewtopper.service;

import com.reviewtopper.entity.DynamicLabel;
import com.reviewtopper.repository.DynamicLabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DynamicLabelService {

    private final DynamicLabelRepository dynamicLabelRepository;

    /**
     * Workspace-specific entries override globals with the same key + locale.
     */
    @Transactional(readOnly = true)
    public Map<String, String> mergedMap(Long workspaceId, String locale) {
        List<DynamicLabel> globals = dynamicLabelRepository.findByWorkspaceIsNullAndLocaleOrderByCategoryAscLabelKeyAsc(locale);
        Map<String, String> map = new LinkedHashMap<>();
        for (DynamicLabel g : globals) {
            map.put(g.getLabelKey(), g.getLabelValue());
        }
        if (workspaceId != null) {
            List<DynamicLabel> scoped =
                    dynamicLabelRepository.findByWorkspaceIdAndLocaleOrderByCategoryAscLabelKeyAsc(workspaceId, locale);
            for (DynamicLabel s : scoped) {
                map.put(s.getLabelKey(), s.getLabelValue());
            }
        }
        return map;
    }
}

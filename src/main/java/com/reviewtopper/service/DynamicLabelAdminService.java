package com.reviewtopper.service;

import com.reviewtopper.dto.label.DynamicLabelResponse;
import com.reviewtopper.dto.label.DynamicLabelUpsertRequest;
import com.reviewtopper.entity.DynamicLabel;
import com.reviewtopper.exception.BadRequestException;
import com.reviewtopper.exception.NotFoundException;
import com.reviewtopper.mapper.DynamicLabelMapper;
import com.reviewtopper.repository.DynamicLabelRepository;
import com.reviewtopper.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DynamicLabelAdminService {

    private final DynamicLabelRepository dynamicLabelRepository;
    private final WorkspaceRepository workspaceRepository;
    private final DynamicLabelMapper dynamicLabelMapper;

    @Transactional(readOnly = true)
    public List<DynamicLabelResponse> listAll() {
        return dynamicLabelRepository.findAll().stream().map(dynamicLabelMapper::toResponse).toList();
    }

    @Transactional
    public DynamicLabelResponse create(DynamicLabelUpsertRequest req) {
        DynamicLabel label = DynamicLabel.builder()
                .workspace(resolveWorkspace(req.workspaceId()))
                .labelKey(req.labelKey())
                .labelValue(req.labelValue())
                .category(req.category())
                .locale(req.locale())
                .build();
        dynamicLabelRepository.save(label);
        return dynamicLabelMapper.toResponse(label);
    }

    @Transactional
    public DynamicLabelResponse replace(Long id, DynamicLabelUpsertRequest req) {
        DynamicLabel label =
                dynamicLabelRepository.findById(id).orElseThrow(() -> new NotFoundException("Label not found."));
        label.setWorkspace(resolveWorkspace(req.workspaceId()));
        label.setLabelKey(req.labelKey());
        label.setLabelValue(req.labelValue());
        label.setCategory(req.category());
        label.setLocale(req.locale());
        dynamicLabelRepository.save(label);
        return dynamicLabelMapper.toResponse(label);
    }

    @Transactional
    public void delete(Long id) {
        if (!dynamicLabelRepository.existsById(id)) {
            throw new NotFoundException("Label not found.");
        }
        dynamicLabelRepository.deleteById(id);
    }

    private com.reviewtopper.entity.Workspace resolveWorkspace(Long workspaceId) {
        if (workspaceId == null) {
            return null;
        }
        return workspaceRepository.findByIdAndActiveTrue(workspaceId).orElseThrow(() -> new BadRequestException("Workspace not found."));
    }
}

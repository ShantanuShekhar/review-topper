package com.reviewtopper.controller.admin;

import com.reviewtopper.dto.label.DynamicLabelResponse;
import com.reviewtopper.dto.label.DynamicLabelUpsertRequest;
import com.reviewtopper.service.DynamicLabelAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dynamic-labels")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDynamicLabelController {

    private final DynamicLabelAdminService dynamicLabelAdminService;

    @GetMapping
    public List<DynamicLabelResponse> list() {
        return dynamicLabelAdminService.listAll();
    }

    @PostMapping
    public ResponseEntity<DynamicLabelResponse> create(@Valid @RequestBody DynamicLabelUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dynamicLabelAdminService.create(request));
    }

    @PutMapping("/{labelId}")
    public DynamicLabelResponse replace(@PathVariable Long labelId, @Valid @RequestBody DynamicLabelUpsertRequest request) {
        return dynamicLabelAdminService.replace(labelId, request);
    }

    @DeleteMapping("/{labelId}")
    public ResponseEntity<Void> delete(@PathVariable Long labelId) {
        dynamicLabelAdminService.delete(labelId);
        return ResponseEntity.noContent().build();
    }
}

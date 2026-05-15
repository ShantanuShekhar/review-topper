package com.reviewtopper.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LogoMetadataRequest(
        @NotBlank @Size(max = 2048) String logoUrl
) {}

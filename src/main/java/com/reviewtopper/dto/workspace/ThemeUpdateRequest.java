package com.reviewtopper.dto.workspace;

import com.reviewtopper.enums.ButtonStyle;
import com.reviewtopper.enums.LogoPosition;
import jakarta.validation.constraints.Size;

public record ThemeUpdateRequest(
        @Size(max = 32) String primaryColor,
        @Size(max = 32) String secondaryColor,
        Boolean darkModeEnabled,
        LogoPosition logoPosition,
        @Size(max = 32) String accentColor,
        ButtonStyle buttonStyle
) {}

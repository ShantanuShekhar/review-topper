package com.reviewtopper.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.reviewtopper.enums.LogoPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Persisted inside {@link Workspace#themeConfigJson} as JSON.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThemeConfiguration implements Serializable {

    private String primaryColor;
    private String secondaryColor;
    private Boolean darkModeEnabled;
    private LogoPosition logoPosition;
    /** Optional accent used by QR / branded surfaces */
    private String accentColor;
}

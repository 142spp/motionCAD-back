package com.motioncad.server.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings implements Serializable {
    private int handSensitivity;
    private boolean isLeftHanded;
    private String cameraResolution;
    private String uiTheme;
}

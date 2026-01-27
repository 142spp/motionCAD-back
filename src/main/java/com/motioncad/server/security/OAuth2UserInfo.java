package com.motioncad.server.security;

public interface OAuth2UserInfo {
    String getProviderId();

    String getEmail();

    String getName();
}

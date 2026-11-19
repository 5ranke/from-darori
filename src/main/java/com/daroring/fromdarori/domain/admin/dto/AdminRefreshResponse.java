package com.daroring.fromdarori.domain.admin.dto;

public record AdminRefreshResponse(String accessToken, String tokenType, long expiresIn) {
    @Override
    public String toString() { return "AdminRefreshResponse[REDACTED]"; }
}

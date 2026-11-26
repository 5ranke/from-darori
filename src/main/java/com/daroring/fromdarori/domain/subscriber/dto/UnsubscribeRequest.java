package com.daroring.fromdarori.domain.subscriber.dto;

import com.daroring.fromdarori.domain.subscriber.entity.UnsubscribeReason;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UnsubscribeRequest(
        @NotNull
        @Email
        String email,

        @NotNull
        UnsubscribeReason reasonCode,

        String detail
) {
}

package com.daroring.fromdarori.domain.subscriber.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminSubscriberCreateRequest(
        @NotNull
        @Size(max = 100)
        String name,

        @NotNull
        @Email
        @Size(max = 254)
        String email
) {
}

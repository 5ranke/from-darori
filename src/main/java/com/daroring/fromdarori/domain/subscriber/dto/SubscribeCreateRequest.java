package com.daroring.fromdarori.domain.subscriber.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubscribeCreateRequest(
        @NotNull
        @Email
        @Size(max = 254)
        String email,

        @NotNull
        @AssertTrue
        Boolean agreedToPrivacy

) {
}

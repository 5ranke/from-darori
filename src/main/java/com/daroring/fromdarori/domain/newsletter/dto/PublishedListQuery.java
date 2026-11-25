package com.daroring.fromdarori.domain.newsletter.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record PublishedListQuery(Integer year, int page, int size) {

    public static PublishedListQuery parse(String year, String page, String size) {
        if (year != null && !year.matches("[1-9][0-9]{3}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "year는 네 자리 양의 정수여야 합니다.");
        }
        return new PublishedListQuery(
                year == null ? null : Integer.valueOf(year),
                parseNumber(page, 0, 0, Integer.MAX_VALUE, "page는 0 이상의 정수여야 합니다."),
                parseNumber(size, 6, 1, 30, "size는 1~30 사이의 정수여야 합니다."));
    }

    private static int parseNumber(String value, int defaultValue, int min, int max, String errorMessage) {
        if (value == null) {
            return defaultValue;
        }
        try {
            int number = Integer.parseInt(value);
            if (number >= min && number <= max) {
                return number;
            }
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage, exception);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
    }
}

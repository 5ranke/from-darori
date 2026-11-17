package com.daroring.fromdarori.domain.admin.exception;

public class RefreshTokenException extends RuntimeException {
    public enum Reason {
        MISSING("AUTH4013", "Refresh Token이 누락되었습니다."),
        EXPIRED("AUTH4014", "Refresh Token이 만료되었습니다."),
        INVALID("AUTH4015", "유효하지 않은 Refresh Token입니다.");

        private final String code;
        private final String message;

        Reason(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    private final Reason reason;

    public RefreshTokenException(Reason reason) {
        super(reason.message);
        this.reason = reason;
    }

    public String getCode() { return reason.code; }
}

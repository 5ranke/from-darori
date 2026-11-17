package com.daroring.fromdarori.domain.admin.exception;

import org.springframework.http.HttpStatus;

public class AdminUserDeletionException extends RuntimeException {
    public enum Reason {
        UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "AUTH4012", "유효하지 않은 운영자 계정입니다."),
        SELF(HttpStatus.FORBIDDEN, "USER4031", "자기 자신은 삭제할 수 없습니다."),
        NOT_FOUND(HttpStatus.NOT_FOUND, "USER4041", "존재하지 않는 운영자입니다."),
        LAST(HttpStatus.CONFLICT, "USER4092", "마지막 운영자는 삭제할 수 없습니다."),
        RELATED_DATA(HttpStatus.CONFLICT, "USER4093", "연관 데이터 제약으로 삭제할 수 없습니다.");

        private final HttpStatus status;
        private final String code;
        private final String message;
        Reason(HttpStatus status, String code, String message) {
            this.status = status;
            this.code = code;
            this.message = message;
        }
    }

    private final Reason reason;
    public AdminUserDeletionException(Reason reason) {
        super(reason.message);
        this.reason = reason;
    }
    public HttpStatus getStatus() { return reason.status; }
    public String getCode() { return reason.code; }
}

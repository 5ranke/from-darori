package com.daroring.fromdarori.domain.admin.exception;

public class DeletedAdminException extends RuntimeException {
    public DeletedAdminException() {
        super("삭제된 운영자 계정입니다.");
    }
}

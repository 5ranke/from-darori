package com.daroring.fromdarori.domain.admin.exception;

public class DuplicateAdminEmailException extends RuntimeException {
    public DuplicateAdminEmailException() {
        super("이미 등록된 이메일입니다.");
    }
}

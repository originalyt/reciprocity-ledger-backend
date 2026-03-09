package com.reciprocityledger.backend.common.exception;

public final class ErrorCode {

    public static final int INVALID_ARGUMENT = 4000;
    public static final int UNAUTHORIZED = 4001;
    public static final int TOKEN_EXPIRED = 4002;
    public static final int FORBIDDEN = 4003;
    public static final int CONTACT_NOT_FOUND = 4101;
    public static final int RECORD_NOT_FOUND = 4201;
    public static final int EVENT_EXCHANGE_NOT_FOUND = 4202;
    public static final int DUPLICATE_RECORD = 4203;
    public static final int INVALID_AMOUNT = 4204;
    public static final int INVALID_DATE = 4205;
    public static final int INVALID_SMS_CODE = 4301;
    public static final int USER_DISABLED = 4302;
    public static final int SYSTEM_ERROR = 5000;

    private ErrorCode() {
    }
}

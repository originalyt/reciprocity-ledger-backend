package com.reciprocityledger.backend.common.exception;

public final class ErrorCode {

    public static final int INVALID_ARGUMENT = 4000;
    public static final int CONTACT_NOT_FOUND = 4101;
    public static final int EVENT_NOT_FOUND = 4102;
    public static final int EVENT_TYPE_NOT_FOUND = 4103;
    public static final int RECORD_NOT_FOUND = 4104;
    public static final int RECIPROCITY_MATCH_NOT_FOUND = 4105;
    public static final int INVALID_AMOUNT = 4201;
    public static final int INVALID_DIRECTION = 4202;
    public static final int INVALID_EVENT_OWNER = 4203;
    public static final int INVALID_RECIPROCITY_OPERATION = 4204;
    public static final int USER_NOT_FOUND = 4301;
    public static final int USER_EMAIL_EXISTS = 4302;
    public static final int USER_DISABLED = 4303;
    public static final int INVALID_PASSWORD = 4304;
    public static final int SYSTEM_ERROR = 5000;

    private ErrorCode() {
    }
}

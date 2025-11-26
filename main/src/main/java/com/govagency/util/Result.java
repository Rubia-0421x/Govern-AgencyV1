package com.govagency.util;

public class Result {
    private final boolean success;
    private final String message;

    private Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static Result ok(String msg) {
        return new Result(true, msg);
    }

    public static Result error(String msg) {
        return new Result(false, msg);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}

package com.jobScheduler.model;

public enum JobAction {
    PRINT_MESSAGE,
    COUNTDOWN,
    CALL_API,
    WRITE_FILE,
    INSERT_DB,
    SEND_EMAIL;

    public static JobAction fromString(String s) {
        return JobAction.valueOf(s.toUpperCase());
    }
}

package com.jobScheduler.utils;

import org.quartz.CronExpression;

import java.text.ParseException;

public class CronUtils {
    public static boolean isValidCron(String cron) {
        try {
            return CronExpression.isValidExpression(cron);
        } catch (Exception e) {
            return false;
        }
    }
}

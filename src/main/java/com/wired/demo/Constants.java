package com.wired.demo;

import java.util.Calendar;
import java.util.Date;

public class Constants {
    public static final Date FIRST_JANUARY_1970;
    public static final String FUNCTION_CODE = "FUNCTION_CODE";
    public static final String CAR_STATUS = "CAR_STATUS";
    public static final String CAR_IS_MOVING = "CAR_IS_MOVING";
    public static final String CAR_OWNER = "CAR_OWNER";
    public static final String CAR_NUMBER = "CAR_NUMBER";
    public static final String CAR_PLATE = "CAR_PLATE";
    public static final String MAP_NAME = "testMap";

    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1970);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        FIRST_JANUARY_1970 = calendar.getTime();
    }

}

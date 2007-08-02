package org.jpos.ee;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SystemDate {
    static long offset = 0L;
    public static Date getDate() {
        return new Date (System.currentTimeMillis() + offset);
    }
    public static void forceDate (Date d) {
        offset = d.getTime() - System.currentTimeMillis();
    }
    public static Calendar getCalendar() {
        Calendar cal = new GregorianCalendar();
        cal.setTime (getDate());
        return cal;
    }
    public static long getOffset() {
        return offset;
    }
    public static void resetOffset() {
        this.offset = 0L;
    }
    public static long currentTimeMillis() {
        return System.currentTimeMillis() + offset;
    }
}


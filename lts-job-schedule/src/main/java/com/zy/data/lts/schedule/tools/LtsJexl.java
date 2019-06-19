package com.zy.data.lts.schedule.tools;

import org.apache.commons.lang3.ArrayUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.stream.Stream;

public class LtsJexl {

    /**
     * #{lts:date('yyyy-MM-dd HH','-1d','-1h')}
     * @param format
     * @param pattern
     * @return
     */
    public String date(String format, String ... pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();

        if(ArrayUtils.isNotEmpty(pattern)) {
            Stream.of(pattern).forEach(p -> {

                // y 年　M 月　d 日 h 时　m 分钟　s 秒　w 星期 默认分钟
                if(p.endsWith("y")) {
                    int year = parsePattern(p.substring(0, p.length() -1));
                    calendar.add(Calendar.YEAR, year);
                } else if(p.endsWith("M")) {
                    int month = parsePattern(p.substring(0, p.length() -1));
                    calendar.add(Calendar.MONTH, month);
                } else if( p.endsWith("d")) {
                    int day = parsePattern(p.substring(0, p.length() -1));
                    calendar.add(Calendar.DATE, day);
                } else if(p.endsWith("h")) {
                    int hour = parsePattern(p.substring(0, p.length() -1));
                    calendar.add(Calendar.HOUR_OF_DAY, hour);
                } else if( p.endsWith("m")) {
                    int minute = parsePattern(p.substring(0, p.length() -1));
                    calendar.add(Calendar.MINUTE, minute);
                } else if( p.endsWith("s")) {
                    int second = parsePattern(p.substring(0, p.length() -1));
                    calendar.add(Calendar.SECOND, second);
                } else {
                    int minute = parsePattern(p);
                    calendar.add(Calendar.MINUTE, minute);
                }
            });
        }

        return sdf.format(calendar.getTime());

    }

    private int parsePattern(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }

    }
}

package com.zy.data.lts.schedule.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenqingsong
 * @date 2019/4/3 14:34
 */
public class IntegerTool {

    public static List<Integer> parseOneBit(int value) {

        int length = Integer.bitCount(value);
        List<Integer> ret = new ArrayList<>(length);

        for (int i = 0; i < 32; i ++) {
            if((value & (1 << i)) > 0) {
                ret.add(i);
            }
        }
        return ret;
    }

    /**
     *
     * @param from 包含 最小值0
     * @param to 不包含 最大值31
     * @return
     */
    public static int format(int from, int to) {

        if(from > to) {
            return 0;
        }

        int value = 0;

        for(int i= from; i < to; i++) {
            value |= 1 << i;
        }

        return value;
    }
}

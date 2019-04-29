package com.zy.data.lts.schedule.model;

/**
 * @author chenqingsong
 * @date 2019/3/29 14:03
 */
public class Tuple<F, S> {

    private F f;
    private S s;
    public Tuple(F f, S s) {
        this.f = f;
        this.s = s;
    }

    public F first() {
        return f;
    }

    public S second() {
        return s;
    }
}

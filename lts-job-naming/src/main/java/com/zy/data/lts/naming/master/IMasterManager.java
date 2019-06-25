package com.zy.data.lts.naming.master;

public interface IMasterManager {

    default void init(){}
    default int getMasterSize() {
        return 1;
    }
}

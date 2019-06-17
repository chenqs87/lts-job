package com.zy.data.lts.ha;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        ZkClient zkClient = new ZkClient();
        zkClient.setZookeeperServer("localhost:2181");
        zkClient.setSessionTimeoutMs(6000);
        zkClient.setConnectionTimeoutMs(6000);
        zkClient.setMaxRetries(3);
        zkClient.setBaseSleepTimeMs(6000);
        zkClient.init();

        zkClient.registerMaster("localhost:8080");
        Thread.sleep(1000);
        System.out.println(zkClient.getMasters());
        zkClient.registerMaster("localhost:8081");
        System.out.println(zkClient.getMasters());



        Thread.sleep(10000000);

    }
}

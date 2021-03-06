package com.zy.data.lts.naming;

import com.zy.data.lts.naming.zk.ZkClient;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws InterruptedException {
       /* ZkClient zkClient = new ZkClient();
        zkClient.setZookeeperServer("localhost:2181");
        zkClient.setSessionTimeoutMs(6000);
        zkClient.setConnectionTimeoutMs(6000);
        zkClient.setMaxRetries(3);
        zkClient.setBaseSleepTimeMs(6000);
        zkClient.init();

        zkClient.register("/lts_job/services/master/localhost:8080");
        Thread.sleep(1000);

        listen(zkClient);



        Thread.sleep(10000000);*/

        Map<String, String> map = new HashMap<>();

        map.putIfAbsent("test", "test");

        map.computeIfAbsent("test", f-> {
            System.out.println("hello");
            return "test";
        });

    }

    public static void listen(ZkClient zkClient) {
        System.out.println(zkClient.getChildren("/lts_job/services/master", new Watcher() {

            @Override
            public void process(WatchedEvent event) {
                System.out.println("+==========" + event);
                listen(zkClient);
            }
        }));
    }
}

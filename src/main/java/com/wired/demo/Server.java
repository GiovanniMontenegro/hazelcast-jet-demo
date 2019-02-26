package com.wired.demo;


import com.hazelcast.config.Config;
import com.hazelcast.config.EventJournalConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Server {


    private static final String DISTINCT_MAP = "distinct";

    private static final long KEY_COUNT = 10_000;

    private static final long SLEEPING_TIME_MS = 1_000;

    private static final String SINK_NAME = "sink";

    private static Config getConfig() {
        Config config = new Config();
        config.addEventJournalConfig(new EventJournalConfig().setEnabled(true).setMapName(Constants.MAP_NAME).setCapacity(10000).setTimeToLiveSeconds(100));
        config.addEventJournalConfig(new EventJournalConfig().setEnabled(true).setMapName(DISTINCT_MAP).setCapacity(10000).setTimeToLiveSeconds(100));
        return config;
    }

    private static LocalTime toLocalTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalTime();
    }

    private static String getRandomCarStatus() {
        String[] carStatus = {"0", "1", "2"};
        int rnd = new Random().nextInt(carStatus.length);
        return carStatus[rnd];
    }

    private static String getRandomCarIsMoving() {
        String[] carIsMoving = {"0", "1"};
        int rnd = new Random().nextInt(carIsMoving.length);
        return carIsMoving[rnd];
    }

    private static Map<String, Car> mockDataCar() {
        Map<String, Car> carMap = new HashMap<>();
        carMap.put("A0456ZT", new Car("A0456ZT", System.currentTimeMillis()).setValues("A0456ZT", "1", "0", "John", "123456", ""));
        carMap.put("A1234RS", new Car("A1234RS", System.currentTimeMillis()).setValues("A1234RS", "1", "0", "Mark", "132326", ""));
        carMap.put("A1A3245", new Car("A1A3245", System.currentTimeMillis()).setValues("A1A3245", "1", "0", "Petrek", "176676", ""));
        carMap.put("A1231ZT", new Car("A1231ZT", System.currentTimeMillis()).setValues("A1231ZT", "1", "0", "Lua", "145646", ""));
        carMap.put("AA234ZT", new Car("AA234ZT", System.currentTimeMillis()).setValues("AA234ZT", "1", "0", "Hon", "179566", ""));
        return carMap;
    }

    private static HazelcastInstance startRemoteHzCluster(Config config) {
        HazelcastInstance remoteHz = Hazelcast.newHazelcastInstance(config);
        return remoteHz;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("-------------------------------------------");
        System.out.println("------------Demo is starting---------------");
        System.out.println("-------------------------------------------");
        new Server().start();
    }

    public void start() throws Exception {

        Config hzConfig = getConfig();

        JetConfig jetConfig = new JetConfig();
        jetConfig.setHazelcastConfig(hzConfig);

        JetInstance jetInstance = Jet.newJetInstance(jetConfig);

        HazelcastInstance instance = jetInstance.getHazelcastInstance();

        IMap<String, Car> map = instance.getMap(Constants.MAP_NAME);
        map.putAll(mockDataCar());

        try {
            System.out.println("*************** Initial Map  address " + map.size());

            Set<String> keys = map.keySet();

            Client client = new Client();
            client.start();
            int i = 0;
            Random random = new Random();
            while (true) {
                int randomNum = random.nextInt((1000 - 500) + 1) + 500;
                String codice = keys.stream().skip((int) (keys.size() * Math.random())).findFirst().get();
                TimeUnit.MILLISECONDS.sleep(randomNum);
                Car carUpdate = map.get(codice);
                Long time = System.currentTimeMillis();
                carUpdate.setTime(time);
                System.out.println(toLocalTime(time) + " update : " + codice);
                map.put(codice, carUpdate);
                Thread.sleep(450);
                time = System.currentTimeMillis();
                carUpdate.setCarStatus(getRandomCarStatus());
                carUpdate.setCarIsMoving(getRandomCarIsMoving());
                map.put(codice, carUpdate);
            }

        } finally {
            Hazelcast.shutdownAll();
        }

    }
}

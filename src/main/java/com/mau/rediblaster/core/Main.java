package com.mau.rediblaster.core;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static long START_TIME;

    /**
     * Benchmarking speed of 10 million SETs, GETs, and SUBSCRIBEs in redis using the java client {@link Jedis}
     */
    public static void main(String[] args) {

        final Jedis jedis = new Jedis("localhost");
        fillRedisPipelined(jedis);
        readRedisPipelined(jedis);
        subscribeRedisKeys(jedis);
    }

    /**
     * Fill local redis instance in non-pipelined fashion
     */
    public static void fillRedis(final Jedis jedis) {

        startTimer();
        for (int i=0; i<10000000; i++) {
            if (i % 10000 == 0)
                System.out.println("Finished " + i);
            jedis.set("normalKey"+i, "normalValue"+i);
        }
        endTimer("Non pipelined PUTs");
    }

    /**
     * Fill local redis instance in pipelined fashion. This should achieve ~5x more throughput than
     * non-pipelined SETs
     */
    public static void fillRedisPipelined(final Jedis jedis) {

        startTimer();
        int commandBatchSize = 1000;
        for (int i=0; i<10000; i++) {

            Pipeline pipeline = jedis.pipelined();
            int index = i * commandBatchSize;
            for (int j=1; j<=commandBatchSize; j++) {
                pipeline.set("pipelinedKey" + (index+j), "pipelinedValue" + (index+j));
            }
            pipeline.sync();
            System.out.println("Completed " + i);
        }
        endTimer("Pipelined SETs");
    }

    /**
     * Get values from local redis instance in pipelined fashion. This should achieve ~5x more throughput
     * than non-pipelined GETs
     */
    public static void readRedisPipelined(final Jedis jedis) {

        startTimer();
        int commandBatchSize = 1000;
        for (int i=0; i<10000; i++) {

            List<Response<String>> responseList = new ArrayList<>(commandBatchSize);
            Pipeline pipeline = jedis.pipelined();
            int index = i * commandBatchSize;
            for (int j=1; j<=commandBatchSize; j++) {
                responseList.add(pipeline.get("pipelinedKey" + (index+j)));
            }
            pipeline.sync();
            System.out.println(responseList.get(0).get());
            System.out.println("Completed " + i);
        }
        endTimer("Pipelined GETs");
    }

    /**
     * Subscribe to values from local redis instance. {@link JedisPubSub} does not support pipelining,
     * and consumes an entire thread as it polls redis for updates.
     */
    public static void subscribeRedisKeys(final Jedis jedis) {

        startTimer();
        JedisPubSub subscriber = new RedisSubscriber();
        Thread subscriberThread = new Thread(() -> {
            jedis.subscribe(subscriber, "pipelinedKey1");
        }, "subscriptionThread");
        subscriberThread.start();

        try {
            System.out.println("Allowing jedisPubSub to establish");
            Thread.sleep(2000);
        } catch(Exception e) {
            System.out.println("Interruption");
        }
        for (int i=0; i<10000000; i++) {
            subscriber.subscribe("pipelinedKey"+i);
            if (i % 10000 == 0) {
                System.out.println("Finished subscription " + i);
            }
        }
        endTimer("SUBSCRIBEs");
        System.out.println("Finished subscribing to 10000000 keys");
        subscriberThread.stop();
    }

    private static void startTimer() {
        START_TIME = System.currentTimeMillis();
    }

    private static void endTimer(final String action) {
        System.out.println(action + " took " + (System.currentTimeMillis() - START_TIME));
    }
}

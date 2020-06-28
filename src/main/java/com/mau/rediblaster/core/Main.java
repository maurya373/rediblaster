package com.mau.rediblaster.core;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static long START_TIME;
    private static final long NUM_KEYS_BENCHMARK = 10_000_000L;
    private static final int PIPELINE_BATCH_SIZE = 1000;
    private static final int LOG_THRESHOLD = 100_000;
    private static final int PIPELINE_LOG_THRESHOLD = LOG_THRESHOLD / PIPELINE_BATCH_SIZE;

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
        for (int i=0; i<NUM_KEYS_BENCHMARK; i++) {
            if (i > 0 && i % LOG_THRESHOLD == 0)
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
        long roundTrips = NUM_KEYS_BENCHMARK / PIPELINE_BATCH_SIZE;
        for (int i=0; i<roundTrips; i++) {

            Pipeline pipeline = jedis.pipelined();
            int index = i * PIPELINE_BATCH_SIZE;
            for (int j=1; j<=PIPELINE_BATCH_SIZE; j++) {
                pipeline.set("pipelinedKey" + (index+j), "pipelinedValue" + (index+j));
            }
            pipeline.sync();
            if (i > 0 && i % PIPELINE_LOG_THRESHOLD == 0) {
                System.out.println("Completed batch " + i);
            }
        }
        endTimer("Pipelined SETs");
    }

    /**
     * Get values from local redis instance in pipelined fashion. This should achieve ~5x more throughput
     * than non-pipelined GETs
     */
    public static void readRedisPipelined(final Jedis jedis) {

        startTimer();
        long roundTrips = NUM_KEYS_BENCHMARK / PIPELINE_BATCH_SIZE;
        for (int i=0; i<roundTrips; i++) {

            List<Response<String>> responseList = new ArrayList<>(PIPELINE_BATCH_SIZE);
            Pipeline pipeline = jedis.pipelined();
            int index = i * PIPELINE_BATCH_SIZE;
            for (int j=1; j<=PIPELINE_BATCH_SIZE; j++) {
                responseList.add(pipeline.get("pipelinedKey" + (index+j)));
            }
            pipeline.sync();
            if (i > 0 && i % PIPELINE_LOG_THRESHOLD == 0) {
                // We print to verify that GETs are working AND accurate
                // We print index+1 as the response list starts with j=1, so index+j
                System.out.println("Latest key pipelinedKey" + (index+1) + " has value " + responseList.get(0).get());
                System.out.println("Completed batch " + i);
            }
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
            Thread.sleep(1000);
        } catch(Exception e) {
            System.out.println("Interruption");
        }

        for (int i=0; i<NUM_KEYS_BENCHMARK; i++) {
            subscriber.subscribe("pipelinedKey"+i);
            if (i > 0 && i % LOG_THRESHOLD == 0) {
                System.out.println("Finished subscription " + i);
            }
        }
        endTimer("SUBSCRIBEs");

        // stop() is deprecated and unsafe, but fine for this use-case of ending the subscription thread so the
        // benchmark process can fully terminate. Otherwise the process will stay alive.
        subscriberThread.stop();
    }

    private static void startTimer() {
        START_TIME = System.currentTimeMillis();
    }

    private static void endTimer(final String action) {
        final StringBuilder builder = new StringBuilder(500);
        builder.append(action);
        builder.append(" took ");
        builder.append((System.currentTimeMillis() - START_TIME));
        builder.append(" for ");
        builder.append(NUM_KEYS_BENCHMARK);
        builder.append(" keys -----------------------------------------------------------------------------");
        System.out.println(builder.toString());
    }
}

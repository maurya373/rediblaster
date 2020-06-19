package com.mau.rediblaster.core;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        final Jedis jedis = new Jedis("localhost");
        long startTime = System.currentTimeMillis();

        //fillRedisPipelined(jedis);
        readRedisPipelined(jedis);

        System.out.println("Operations took " + (System.currentTimeMillis() - startTime));
    }

    public static void fillRedis(final Jedis jedis) {

        for (int i=0; i<500000; i++) {
            if (i % 1000 == 0)
                System.out.println("Finished " + i);
            jedis.set("normalKey"+i, "normalValue"+i);
        }
    }

    public static void fillRedisPipelined(final Jedis jedis) {

        int commandBatchSize = 1000;
        for (int i=1; i<=5000; i++) {

            Pipeline pipeline = jedis.pipelined();
            for (int j=1; j<commandBatchSize; j++) {
                int index = i * commandBatchSize + j;
                pipeline.set("pipelinedKey" + index, "pipelinedValue" + index);
            }
            pipeline.sync();
            System.out.println("Completed " + i);
        }
    }

    public static void readRedisPipelined(final Jedis jedis) {

        int commandBatchSize = 1000;
        for (int i=1; i<=5000; i++) {

            List<Response<String>> responseList = new ArrayList<>(commandBatchSize);
            Pipeline pipeline = jedis.pipelined();
            for (int j=1; j<=commandBatchSize; j++) {
                responseList.add(pipeline.get("pipelinedKey" + (i * commandBatchSize + j)));
            }
            pipeline.sync();
            System.out.println(responseList.get(commandBatchSize-1).get());
            System.out.println("Completed " + i);
        }
    }
}

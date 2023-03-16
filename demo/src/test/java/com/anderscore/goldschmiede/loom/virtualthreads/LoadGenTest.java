package com.anderscore.goldschmiede.loom.virtualthreads;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadGenTest {

    @Test
    @Disabled
    void testThreads() throws InterruptedException {
        int count = 1_000_000;
        var countDownLatch = new CountDownLatch(count);
        long ts = System.nanoTime();
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < count; i++) {
                executorService.submit(this::work);
            }
            countDownLatch.await();
        }
    }

    private void work() {
        BigInteger sum = BigInteger.ZERO;
        for (long i = 1; i <= 10_000_000_000L; i++) {
            sum = sum.add(BigInteger.valueOf(i));
        }
    }

}

package com.anderscore.goldschmiede.loom.structuredconcurreny;

import jdk.incubator.concurrent.StructuredTaskScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StructuredConcurrencyTest {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void simpleSample() throws InterruptedException {
        // tag::simple[]
        var sum = new AtomicInteger();

        // Create 3 concurrent tasks that compute the total sum in parallel
        for (int i = 1; i <= 3; i++) {
            CompletableFuture.runAsync(() -> {
                var data = fetchData();
                sum.addAndGet(data);
            });
        }

        // Do something useful in the meantime while the sum is being computed
        TimeUnit.SECONDS.sleep(2);

        // And then use the sum
        System.out.printf("The final sum is %d%n", sum.get());
        // end::simple[]
    }

    private int fetchData() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ex) {
            log.info("interrupted during fetchData...");
            throw new IllegalStateException("interrupted", ex);
        }
        log.debug("fetch data done", new Exception("show stacktrace"));

        return 1;
    }

    @Test
    public void improvedSample() throws InterruptedException, ExecutionException {
        // tag::improved[]
        var sum = new AtomicInteger();

        // A list to keep track of the created futures
        var futures = new ArrayList<Future<Void>>();

        // Create 3 concurrent tasks that compute the total sum in parallel
        for (int i = 1; i <= 3; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                var data = fetchData();
                sum.addAndGet(data);
            }));
        }

        // Do something useful in the meantime while the sum is being computed
        TimeUnit.SECONDS.sleep(2);

        // Wait for all the futures and propagate exceptions
        for (Future future : futures) {
            future.get();
        }

        // And then use the sum
        System.out.printf("The final sum is %d%n", sum.get());
        // end::improved[]
    }

    @Test
    public void structuredSample() throws InterruptedException, ExecutionException {
        // tag::structured[]
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var sum = new AtomicInteger();

            // Create 3 concurrent tasks that compute the total sum in parallel
            for (int i = 1; i <= 3; i++) {
                scope.fork(Executors.callable(() -> {
                    var data = fetchData();
                    sum.addAndGet(data);
                }));
            }

            // Do something useful in the meantime while the sum is being computed
            TimeUnit.SECONDS.sleep(2);

            // Wait for all the futures and propagate exceptions
            scope.join();
            scope.throwIfFailed();

            // And then use the sum
            System.out.printf("The final sum is %d%n", sum.get());
        }
        // end::structured[]
    }
}

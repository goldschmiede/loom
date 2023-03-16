package com.anderscore.goldschmiede.loom.limits;

import jdk.incubator.concurrent.StructuredTaskScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FileWalkTest {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private final AtomicLong totalSize = new AtomicLong(0);
    private final AtomicInteger threadCount = new AtomicInteger(1);

    @Test
    void testWalk() {
        walk(Path.of("/home/hjhessmann"));
        System.out.println("totalSize: " + totalSize.get());
        log.info("thread-count: {}", threadCount.get());
    }

    void walk(Path file) {
        threadCount.incrementAndGet();
        if (Files.isRegularFile(file)) {
            try {
                totalSize.addAndGet(Files.size(file));
            } catch (IOException ex) {
                throw new FileWalkException(file, ex);
            }
        } else if (Files.isDirectory(file)) {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                Files.list(file).forEach((f) -> scope.fork(Executors.callable(() -> walk(f))));
                // Wait for all the futures and propagate exceptions
                scope.join();
                scope.throwIfFailed();
                log.info("thread-count: {}", threadCount.get());
            } catch (IOException|ExecutionException|InterruptedException ex) {
                throw new FileWalkException(file, ex);
            }
        }
        threadCount.decrementAndGet();
    }
}

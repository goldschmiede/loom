package om.anderscore.goldschmiede.loom.virtualthreads;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;

public class VirtualThreadsTest {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private CountDownLatch countDownLatch;

    private AtomicInteger counter = new AtomicInteger(0);

    private Lock[] locks;

    @BeforeEach
    void init() {
        locks = new Lock[100];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    @Test
    void testThreads() throws InterruptedException {
        int count = 10_000;
        countDownLatch = new CountDownLatch(count);
        long ts = System.nanoTime();
        try (ExecutorService executorService = Executors.newFixedThreadPool(100)) {
            for (int i = 0; i < count; i++) {
                executorService.submit(this::delay);
                // executorService.submit(i % 3 == 0 ? this::request : this::delay);
            }
            countDownLatch.await();
            log.info("done after {} s", (System.nanoTime() - ts) / 1e9);
        }
    }

    private synchronized void delay() {
        delayCount(counter.incrementAndGet());
    }

    private void delayCount(int count) {
        log.debug("delay start: started {}, remaining {}", count, countDownLatch.getCount());
        try {
            TimeUnit.SECONDS.sleep(1);
            countDownLatch.countDown();
            log.info("delay done: started {}, remaining {}", count, countDownLatch.getCount());
        } catch (InterruptedException ex) {
            throw new IllegalStateException("interrupted", ex);
        }
    }

    private void lockedDelay() {
        int count = counter.incrementAndGet();
        Lock lock = locks[count % locks.length];
        lock.lock();
        try {
            delayCount(count);
        } finally {
            lock.unlock();
        }
    }

    private void request() {
        int count = counter.incrementAndGet();
        log.debug("request start: started {}, remaining {}", count, countDownLatch.getCount());
        try {
            URL url = new URL("http://localhost:8080/hello?name=Goldschmiede");
            try (InputStream in = url.openStream()) {
                String response = IOUtils.toString(in, StandardCharsets.UTF_8);
                assertThat(response).isEqualTo("Hello Goldschmiede!");
            }
            countDownLatch.countDown();
            log.info("request done: started {}, remaining {}", count, countDownLatch.getCount());
        } catch (IOException ex) {
            throw new IllegalStateException("IO-error", ex);
        }
    }

    private void createVirtualThreads() {
        Runnable runnable = () -> {};
        Thread.ofPlatform().name("my-thread").start(runnable);
        Thread.ofVirtual().name("my-thread").unstarted(runnable);
    }
}

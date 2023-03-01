package om.anderscore.goldschmiede.loom.virtualthreads;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLConnection;
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

    private Lock lock = new ReentrantLock();

    @Test
    void testThreads() throws InterruptedException {
        int count = 10_000;
        countDownLatch = new CountDownLatch(count);
        long ts = System.nanoTime();
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < count; i++) {
                executorService.submit(i % 3 == 0 ? this::request : this::delay);
            }
            countDownLatch.await();
            log.info("done after {} s", (System.nanoTime() - ts) / 1e9);
        }
    }

    void delay() {
        int count = counter.incrementAndGet();
        log.debug("delay start: started {}, remaining {}", count, countDownLatch.getCount());
        try {
            TimeUnit.SECONDS.sleep(1);
            countDownLatch.countDown();
            log.info("delay done: started {}, remaining {}", count, countDownLatch.getCount());
        } catch (InterruptedException ex) {
            throw new IllegalStateException("interrupted", ex);
        }
    }

    void lockedDelay() {
        lock.lock();
        try {
            delay();
        } finally {
            lock.unlock();
        }
    }

    void request() {
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
}

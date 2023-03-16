package com.anderscore.goldschmiede.loom.scopedvalules;

import jdk.incubator.concurrent.StructuredTaskScope;
import jdk.incubator.concurrent.ScopedValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ScopedValuesTest {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    // tag::scoped[]
    private static final ScopedValue<String> CONTEXT = ScopedValue.newInstance();
    private static final ScopedValue<String> USERNAME = ScopedValue.newInstance();
    // end::scoped[]

    @Test
    void testScopedValue() {
        // tag::where[]
        ScopedValue.where(USERNAME, "duke")
                .where(CONTEXT, "test")
                .run(this::doNested);
        // end::where[]
    }

    private void doNested() {
        ScopedValue.where(USERNAME, "hugo", this::doParallel);
        doParallel();
    }

    private void doParallel() {
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            for (int i = 1; i <= 3; i++) {
                scope.fork(this::produceResult);
            }
            scope.join();
            System.out.printf("result: %s%n", scope.result());
        } catch (InterruptedException ex) {
            log.info("was interrupted", ex);
        } catch (ExecutionException ex) {
            log.error("error occurred", ex);
        }
    }

    private String produceResult() {
        sleep(2);
        return String.format("context: %s, user: %s, thread: %s",
                // tag::read[]
                CONTEXT.orElse("undefined"),
                USERNAME.orElse("undefined"),
                // end::read[]
                Thread.currentThread().getName());
    }

    private void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException ex) {
            log.info("was interrupted", ex);
            Thread.currentThread().interrupt();
        }
    }
}

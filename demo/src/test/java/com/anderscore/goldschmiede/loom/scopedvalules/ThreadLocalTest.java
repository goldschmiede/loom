package com.anderscore.goldschmiede.loom.scopedvalules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ThreadLocalTest {

    // tag::threadLocal[]
    private ThreadLocal<Integer> threadLocalValue =
            new InheritableThreadLocal<>();

    @Test
    public void testThreadLocal() {
        assertThat(threadLocalValue.get()).isNull();
        threadLocalValue.set(0);

        for (int i = 1; i <= 5; i++) {
            Thread.ofVirtual()
                    .name("my-thread-", i)
                    .allowSetThreadLocals(true) // default
                    .inheritInheritableThreadLocals(true) // default
                    .start(this::dumpThreadLocal);
        }
        dumpThreadLocal();
        dumpThreadLocal();
        threadLocalValue.remove();
        dumpThreadLocal();
    }

    private void dumpThreadLocal() {
        System.out.printf("[%s] threadLocalValue = %d%n",
                Thread.currentThread().getName(),
                threadLocalValue.get());
        threadLocalValue.set(99);
    }
    // end::threadLocal[]
}

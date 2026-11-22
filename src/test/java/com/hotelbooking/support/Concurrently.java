package com.hotelbooking.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

/** Starts N tasks at the same instant and collects each result or exception. */
public final class Concurrently {

    private Concurrently() {
    }

    public static <T> List<Outcome<T>> run(int threads, IntFunction<T> task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        try {
            List<Future<Outcome<T>>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                int index = i;
                futures.add(pool.submit(() -> {
                    startGate.await();
                    try {
                        return Outcome.success(task.apply(index));
                    } catch (RuntimeException ex) {
                        return Outcome.<T>failure(ex);
                    }
                }));
            }
            startGate.countDown();
            List<Outcome<T>> outcomes = new ArrayList<>();
            for (Future<Outcome<T>> future : futures) {
                outcomes.add(future.get(60, TimeUnit.SECONDS));
            }
            return outcomes;
        } finally {
            pool.shutdownNow();
        }
    }

    public record Outcome<T>(T value, RuntimeException error) {

        static <T> Outcome<T> success(T value) {
            return new Outcome<>(value, null);
        }

        static <T> Outcome<T> failure(RuntimeException error) {
            return new Outcome<>(null, error);
        }

        public boolean succeeded() {
            return error == null;
        }
    }
}

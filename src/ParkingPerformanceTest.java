package ParkingSystem;

import Floor.FloorFactory;
import PricingStrategies.MinutesPricingStrategy;
import PricingStrategies.PricingStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ParkingPerformanceTest {

    // ============================================================
    // CONFIGURATION
    // ============================================================

    private static final int WARMUP_ROUNDS = 5;

    private static final int MEASURED_ROUNDS = 10;

    private static final int OPERATIONS_PER_ROUND = 100_000;

    private static final int[] THREAD_COUNTS = {
            1, 2, 4, 8, 16, 32, 64
    };


    // ============================================================
    // MAIN
    // ============================================================

    public static void main(String[] args) throws Exception {

        System.out.println();
        System.out.println("==========================================================");
        System.out.println("        PARKING LOT CONCURRENCY BENCHMARK");
        System.out.println("==========================================================");

        System.out.println(
                "Operations / round : " + OPERATIONS_PER_ROUND
        );

        System.out.println(
                "Warmup rounds      : " + WARMUP_ROUNDS
        );

        System.out.println(
                "Measured rounds    : " + MEASURED_ROUNDS
        );

        System.out.println();

        runSustainedEntryExitBenchmark();

        runHighContentionBenchmark();

        runMixedVehicleTypeBenchmark();

        System.out.println();
        System.out.println("==========================================================");
        System.out.println("              BENCHMARK FINISHED");
        System.out.println("==========================================================");
    }


    // ============================================================
    // TEST 1
    //
    // SUSTAINED ENTRY + EXIT
    //
    // Every successful operation:
    //
    // ENTRY
    //   ↓
    // SLOT
    //   ↓
    // TOKEN
    //   ↓
    // EXIT
    //   ↓
    // SLOT RELEASED
    //
    // This prevents the parking lot from becoming permanently full.
    // ============================================================

    private static void runSustainedEntryExitBenchmark()
            throws Exception {

        System.out.println();
        System.out.println(
                "========== SUSTAINED ENTRY / EXIT TEST =========="
        );

        printHeader();

        for (int threadCount : THREAD_COUNTS) {

            // -------------------------
            // WARMUP
            // -------------------------

            for (int i = 0; i < WARMUP_ROUNDS; i++) {

                runSustainedRound(
                        threadCount,
                        false
                );
            }


            // -------------------------
            // MEASUREMENT
            // -------------------------

            List<Long> allLatencies =
                    new ArrayList<>();

            long totalStart =
                    System.nanoTime();

            long totalSuccessful = 0;

            long totalFailed = 0;

            long totalRaceErrors = 0;


            for (int i = 0;
                 i < MEASURED_ROUNDS;
                 i++) {

                RoundResult result =
                        runSustainedRound(
                                threadCount,
                                true
                        );

                totalSuccessful +=
                        result.successful;

                totalFailed +=
                        result.failed;

                totalRaceErrors +=
                        result.raceErrors;

                allLatencies.addAll(
                        result.latencies
                );
            }


            long totalEnd =
                    System.nanoTime();


            double seconds =
                    (totalEnd - totalStart)
                            / 1_000_000_000.0;


            double throughput =
                    totalSuccessful / seconds;


            LatencyResult latency =
                    calculateLatency(
                            allLatencies
                    );


            System.out.printf(
                    "%-10d %-15.2f %-15.2f %-15.2f %-15.2f%n",
                    threadCount,
                    throughput,
                    latency.averageMicros,
                    latency.p95Micros,
                    latency.p99Micros
            );


            if (totalFailed > 0) {

                System.out.println(
                        "WARNING: "
                                + totalFailed
                                + " operations could not acquire a slot."
                );
            }


            if (totalRaceErrors > 0) {

                throw new AssertionError(
                        "RACE CONDITION DETECTED: "
                                + totalRaceErrors
                                + " violations."
                );
            }
        }
    }


    // ============================================================
    // SUSTAINED ROUND
    // ============================================================

    private static RoundResult runSustainedRound(
            int threadCount,
            boolean collectLatency)
            throws Exception {


        ParkingSystem parkingSystem =
                createParkingSystem();


        ExecutorService executor =
                Executors.newFixedThreadPool(
                        threadCount
                );


        CountDownLatch ready =
                new CountDownLatch(
                        threadCount
                );


        CountDownLatch startSignal =
                new CountDownLatch(1);


        CountDownLatch finished =
                new CountDownLatch(
                        threadCount
                );


        AtomicInteger successful =
                new AtomicInteger();


        AtomicInteger failed =
                new AtomicInteger();


        AtomicInteger errors =
                new AtomicInteger();


        AtomicInteger raceErrors =
                new AtomicInteger();


        /*
         * Benchmark-only structures.
         *
         * These are NOT part of ParkingSystem.
         *
         * They allow us to detect whether the same slot/token
         * is accidentally assigned to multiple operations.
         */
        Set<ParkingSlot> activeSlots =
                ConcurrentHashMap.newKeySet();


        Set<Token> activeTokens =
                ConcurrentHashMap.newKeySet();


        List<Long> latencies =
                Collections.synchronizedList(
                        new ArrayList<>()
                );


        int operationsPerThread =
                OPERATIONS_PER_ROUND
                        / threadCount;


        for (int threadId = 0;
             threadId < threadCount;
             threadId++) {


            final int gateId =
                    threadId;


            executor.submit(() -> {

                ready.countDown();


                try {

                    /*
                     * Wait until all workers are ready.
                     */
                    startSignal.await();


                    for (int i = 0;
                         i < operationsPerThread;
                         i++) {


                        long begin =
                                System.nanoTime();


                        // =================================================
                        // ENTRY
                        // =================================================

                        VehicleType vehicleType =
                                getVehicleType(
                                        gateId,
                                        i
                                );


                        Vehicle vehicle =
                                new Vehicle(
                                        "PERF-"
                                                + gateId
                                                + "-"
                                                + i,
                                        vehicleType
                                );


                        ParkingSlot slot =
                                parkingSystem
                                        .checkParkingAvailability(
                                                vehicleType
                                        );


                        if (slot == null) {

                            failed.incrementAndGet();

                            continue;
                        }


                        /*
                         * RACE CHECK #1
                         *
                         * If add() returns false, the slot is already
                         * present in activeSlots.
                         *
                         * That means two operations got the same slot
                         * before the first operation released it.
                         */
                        if (!activeSlots.add(slot)) {

                            raceErrors.incrementAndGet();

                            System.err.println(
                                    "RACE CONDITION: "
                                            + "duplicate slot detected: "
                                            + slot
                            );

                            continue;
                        }


                        Token token =
                                parkingSystem.createToken(
                                        vehicle,
                                        gateId,
                                        slot
                                );


                        if (token == null) {

                            activeSlots.remove(slot);

                            failed.incrementAndGet();

                            continue;
                        }


                        /*
                         * RACE CHECK #2
                         *
                         * Token must be unique.
                         */
                        if (!activeTokens.add(token)) {

                            raceErrors.incrementAndGet();

                            System.err.println(
                                    "RACE CONDITION: "
                                            + "duplicate token detected: "
                                            + token
                            );

                            activeSlots.remove(slot);

                            continue;
                        }


                        // =================================================
                        // EXIT
                        // =================================================

                        parkingSystem.exitVehicle(
                                token
                        );


                        /*
                         * The token should have been active.
                         */
                        if (!activeTokens.remove(token)) {

                            raceErrors.incrementAndGet();

                            System.err.println(
                                    "RACE CONDITION: "
                                            + "token was not active during exit."
                            );
                        }


                        /*
                         * The slot should have been active.
                         */
                        if (!activeSlots.remove(slot)) {

                            raceErrors.incrementAndGet();

                            System.err.println(
                                    "RACE CONDITION: "
                                            + "slot was not active during exit."
                            );
                        }


                        successful.incrementAndGet();


                        long end =
                                System.nanoTime();


                        if (collectLatency) {

                            latencies.add(
                                    end - begin
                            );
                        }
                    }


                } catch (Throwable e) {

                    errors.incrementAndGet();

                    e.printStackTrace();

                } finally {

                    finished.countDown();
                }
            });
        }


        /*
         * Wait until all workers are ready.
         */
        ready.await();


        /*
         * Start timing.
         */
        long startTime =
                System.nanoTime();


        /*
         * Release all workers.
         */
        startSignal.countDown();


        /*
         * Wait until all workers finish.
         */
        finished.await();


        long endTime =
                System.nanoTime();


        executor.shutdown();

        executor.awaitTermination(
                30,
                TimeUnit.SECONDS
        );


        /*
         * Any exception is a concurrency failure.
         */
        if (errors.get() > 0) {

            throw new AssertionError(
                    "Concurrency errors detected: "
                            + errors.get()
            );
        }


        /*
         * Final benchmark-level invariant:
         *
         * Since every operation performs ENTRY -> EXIT,
         * there should be no active slots or tokens left.
         */
        if (!activeSlots.isEmpty()) {

            raceErrors.incrementAndGet();

            System.err.println(
                    "RACE CONDITION / LEAK: "
                            + activeSlots.size()
                            + " slots still active."
            );
        }


        if (!activeTokens.isEmpty()) {

            raceErrors.incrementAndGet();

            System.err.println(
                    "RACE CONDITION / LEAK: "
                            + activeTokens.size()
                            + " tokens still active."
            );
        }


        return new RoundResult(
                successful.get(),
                failed.get(),
                raceErrors.get(),
                latencies
        );
    }


    // ============================================================
    // TEST 2
    //
    // HIGH CONTENTION
    //
    // 64 threads all use CAR.
    //
    // This intentionally creates maximum contention on CAR slots.
    // ============================================================

    private static void runHighContentionBenchmark()
            throws Exception {

        System.out.println();
        System.out.println(
                "============== HIGH CONTENTION TEST =============="
        );


        final int THREADS = 64;

        final int OPERATIONS_PER_THREAD = 10_000;

        final int ROUNDS = 10;


        long totalSuccessful = 0;

        long totalFailed = 0;

        long totalRaceErrors = 0;

        long totalNanos = 0;


        for (int round = 1;
             round <= ROUNDS;
             round++) {


            ParkingSystem parkingSystem =
                    createParkingSystem();


            ExecutorService executor =
                    Executors.newFixedThreadPool(
                            THREADS
                    );


            CountDownLatch ready =
                    new CountDownLatch(
                            THREADS
                    );


            CountDownLatch startSignal =
                    new CountDownLatch(1);


            CountDownLatch finished =
                    new CountDownLatch(
                            THREADS
                    );


            AtomicInteger successful =
                    new AtomicInteger();


            AtomicInteger failed =
                    new AtomicInteger();


            AtomicInteger errors =
                    new AtomicInteger();


            AtomicInteger raceErrors =
                    new AtomicInteger();


            /*
             * Benchmark-only verification sets.
             */
            Set<ParkingSlot> activeSlots =
                    ConcurrentHashMap.newKeySet();


            Set<Token> activeTokens =
                    ConcurrentHashMap.newKeySet();


            for (int threadId = 0;
                 threadId < THREADS;
                 threadId++) {


                final int gateId =
                        threadId;


                executor.submit(() -> {

                    ready.countDown();


                    try {

                        startSignal.await();


                        for (int i = 0;
                             i < OPERATIONS_PER_THREAD;
                             i++) {


                            Vehicle vehicle =
                                    new Vehicle(
                                            "STRESS-"
                                                    + gateId
                                                    + "-"
                                                    + i,
                                            VehicleType.CAR
                                    );


                            ParkingSlot slot =
                                    parkingSystem
                                            .checkParkingAvailability(
                                                    VehicleType.CAR
                                            );


                            if (slot == null) {

                                failed.incrementAndGet();

                                continue;
                            }


                            /*
                             * Verify that this slot isn't already
                             * assigned to another active operation.
                             */
                            if (!activeSlots.add(slot)) {

                                raceErrors.incrementAndGet();

                                System.err.println(
                                        "RACE CONDITION: "
                                                + "duplicate CAR slot."
                                );

                                continue;
                            }


                            Token token =
                                    parkingSystem.createToken(
                                            vehicle,
                                            gateId,
                                            slot
                                    );


                            if (token == null) {

                                activeSlots.remove(slot);

                                failed.incrementAndGet();

                                continue;
                            }


                            /*
                             * Verify token uniqueness.
                             */
                            if (!activeTokens.add(token)) {

                                raceErrors.incrementAndGet();

                                activeSlots.remove(slot);

                                System.err.println(
                                        "RACE CONDITION: "
                                                + "duplicate token."
                                );

                                continue;
                            }


                            /*
                             * Immediately release the slot.
                             */
                            parkingSystem.exitVehicle(
                                    token
                            );


                            /*
                             * Verify token was active.
                             */
                            if (!activeTokens.remove(token)) {

                                raceErrors.incrementAndGet();

                                System.err.println(
                                        "RACE CONDITION: "
                                                + "token missing during exit."
                                );
                            }


                            /*
                             * Verify slot was active.
                             */
                            if (!activeSlots.remove(slot)) {

                                raceErrors.incrementAndGet();

                                System.err.println(
                                        "RACE CONDITION: "
                                                + "slot missing during exit."
                                );
                            }


                            successful.incrementAndGet();
                        }


                    } catch (Throwable e) {

                        errors.incrementAndGet();

                        e.printStackTrace();

                    } finally {

                        finished.countDown();
                    }
                });
            }


            ready.await();


            long startTime =
                    System.nanoTime();


            startSignal.countDown();


            finished.await();


            long endTime =
                    System.nanoTime();


            executor.shutdown();

            executor.awaitTermination(
                    30,
                    TimeUnit.SECONDS
            );


            if (errors.get() > 0) {

                throw new AssertionError(
                        "High contention errors: "
                                + errors.get()
                );
            }


            /*
             * Every successful operation exits immediately.
             *
             * Therefore nothing should remain active.
             */
            if (!activeSlots.isEmpty()) {

                raceErrors.incrementAndGet();
            }


            if (!activeTokens.isEmpty()) {

                raceErrors.incrementAndGet();
            }


            if (raceErrors.get() > 0) {

                throw new AssertionError(
                        "RACE CONDITION DETECTED in round "
                                + round
                                + ": "
                                + raceErrors.get()
                                + " violations."
                );
            }


            long successfulCount =
                    successful.get();


            long failedCount =
                    failed.get();


            totalSuccessful +=
                    successfulCount;


            totalFailed +=
                    failedCount;


            totalRaceErrors +=
                    raceErrors.get();


            totalNanos +=
                    endTime - startTime;


            double seconds =
                    (endTime - startTime)
                            / 1_000_000_000.0;


            double throughput =
                    successfulCount
                            / seconds;


            System.out.printf(
                    "Round %-2d | "
                            + "Throughput: %10.2f ops/sec | "
                            + "Success: %-8d | "
                            + "Failed: %-6d | "
                            + "Race Errors: %d%n",
                    round,
                    throughput,
                    successfulCount,
                    failedCount,
                    raceErrors.get()
            );
        }


        double totalSeconds =
                totalNanos
                        / 1_000_000_000.0;


        System.out.println();

        System.out.println(
                "High contention summary:"
        );

        System.out.println(
                "Total successful : "
                        + totalSuccessful
        );

        System.out.println(
                "Total failed     : "
                        + totalFailed
        );

        System.out.println(
                "Race errors      : "
                        + totalRaceErrors
        );

        System.out.printf(
                "Overall throughput: %.2f ops/sec%n",
                totalSuccessful
                        / totalSeconds
        );


        if (totalRaceErrors == 0) {

            System.out.println(
                    "HIGH CONTENTION RACE TEST: PASSED"
            );

        } else {

            throw new AssertionError(
                    "HIGH CONTENTION RACE TEST: FAILED"
            );
        }
    }


    // ============================================================
    // TEST 3
    //
    // MIXED VEHICLE TYPES
    // ============================================================

    private static void runMixedVehicleTypeBenchmark()
            throws Exception {

        System.out.println();
        System.out.println(
                "=========== MIXED VEHICLE TYPE TEST ============"
        );

        printHeader();


        for (int threadCount : THREAD_COUNTS) {


            // -------------------------
            // WARMUP
            // -------------------------

            for (int i = 0;
                 i < WARMUP_ROUNDS;
                 i++) {

                runMixedTypeRound(
                        threadCount,
                        false
                );
            }


            // -------------------------
            // MEASUREMENT
            // -------------------------

            List<Long> allLatencies =
                    new ArrayList<>();


            long totalStart =
                    System.nanoTime();


            long totalSuccessful = 0;

            long totalFailed = 0;

            long totalRaceErrors = 0;


            for (int i = 0;
                 i < MEASURED_ROUNDS;
                 i++) {


                RoundResult result =
                        runMixedTypeRound(
                                threadCount,
                                true
                        );


                totalSuccessful +=
                        result.successful;


                totalFailed +=
                        result.failed;


                totalRaceErrors +=
                        result.raceErrors;


                allLatencies.addAll(
                        result.latencies
                );
            }


            long totalEnd =
                    System.nanoTime();


            if (totalRaceErrors > 0) {

                throw new AssertionError(
                        "RACE CONDITION DETECTED: "
                                + totalRaceErrors
                                + " violations."
                );
            }


            double seconds =
                    (totalEnd - totalStart)
                            / 1_000_000_000.0;


            double throughput =
                    totalSuccessful
                            / seconds;


            LatencyResult latency =
                    calculateLatency(
                            allLatencies
                    );


            System.out.printf(
                    "%-10d %-15.2f %-15.2f %-15.2f %-15.2f%n",
                    threadCount,
                    throughput,
                    latency.averageMicros,
                    latency.p95Micros,
                    latency.p99Micros
            );


            if (totalFailed > 0) {

                System.out.println(
                        "WARNING: "
                                + totalFailed
                                + " operations failed."
                );
            }
        }
    }


    // ============================================================
    // MIXED VEHICLE ROUND
    // ============================================================

    private static RoundResult runMixedTypeRound(
            int threadCount,
            boolean collectLatency)
            throws Exception {


        ParkingSystem parkingSystem =
                createParkingSystem();


        ExecutorService executor =
                Executors.newFixedThreadPool(
                        threadCount
                );


        CountDownLatch ready =
                new CountDownLatch(
                        threadCount
                );


        CountDownLatch startSignal =
                new CountDownLatch(1);


        CountDownLatch finished =
                new CountDownLatch(
                        threadCount
                );


        AtomicInteger successful =
                new AtomicInteger();


        AtomicInteger failed =
                new AtomicInteger();


        AtomicInteger errors =
                new AtomicInteger();


        AtomicInteger raceErrors =
                new AtomicInteger();


        Set<ParkingSlot> activeSlots =
                ConcurrentHashMap.newKeySet();


        Set<Token> activeTokens =
                ConcurrentHashMap.newKeySet();


        List<Long> latencies =
                Collections.synchronizedList(
                        new ArrayList<>()
                );


        int operationsPerThread =
                OPERATIONS_PER_ROUND
                        / threadCount;


        for (int threadId = 0;
             threadId < threadCount;
             threadId++) {


            final int gateId =
                    threadId;


            executor.submit(() -> {

                ready.countDown();


                try {

                    startSignal.await();


                    for (int i = 0;
                         i < operationsPerThread;
                         i++) {


                        long begin =
                                System.nanoTime();


                        VehicleType type =
                                getVehicleType(
                                        gateId,
                                        i
                                );


                        Vehicle vehicle =
                                new Vehicle(
                                        "MIXED-"
                                                + gateId
                                                + "-"
                                                + i,
                                        type
                                );


                        ParkingSlot slot =
                                parkingSystem
                                        .checkParkingAvailability(
                                                type
                                        );


                        if (slot == null) {

                            failed.incrementAndGet();

                            continue;
                        }


                        /*
                         * Race detection.
                         */
                        if (!activeSlots.add(slot)) {

                            raceErrors.incrementAndGet();

                            System.err.println(
                                    "RACE CONDITION: "
                                            + "duplicate slot."
                            );

                            continue;
                        }


                        Token token =
                                parkingSystem.createToken(
                                        vehicle,
                                        gateId,
                                        slot
                                );


                        if (token == null) {

                            activeSlots.remove(slot);

                            failed.incrementAndGet();

                            continue;
                        }


                        /*
                         * Race detection.
                         */
                        if (!activeTokens.add(token)) {

                            raceErrors.incrementAndGet();

                            activeSlots.remove(slot);

                            System.err.println(
                                    "RACE CONDITION: "
                                            + "duplicate token."
                            );

                            continue;
                        }


                        /*
                         * EXIT
                         */
                        parkingSystem.exitVehicle(
                                token
                        );


                        /*
                         * Verify token cleanup.
                         */
                        if (!activeTokens.remove(token)) {

                            raceErrors.incrementAndGet();
                        }


                        /*
                         * Verify slot cleanup.
                         */
                        if (!activeSlots.remove(slot)) {

                            raceErrors.incrementAndGet();
                        }


                        successful.incrementAndGet();


                        long end =
                                System.nanoTime();


                        if (collectLatency) {

                            latencies.add(
                                    end - begin
                            );
                        }
                    }


                } catch (Throwable e) {

                    errors.incrementAndGet();

                    e.printStackTrace();

                } finally {

                    finished.countDown();
                }
            });
        }


        ready.await();


        startSignal.countDown();


        finished.await();


        executor.shutdown();

        executor.awaitTermination(
                30,
                TimeUnit.SECONDS
        );


        if (errors.get() > 0) {

            throw new AssertionError(
                    "Mixed vehicle test errors: "
                            + errors.get()
            );
        }


        /*
         * Because every successful operation exits,
         * no benchmark-tracked state should remain.
         */
        if (!activeSlots.isEmpty()) {

            raceErrors.incrementAndGet();
        }


        if (!activeTokens.isEmpty()) {

            raceErrors.incrementAndGet();
        }


        return new RoundResult(
                successful.get(),
                failed.get(),
                raceErrors.get(),
                latencies
        );
    }


    // ============================================================
    // CREATE PARKING SYSTEM
    // ============================================================

    private static ParkingSystem createParkingSystem() {

        PricingStrategy pricingStrategy =
                new MinutesPricingStrategy();


        FloorFactory floorFactory =
                new FloorFactory();


        return new ParkingSystem(
                pricingStrategy,
                floorFactory
        );
    }


    // ============================================================
    // VEHICLE TYPE
    // ============================================================

    private static VehicleType getVehicleType(
            int threadId,
            int operation) {


        int value =
                (threadId + operation) % 3;


        if (value == 0) {

            return VehicleType.CAR;
        }


        if (value == 1) {

            return VehicleType.BIKE;
        }


        return VehicleType.BUS;
    }


    // ============================================================
    // LATENCY CALCULATION
    // ============================================================

    private static LatencyResult calculateLatency(
            List<Long> latencies) {


        if (latencies.isEmpty()) {

            return new LatencyResult(
                    0,
                    0,
                    0
            );
        }


        /*
         * Sort ALL samples from ALL measured rounds.
         */
        Collections.sort(
                latencies
        );


        long total =
                0;


        for (long latency : latencies) {

            total += latency;
        }


        double averageMicros =
                total
                        / (double) latencies.size()
                        / 1_000.0;


        long p95 =
                percentile(
                        latencies,
                        0.95
                );


        long p99 =
                percentile(
                        latencies,
                        0.99
                );


        return new LatencyResult(
                averageMicros,
                p95 / 1_000.0,
                p99 / 1_000.0
        );
    }


    private static long percentile(
            List<Long> sorted,
            double percentile) {


        int index =
                (int) Math.ceil(
                        percentile
                                * sorted.size()
                ) - 1;


        index =
                Math.max(
                        0,
                        Math.min(
                                index,
                                sorted.size() - 1
                        )
                );


        return sorted.get(index);
    }


    // ============================================================
    // OUTPUT
    // ============================================================

    private static void printHeader() {

        System.out.printf(
                "%-10s %-15s %-15s %-15s %-15s%n",
                "Threads",
                "Ops/sec",
                "Avg(us)",
                "P95(us)",
                "P99(us)"
        );


        System.out.println(
                "----------------------------------------------------------------"
        );
    }


    // ============================================================
    // RESULT CLASSES
    // ============================================================

    private static class RoundResult {

        int successful;

        int failed;

        int raceErrors;

        List<Long> latencies;


        RoundResult(
                int successful,
                int failed,
                int raceErrors,
                List<Long> latencies) {

            this.successful =
                    successful;

            this.failed =
                    failed;

            this.raceErrors =
                    raceErrors;

            this.latencies =
                    latencies;
        }
    }


    private static class LatencyResult {

        double averageMicros;

        double p95Micros;

        double p99Micros;


        LatencyResult(
                double averageMicros,
                double p95Micros,
                double p99Micros) {

            this.averageMicros =
                    averageMicros;

            this.p95Micros =
                    p95Micros;

            this.p99Micros =
                    p99Micros;
        }
    }
}
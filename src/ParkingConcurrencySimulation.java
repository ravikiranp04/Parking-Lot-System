import Floor.FloorFactory;
import ParkingSystem.Gate;
import ParkingSystem.ParkingSystem;
import ParkingSystem.ParkingSlot;
import ParkingSystem.Token;
import ParkingSystem.Vehicle;
import ParkingSystem.VehicleType;
import PricingStrategies.HourlyPricingStrategy;
import ParkingSystem.GateType;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


public class ParkingConcurrencySimulation {
    private static final Logger simLog = Logger.getLogger(ParkingConcurrencySimulation.class.getName());

    // Captures the tokenId parsed out of Token.printToken()'s log line. Only ever written to
    // while holding the ParkingSystem lock, so a single value is safe to share this way.
    private static volatile String lastCapturedTokenId = null;

    private static final Random random = new Random();

    public static void main(String[] args) throws InterruptedException {


        // --- 1. Set up the parking lot's slot inventory ---
        Map<VehicleType, Integer> slotCounts = new HashMap<>();

        FloorFactory floorFactory = new FloorFactory();

        ParkingSystem parkingSystem = new ParkingSystem(new HourlyPricingStrategy(), floorFactory);

        // --- 2. Set up multiple entry and exit gates ---
        int entryGateCount = 3;
        int exitGateCount = 2;
        List<Gate> entryGates = new ArrayList<>();
        for (int i = 1; i <= entryGateCount; i++) {
            entryGates.add(new Gate(i, GateType.ENTRY, parkingSystem));
        }
        List<Gate> exitGates = new ArrayList<>();
        for (int i = 1; i <= exitGateCount; i++) {
            exitGates.add(new Gate(i, GateType.EXIT, parkingSystem));
        }

        // --- 3. Launch many vehicles concurrently ---
        int vehicleCount = 500;
        AtomicInteger entered = new AtomicInteger(0);
        AtomicInteger rejected = new AtomicInteger(0);
        AtomicInteger exited = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(100);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 1; i <= vehicleCount; i++) {
            final int vehicleNum = i;
            futures.add(executor.submit(() ->
                    simulateVehicle(vehicleNum, entryGates, exitGates, parkingSystem, entered, rejected, exited)));
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (ExecutionException e) {
                simLog.warning("A vehicle simulation thread failed: " + e.getCause());
            }
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        simLog.info("=========================================");
        simLog.info("Simulation complete. Vehicles entered: " + entered.get()
                + ", rejected (no slot): " + rejected.get()
                + ", exited: " + exited.get());
    }

    private static void simulateVehicle(int vehicleNum,
                                        List<Gate> entryGates,
                                        List<Gate> exitGates,
                                        ParkingSystem parkingSystem,
                                        AtomicInteger entered,
                                        AtomicInteger rejected,
                                        AtomicInteger exited) {
        try {
            VehicleType type = VehicleType.values()[random.nextInt(VehicleType.values().length)];
            String regNo = "REG-" + vehicleNum;
            Vehicle vehicle = new Vehicle(regNo, type);
            Gate entryGate = entryGates.get(random.nextInt(entryGates.size()));

            Token token = null;

            // Entire entry transaction serialized on the ParkingSystem instance — required
            // since we cannot add synchronization inside ParkingSystem itself, and we need
            // the log-capture window to be exclusive to this thread.
//            synchronized (parkingSystem) {
//                ParkingSlot parkingSlot = parkingSystem.checkParkingAvailability(type);
//                if (parkingSlot != null) {
//                    entryGate.handleEntry(vehicle, parkingSlot);
//                    token = parkingSlot.getTokenDetails();
//                }
//            }

            ParkingSlot parkingSlot = parkingSystem.checkParkingAvailability(type);
            if (parkingSlot != null) {
                entryGate.handleEntry(vehicle, parkingSlot);
                token = parkingSlot.getTokenDetails();
            }


            if (token == null) {
                rejected.incrementAndGet();
                simLog.info("Vehicle-" + vehicleNum + " (" + type + ") found no slot available at Entry Gate "
                        + entryGate.getGateId() + ", turned away.");
                return;
            }
            entered.incrementAndGet();

            // Simulate the vehicle staying parked for a short, random duration
            Thread.sleep(50 + random.nextInt(200));

            Gate exitGate = exitGates.get(random.nextInt(exitGates.size()));
            final String tokenIdToExit = token.getTokenid();

            // Exit also serialized on the same lock — ParkingSystem.exitVehicle() and
            // isTokenActive() are not synchronized in the original code, so without this
            // external lock concurrent exits could corrupt the shared maps.
//            synchronized (parkingSystem) {
//                if (!parkingSystem.isTokenActive(tokenIdToExit)) {
//                    simLog.warning("Vehicle-" + vehicleNum + " token no longer active — skipping exit.");
//                    return;
//                }
//                exitGate.handleExit(token); // 100% original, unmodified call
//            }
            if (!parkingSystem.isTokenActive(tokenIdToExit)) {
                simLog.warning("Vehicle-" + vehicleNum + " token no longer active — skipping exit.");
                return;
            }
            exitGate.handleExit(token); // 100% original, unmodified call
            exited.incrementAndGet();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            simLog.warning("Vehicle-" + vehicleNum + " thread interrupted.");
        } catch (Exception e) {
            simLog.warning("Vehicle-" + vehicleNum + " simulation failed: " + e.getMessage());
        }
    }
}
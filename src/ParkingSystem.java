import java.time.LocalDateTime;
import java.util.*;

public class ParkingSystem {

    private static Map<VehicleType, Queue<Integer>> availableSlotIds;
    private static Map<String,Token> tokenIdToTokenMap;
    ParkingSystem(int busSlots, int carSlots, int bikeSlots){
        tokenIdToTokenMap = new HashMap<>();
        availableSlotIds = new HashMap<>();

        Queue<Integer> busQueue = new LinkedList<>();
        for (int i = 1; i <= busSlots; i++)
            busQueue.offer(i);
        availableSlotIds.put(VehicleType.BUS, busQueue);

        Queue<Integer> carQueue = new LinkedList<>();
        for (int i = 1; i <= carSlots; i++)
            carQueue.offer(i);
        availableSlotIds.put(VehicleType.CAR, carQueue);

        Queue<Integer> bikeQueue = new LinkedList<>();
        for (int i = 1; i <= bikeSlots; i++)
            bikeQueue.offer(i);
        availableSlotIds.put(VehicleType.BIKE, bikeQueue);

    }
    boolean checkSlotAvailability(VehicleType vehicleType){
        return availableSlotIds.get(vehicleType).size()>0;
    }
    ParkingSlot assignParkingSlot(Vehicle vehicleDetails){
        int slotId = availableSlotIds.get(vehicleDetails.getVehicleType()).poll();
        return new ParkingSlot(slotId,vehicleDetails);
    }
    Token createToken(Vehicle vehicleDetails){
        ParkingSlot parkingSlot = assignParkingSlot(vehicleDetails);
        Token tokenDetails = new Token(vehicleDetails,parkingSlot);
        tokenIdToTokenMap.put(tokenDetails.getTokenid(),tokenDetails);
        parkingSlot.setTokenDetails(tokenDetails);
        return tokenDetails;
    }
    void exitVehicle(Token tokenDetails){
        if(tokenDetails.getParkingSlot().getVehicleDetails()==null){
            System.out.println("Invalid Token\n");
            return;
        }
        tokenDetails.setExitTime();
        tokenIdToTokenMap.remove(tokenDetails.getTokenid());
        double totalFare = tokenDetails.calculateFare();
        System.out.println("Please pay Rs. "+totalFare);
        releaseParkingSlot(tokenDetails);
        System.out.println("Gates Opened.\n");
    }
    void releaseParkingSlot(Token tokenDetails){
        ParkingSlot parkingSlot = tokenDetails.getParkingSlot();
        Vehicle vehicleDetails = tokenDetails.getVehicleDetails();
        availableSlotIds.get(vehicleDetails.getVehicleType()).offer(parkingSlot.getSlotId());
        parkingSlot.setVehicleDetails(null);
        parkingSlot.setTokenDetails(null);
        parkingSlot.setIsOccupied(false);
   }
   Token getToken(String tokenId){
        return tokenIdToTokenMap.get(tokenId);
   }
   boolean isTokenActive(String tokenId){
        return tokenIdToTokenMap.containsKey(tokenId);
   }

}

package ParkingSystem;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;
import PricingStrategies.pricingStrategy;

public class ParkingSystem {
    private static final Logger log = Logger.getLogger(ParkingSystem.class.getName());
    private final Object lock = new Object();
    private Map<VehicleType, Queue<Integer>> availableSlotIds;
    private Map<String,Token> tokenIdToTokenMap;
    private pricingStrategy pricingStrategy;
    public ParkingSystem(Map<VehicleType, Integer> slotCountsByType, pricingStrategy pricingStrategy){
        tokenIdToTokenMap = new HashMap<>();
        availableSlotIds = new HashMap<>();
        for(VehicleType vehicleType: VehicleType.values()){
            Queue<Integer> queue = new LinkedList<>();
            for(int i=1;i<=slotCountsByType.get(vehicleType);i++){
                queue.offer(i);
            }
            availableSlotIds.put(vehicleType,queue);
        }
        this.pricingStrategy=pricingStrategy;
    }
    public boolean checkSlotAvailability(VehicleType vehicleType){
        return availableSlotIds.get(vehicleType).size()>0;
    }
    ParkingSlot assignParkingSlot(Vehicle vehicleDetails){
        int slotId = availableSlotIds.get(vehicleDetails.getVehicleType()).poll();
        return new ParkingSlot(slotId,vehicleDetails);
    }
    Token createToken(Vehicle vehicleDetails,Integer gateId){
        synchronized (lock){
            ParkingSlot parkingSlot = assignParkingSlot(vehicleDetails);
            Token tokenDetails = new Token(vehicleDetails,parkingSlot,gateId);
            tokenIdToTokenMap.put(tokenDetails.getTokenid(),tokenDetails);
            parkingSlot.setTokenDetails(tokenDetails);
            return tokenDetails;
        }
    }
    void exitVehicle(Token tokenDetails){
        tokenDetails.setExitTime();
        tokenIdToTokenMap.remove(tokenDetails.getTokenid());
        BigDecimal totalFare = pricingStrategy.calculateFare(tokenDetails);
        log.info("Please pay Rs. "+totalFare);
        releaseParkingSlot(tokenDetails);
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
   public boolean isTokenActive(String tokenId){
        return tokenIdToTokenMap.containsKey(tokenId);
   }

}

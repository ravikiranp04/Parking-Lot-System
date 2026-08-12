import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class ParkingSystem {
    private static final Logger log = Logger.getLogger(ParkingSystem.class.getName());
    private final Object lock = new Object();
    private Map<VehicleType, Queue<Integer>> availableSlotIds;
    private Map<String,Token> tokenIdToTokenMap;
    private PricingStrategy pricingStrategy;
    ParkingSystem(Map<VehicleType, Integer> slotCountsByType,PricingStrategy pricingStrategy){
        tokenIdToTokenMap = new HashMap<>();
        availableSlotIds = new HashMap<>();
        for(VehicleType vehicleType: VehicleType.values()){
            Queue<Integer> queue = new LinkedList<>();
            for(int i=1;i<=slotCountsByType.get(vehicleType);i++){
                availableSlotIds.get(vehicleType).offer(i);
            }
        }
        this.pricingStrategy=pricingStrategy;
    }
    boolean checkSlotAvailability(VehicleType vehicleType){
        return availableSlotIds.get(vehicleType).size()>0;
    }
    ParkingSlot assignParkingSlot(Vehicle vehicleDetails){
        int slotId = availableSlotIds.get(vehicleDetails.getVehicleType()).poll();
        return new ParkingSlot(slotId,vehicleDetails);
    }
    Token createToken(Vehicle vehicleDetails){
        synchronized (lock){
            ParkingSlot parkingSlot = assignParkingSlot(vehicleDetails);
            BigDecimal pricePerHour = pricingStrategy.getHourlyPrice(vehicleDetails.getVehicleType());
            Token tokenDetails = new Token(vehicleDetails,parkingSlot, pricePerHour);
            tokenIdToTokenMap.put(tokenDetails.getTokenid(),tokenDetails);
            parkingSlot.setTokenDetails(tokenDetails);
            return tokenDetails;
        }
    }
    void exitVehicle(Token tokenDetails){
        tokenDetails.setExitTime();
        tokenIdToTokenMap.remove(tokenDetails.getTokenid());
        BigDecimal totalFare = tokenDetails.calculateFare();
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
   boolean isTokenActive(String tokenId){
        return tokenIdToTokenMap.containsKey(tokenId);
   }

}

package ParkingSystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import Floor.FloorFactory;
import PricingStrategies.PricingStrategy;

public class ParkingSystem {
    private static final Logger log = Logger.getLogger(ParkingSystem.class.getName());
    private final Object lock = new Object();
    private Map<String,Token> tokenIdToTokenMap;
    private FloorFactory floorFactory;
    private PricingStrategy pricingStrategy;

    public ParkingSystem(PricingStrategy pricingStrategy, FloorFactory floorFactory){
        tokenIdToTokenMap = new ConcurrentHashMap<>();
        this.pricingStrategy=pricingStrategy;
        this.floorFactory = floorFactory;
    }

    public FloorFactory getFloorFactory() {
        return floorFactory;
    }

    public ParkingSlot checkParkingAvailability(VehicleType vehicleType){
        return floorFactory.getAvailability(vehicleType);
    }

    Token createToken(Vehicle vehicleDetails,Integer gateId, ParkingSlot parkingSlot){

        Token tokenDetails = new Token(vehicleDetails, parkingSlot, gateId);
        tokenIdToTokenMap.put(tokenDetails.getTokenid(), tokenDetails);
        parkingSlot.setTokenDetails(tokenDetails);
        return tokenDetails;

//        synchronized (lock) {
//            Token tokenDetails = new Token(vehicleDetails, parkingSlot, gateId);
//            tokenIdToTokenMap.put(tokenDetails.getTokenid(), tokenDetails);
//            parkingSlot.setTokenDetails(tokenDetails);
//            return tokenDetails;
//        }
    }
    void exitVehicle(Token tokenDetails){
        tokenIdToTokenMap.remove(tokenDetails.getTokenid());
        releaseParkingSlot(tokenDetails);
    }
    void releaseParkingSlot(Token tokenDetails){
        ParkingSlot parkingSlot = tokenDetails.getParkingSlot();
        floorFactory.releaseParkingSlot(parkingSlot);
        parkingSlot.freeSlot();

   }
   public Token getToken(String tokenId){
        return tokenIdToTokenMap.get(tokenId);
   }
   public boolean isTokenActive(String tokenId){
        return tokenIdToTokenMap.containsKey(tokenId);
   }

    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }

}

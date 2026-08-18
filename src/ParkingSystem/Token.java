package ParkingSystem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

public class Token {
    private String tokenid;
    private Vehicle vehicleDetails;
    private ParkingSlot parkingSlot;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Integer entryGateId;
    private Integer exitGateId;
    private static final Logger log = Logger.getLogger(Token.class.getName());

    Token(Vehicle vehicleDetails, ParkingSlot parkingSlot,Integer entryGateId){
        this.vehicleDetails=vehicleDetails;
        this.parkingSlot=parkingSlot;
        this.tokenid=UUID.randomUUID().toString();
        this.entryTime = LocalDateTime.now();
        this.entryGateId=entryGateId;
    }
    void setExitTime(){
        this.exitTime=LocalDateTime.now();
    }
    public LocalDateTime getEntryTime(){
        return entryTime;
    }
    public LocalDateTime getExitTime(){
        return exitTime;
    }
    void setExitGateId(Integer exitGateId){
        this.exitGateId=exitGateId;
    }
    void setEntryGateId(Integer entryGateId){
        this.entryGateId=entryGateId;
    }
    Integer getEntryGateId(){
        return entryGateId;
    }
    Integer getExitGateId(){
        return exitGateId;
    }
    void printToken(){
        log.info("Printing ParkingSystem.Token:");
        log.info("Entry ParkingSystem.Gate: "+entryGateId);
        log.info("ParkingSystem.Token Id: "+ getTokenid());
        log.info("RegNo: "+vehicleDetails.getRegistrationNumber());
        log.info("Entry Time: "+entryTime);
        log.info("ParkingSystem.Vehicle Type: "+vehicleDetails.getVehicleType());
        log.info("Parking Slot: "+parkingSlot.getSlotId());
    }
    ParkingSlot getParkingSlot(){
        return parkingSlot;
    }
    public Vehicle getVehicleDetails(){
        return vehicleDetails;
    }
    String getTokenid(){
        return tokenid;
    }


}

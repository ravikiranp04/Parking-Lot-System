package ParkingSystem;


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
    public Integer getEntryGateId(){
        return entryGateId;
    }
    public Integer getExitGateId(){
        return exitGateId;
    }
    void printToken(){
        log.info("Printing ParkingSystem.Token:");
        log.info("Entry ParkingSystem.Gate: "+entryGateId);
        log.info("ParkingSystem.Token Id: "+ getTokenid());
        log.info("RegNo: "+vehicleDetails.getRegistrationNumber());
        log.info("Entry Time: "+entryTime);
        log.info("ParkingSystem.Vehicle Type: "+vehicleDetails.getVehicleType());
        log.info("Parking Slot Details: ");
        getSlotDetails();
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

    public void getSlotDetails(){
        log.info("Floor: "+parkingSlot.getFloorId()+", Slot: "+parkingSlot.getSlotId());
        return;
    }
    public void setExitTime(){
        this.exitTime=LocalDateTime.now();
    }
}

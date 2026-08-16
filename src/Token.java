import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

public class Token {
    private String tokenid;
    private Vehicle vehicleDetails;
    private ParkingSlot parkingSlot;
    private LocalDateTime entryTime;
    private BigDecimal pricePerHour;
    private LocalDateTime exitTime;
    private Integer entryGateId;
    private Integer exitGateId;
    private static final Logger log = Logger.getLogger(Token.class.getName());

    Token(Vehicle vehicleDetails, ParkingSlot parkingSlot, BigDecimal pricePerHour,Integer entryGateId){
        this.vehicleDetails=vehicleDetails;
        this.parkingSlot=parkingSlot;
        this.tokenid=UUID.randomUUID().toString();
        this.entryTime = LocalDateTime.now();
        this.pricePerHour=pricePerHour;
        this.entryGateId=entryGateId;
    }
    void setExitTime(){
        this.exitTime=LocalDateTime.now();
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
        log.info("Printing Token:");
        log.info("Entry Gate: "+entryGateId);
        log.info("Token Id: "+ getTokenid());
        log.info("RegNo: "+vehicleDetails.getRegistrationNumber());
        log.info("Entry Time: "+entryTime);
        log.info("Vehicle Type: "+vehicleDetails.getVehicleType());
        log.info("Parking Slot: "+parkingSlot.getSlotId());
    }
    BigDecimal calculateFare(){
        Duration totalDuration = Duration.between(entryTime, exitTime);

        BigDecimal totalHours = BigDecimal.valueOf(totalDuration.toMinutes() / 60.0);
        VehicleType vehicleType = vehicleDetails.getVehicleType();
        log.info("Total Duration :"+ totalDuration.toHours()+ " hours "+totalDuration.toMinutes()%60+ "Mins.\n");
        BigDecimal totalFare = totalHours.multiply(pricePerHour);
        return totalFare;
    }
    ParkingSlot getParkingSlot(){
        return parkingSlot;
    }
    Vehicle getVehicleDetails(){
        return vehicleDetails;
    }
    String getTokenid(){
        return tokenid;
    }


}

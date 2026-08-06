import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class Token {
    private String tokenid;
    private Vehicle vehicleDetails;
    private ParkingSlot parkingSlot;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private boolean isTokenValid;

    Token(Vehicle vehicleDetails, ParkingSlot parkingSlot){
        this.vehicleDetails=vehicleDetails;
        this.parkingSlot=parkingSlot;
        this.tokenid=UUID.randomUUID().toString();
        this.entryTime = LocalDateTime.now();
        this.isTokenValid=true;
    }
    void setExitTime(){
        this.exitTime=LocalDateTime.now();

    }
    void printToken(){
        System.out.println("Printing Token:");
        System.out.println("Token Id: "+getTokenid());
        System.out.println("RegNo: "+vehicleDetails.getRegistrationNumber());
        System.out.println("Entry Time: "+entryTime);
        System.out.println("Vehicle Type: "+vehicleDetails.getVehicleType());
        System.out.println("Parking Slot: "+parkingSlot.getSlotId());
    }
    double calculateFare(){
        Duration totalDuration = Duration.between(entryTime, exitTime);

        double totalHours = totalDuration.toMinutes() / 60.0;

        System.out.println("Total Duration :"+ totalDuration.toHours()+ " hours "+totalDuration.toMinutes()%60+ "Mins.\n");
        double totalFare = totalHours * vehicleDetails.getVehicleType().getHourlyPrice();
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

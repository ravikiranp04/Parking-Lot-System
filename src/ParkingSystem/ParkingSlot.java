package ParkingSystem;

public class ParkingSlot {
    private int slotId;
    private Token tokenDetails;
    private Vehicle vehicleDetails;
    private boolean isOccupied;
    ParkingSlot(int slotId,Vehicle vehicleDetails){
        this.slotId=slotId;
        this.vehicleDetails=vehicleDetails;
        this.isOccupied= true;
    }
    void setVehicleDetails(Vehicle vehicleDetails){
        this.vehicleDetails=vehicleDetails;
    }
    void setTokenDetails(Token tokenDetails){
        this.tokenDetails=tokenDetails;
    }
    Vehicle getVehicleDetails(){
        return vehicleDetails;
    }
    Token getTokenDetails(){
        return tokenDetails;
    }
    Integer getSlotId(){
        return slotId;
    }
    void setIsOccupied(boolean value){
        this.isOccupied=value;
    }
}

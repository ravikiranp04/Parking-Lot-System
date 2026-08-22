package ParkingSystem;

public class ParkingSlot {
    private Integer slotId;

    public void setFloorId(Integer floorId) {
        this.floorId = floorId;
    }

    private Integer floorId;

    private Token tokenDetails;
    private Vehicle vehicleDetails;

    public ParkingSlot(Integer floorId, Integer slotId){
        this.slotId=slotId;
        this.floorId=floorId;
    }

    void setVehicleDetails(Vehicle vehicleDetails){
        this.vehicleDetails=vehicleDetails;
    }

    void setTokenDetails(Token tokenDetails){
        this.tokenDetails=tokenDetails;
    }

    public Token getTokenDetails() {
        return tokenDetails;
    }

    public Vehicle getVehicleDetails() {
        return vehicleDetails;
    }

    public Integer getFloorId() {
        return floorId;
    }

    public Integer getSlotId(){
        return slotId;
    }
    void freeSlot(){
        setVehicleDetails(null);
        setTokenDetails(null);
        setFloorId(null);
    }
}

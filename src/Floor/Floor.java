package Floor;

import ParkingSystem.ParkingSlot;
import ParkingSystem.VehicleType;

import java.util.Queue;

public class Floor {
    private Queue<Integer> availableSlots;
    private VehicleType vehicleType;
    private Integer floorId;
    Floor(Integer floorId,VehicleType vehicleType, Integer slots){
        this.vehicleType=vehicleType;
        this.floorId=floorId;
        for(int i=1;i<=slots;i++){
            availableSlots.offer(i);
        }
    }
    public ParkingSlot getSlot(){
        if(availableSlots.isEmpty()){
            return null;
        }
        return new ParkingSlot(floorId,availableSlots.poll());
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public Integer getFloorId() {
        return floorId;
    }

    void releaseSlot(Integer slotId){
        availableSlots.offer(slotId);
    }
}

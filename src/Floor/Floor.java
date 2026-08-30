package Floor;

import ParkingSystem.ParkingSlot;
import ParkingSystem.VehicleType;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Floor {
    private ArrayBlockingQueue<Integer> availableSlots;
    private VehicleType vehicleType;
    private Integer floorId;
    Floor(Integer floorId,VehicleType vehicleType, Integer slots){
        this.vehicleType=vehicleType;
        this.floorId=floorId;
        this.availableSlots = new ArrayBlockingQueue<>(slots);
        for(Integer i=1;i<=slots;i++){
            availableSlots.offer(i);
        }
    }
    public ParkingSlot allocateSlot(){
        Integer slotId = availableSlots.poll();
        if(slotId==null){
            return null;
        }
        return new ParkingSlot(floorId,slotId);
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

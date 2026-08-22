package Floor;

import ParkingSystem.ParkingSystem;
import ParkingSystem.Token;
import ParkingSystem.Vehicle;
import ParkingSystem.VehicleType;
import com.sun.source.tree.ForLoopTree;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import ParkingSystem.ParkingSlot;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloorFactory {
    private Map<VehicleType, ArrayList<Floor>> vehicleTypeToFloors;
    private Map<Integer, Floor> floorIdToFloor;
    public FloorFactory(){
        vehicleTypeToFloors = new HashMap<>();
        floorIdToFloor = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, List<Object>> floorData =
                mapper.readValue(
                        new File("src/FloorData.json"),
                        new TypeReference<Map<String, List<Object>>>() {}
                );
        for (Map.Entry<String, List<Object>> entry : floorData.entrySet()) {

            Integer floorId = Integer.parseInt(entry.getKey());

            VehicleType vehicleType =
                    VehicleType.valueOf((String) entry.getValue().get(0));

            Integer totalSlots =
                    (Integer) entry.getValue().get(1);
            Floor floor = new Floor(floorId, vehicleType,totalSlots);

            floorIdToFloor.put(floorId, floor);
            vehicleTypeToFloors.computeIfAbsent(vehicleType, k -> new ArrayList<>()).add(floor);
        }
    }

    public ParkingSlot getAvailability(VehicleType vehicleType){
        ParkingSlot parkingSlot=null;
        for(Floor floor: vehicleTypeToFloors.get(vehicleType)){
            parkingSlot = floor.allocateSlot();
            if(parkingSlot!=null){
                return parkingSlot;
            }
        }
        return parkingSlot;
    }

    public void releaseParkingSlot(ParkingSlot parkingSlot){
        Integer floorId = parkingSlot.getFloorId();
        Integer slotId = parkingSlot.getSlotId();
        floorIdToFloor.get(floorId).releaseSlot(slotId);
    }
}

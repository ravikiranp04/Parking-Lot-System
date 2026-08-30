package ParkingSystem;

import java.util.logging.Logger;
public class Gate {
    public GateType getGateType() {
        return gateType;
    }

    public Integer getGateId() {
        return gateId;
    }

    private Integer gateId;
    private GateType gateType;
    private ParkingSystem parkingSystem;
    private static final Logger log = Logger.getLogger(Gate.class.getName());
    public Gate(Integer gateId, GateType gateType, ParkingSystem parkingSystem){
        this.gateId=gateId;
        this.gateType=gateType;
        this.parkingSystem=parkingSystem;
    }
    public void handleEntry(Vehicle vehicleDetails, ParkingSlot parkingSlot){

        parkingSlot.setVehicleDetails(vehicleDetails);
        Token tokenDetails = parkingSystem.createToken(vehicleDetails,gateId, parkingSlot);
        log.info("Token Created for Vehicle "+vehicleDetails.getRegistrationNumber()+", "+vehicleDetails.getVehicleType()+" at Entry ParkingSystem.Gate: "+tokenDetails.getEntryGateId());
        tokenDetails.printToken();
        return;
    }
    public void handleExit(Token tokenDetails){

        tokenDetails.setExitGateId(gateId);
        parkingSystem.exitVehicle(tokenDetails);
        log.info("Gates Opened. Vehicle exit at Gate Id: "+tokenDetails.getExitGateId());
        return;
    }
}

package ParkingSystem;

import java.util.logging.Logger;

public class Gate {
    Integer gateId;
    GateType gateType;
    ParkingSystem parkingSystem;
    private static final Logger log = Logger.getLogger(Gate.class.getName());
    public Gate(Integer gateId, GateType gateType, ParkingSystem parkingSystem){
        this.gateId=gateId;
        this.gateType=gateType;
        this.parkingSystem=parkingSystem;
    }
    public void handleEntry(Vehicle vehicleDetails){
        Token tokenDetails = parkingSystem.createToken(vehicleDetails,gateId);
        log.info("Token Created for ParkingSystem.Vehicle "+vehicleDetails.getRegistrationNumber()+", "+vehicleDetails.getVehicleType()+" at Entry ParkingSystem.Gate: "+tokenDetails.getEntryGateId());
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

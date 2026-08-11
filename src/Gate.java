import java.util.logging.Logger;

public class Gate {
    Integer gateId;
    GateType gateType;
    ParkingSystem parkingSystem;
    private static final Logger log = Logger.getLogger(Gate.class.getName());
    Gate(Integer gateId, GateType gateType, ParkingSystem parkingSystem){
        this.gateId=gateId;
        this.gateType=gateType;
        this.parkingSystem=parkingSystem;
    }
    void handleEntry(Vehicle vehicleDetails){
        Token tokenDetails = parkingSystem.createToken(vehicleDetails);
        log.info("Token Created for Vehicle "+vehicleDetails.getRegistrationNumber()+", "+vehicleDetails.getVehicleType()+" at Entry Gate: "+tokenDetails.getEntryGateId());
        tokenDetails.printToken();
        return;
    }
    void handleExit(String tokenId){
        Token tokenDetails = parkingSystem.getToken(tokenId);
        Vehicle vehicleDetails = tokenDetails.getVehicleDetails();
        tokenDetails.setExitGateId(gateId);
        parkingSystem.exitVehicle(tokenDetails);
        log.info("Gates Opened. Vehicle exit at Gate Id: "+gateId);

        return;
    }
}

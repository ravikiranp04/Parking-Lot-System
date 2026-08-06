public class Vehicle {
    private String registrationNumber;
    private VehicleType vehicleType;

    Vehicle(String registrationNumber, VehicleType vehicleType){
        this.registrationNumber=registrationNumber;
        this.vehicleType=vehicleType;
    }
    VehicleType getVehicleType(){
        return vehicleType;
    }
    String getRegistrationNumber(){
        return registrationNumber;
    }
}

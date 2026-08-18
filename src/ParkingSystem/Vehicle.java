package ParkingSystem;

public class Vehicle {
    private String registrationNumber;
    private VehicleType vehicleType;

    public Vehicle(String registrationNumber, VehicleType vehicleType){
        this.registrationNumber=registrationNumber;
        this.vehicleType=vehicleType;
    }
    public VehicleType getVehicleType(){
        return vehicleType;
    }
    String getRegistrationNumber(){
        return registrationNumber;
    }
}

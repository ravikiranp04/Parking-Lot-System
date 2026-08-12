import java.util.HashMap;
import java.util.Scanner;
import java.util.*;
import java.util.logging.Logger;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

public class Main {
    static Scanner sc = new Scanner(System.in);
    private static final Logger log = Logger.getLogger(Main.class.getName());
    public static VehicleType selectVehicleType(){
        System.out.println("Enter Vehicle Type (1/2/3) \n1.CAR\n2.BIKE\n3.BUS");
        int num = sc.nextInt();
        VehicleType vehicleType = null;
        switch(num){
            case 1:
                vehicleType = VehicleType.CAR;
                break;
            case 2:
                vehicleType = VehicleType.BIKE;
                break;
            case 3:
                vehicleType = VehicleType.BUS;
                break;
            default:
                break;
        };
        return vehicleType;
    }

    public static ParkingSystem initializeParkingSystem(){
        Map<VehicleType,Integer> slotsCountByType= new HashMap<>();
        System.out.println("Enter no of bus slots");
        int busSlots = sc.nextInt();
        slotsCountByType.put(VehicleType.BUS,busSlots);

        System.out.println("Enter no of car slots");
        int carSlots = sc.nextInt();
        slotsCountByType.put(VehicleType.CAR,carSlots);

        System.out.println("Enter no of bike slots");
        int bikeSlots = sc.nextInt();
        slotsCountByType.put(VehicleType.BIKE,bikeSlots);

        ParkingSystem parkingSystem = new ParkingSystem(slotsCountByType, new PricingStrategy());
        return parkingSystem;
    }

    public static void main(String[] args) {
        //Initializing Parking System
        ParkingSystem parkingSystem = initializeParkingSystem();
        System.out.println("Enter Number of Entry Gates");
        int entryGatesCount=sc.nextInt();
        System.out.println("Enter Number of Exit Gates");
        int exitGatesCount = sc.nextInt();
        Map<Integer, Gate> entryGatesRegistry = new HashMap<>();
        for(int i=1;i<=entryGatesCount;i++){
            entryGatesRegistry.put(i,new Gate(i,GateType.ENTRY,parkingSystem));
        }
        Map<Integer, Gate> exitGatesRegistry = new HashMap<>();
        for(int i=1;i<=exitGatesCount;i++){
            exitGatesRegistry.put(i,new Gate(i,GateType.ENTRY,parkingSystem));
        }
        while(true){
           System.out.println("Enter an Option:\n1)Enter Vehicle\n2) Exit Vehicle");
           int option = sc.nextInt();
           switch(option){
               case 1:

                   //Selecting Vehicle Type and Entry Gate Id
                   System.out.println("Enter Entry Gate Id (Max: :"+entryGatesCount+")");
                   Integer entryGateId = sc.nextInt();
                   Gate entryGate = entryGatesRegistry.get(entryGateId);

                   //Selecting Vehicle Type
                   VehicleType vehicleType = selectVehicleType();
                   if(vehicleType==null){
                       System.out.println("Invalid Vehicle Type");
                       break;
                   }
                   // Checking Slot availability
                    if(!parkingSystem.checkSlotAvailability(vehicleType)){
                        log.info("No slots available");
                        break;
                    }
                    //Enter Vehicle Reg No
                   System.out.println("Enter Registration Number");
                   String registrationNumber = sc.next();
                   Vehicle vehicleDetails = new Vehicle(registrationNumber,vehicleType);

                    //Creating and printing Token at gate
                    entryGate.handleEntry(vehicleDetails);
                    break;

               case 2:
                   System.out.println("Enter Exit Gate Id (Max: :"+exitGatesCount+")");
                   Integer exitGateId = sc.nextInt();

                   // Get Exit Gate Object
                   Gate exitGate = exitGatesRegistry.get(exitGateId);
                   System.out.println("Scan the Token");
                   String tokenId = sc.next();

                   // Checking Token Validity
                   if(!parkingSystem.isTokenActive(tokenId)){
                       log.warning("Exit Attempted at Gate "+exitGateId+" with invalid token.");
                       break;
                   }
                   // Retreiving Token Details and Exit clearance
                   exitGate.handleExit(tokenId);

                   break;
               default:
                   System.out.println("Invalid Option");
                   break;
           };
       }
    }
}
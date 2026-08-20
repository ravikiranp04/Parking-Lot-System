import ParkingSystem.ParkingSystem;
import ParkingSystem.VehicleType;
import PaymentProcessor.CardPaymentProcessor;
import PaymentProcessor.CashPaymentProcessor;
import PaymentProcessor.PaymentProcessor;
import PaymentProcessor.UpiPaymentProcessor;
import PricingStrategies.minutesPricingStrategy;
import PricingStrategies.pricingStrategy;
import PaymentProcessor.PaymentResult;
import ParkingSystem.Gate;
import ParkingSystem.GateType;
import ParkingSystem.Vehicle;
import ParkingSystem.Token;
import java.math.BigDecimal;
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
        log.info("Enter Vehicle Type (1/2/3) \n1.CAR\n2.BIKE\n3.BUS");
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
        log.info("Enter no of bus slots");
        int busSlots = sc.nextInt();
        slotsCountByType.put(VehicleType.BUS,busSlots);

        log.info("Enter no of car slots");
        int carSlots = sc.nextInt();
        slotsCountByType.put(VehicleType.CAR,carSlots);

        log.info("Enter no of bike slots");
        int bikeSlots = sc.nextInt();
        slotsCountByType.put(VehicleType.BIKE,bikeSlots);

        pricingStrategy pricingStrategy = new minutesPricingStrategy();
        ParkingSystem parkingSystem = new ParkingSystem(slotsCountByType,pricingStrategy);
        return parkingSystem;
    }

    public static PaymentResult collectPayment(BigDecimal totalFare){
        while(true){
            log.info("Select Payment Method.\n1)UPI\n2)Card\n3)Cash");
            int paymentOption = sc.nextInt();
            PaymentProcessor paymentProcessor;
            PaymentResult paymentResult;
            switch(paymentOption){
                case 1:
                    paymentProcessor = new UpiPaymentProcessor();
                    paymentResult=paymentProcessor.pay(totalFare);
                    if(paymentResult.isPaymentStatus()){
                        log.info("Payment successful");
                        return paymentResult;
                    }
                    log.info("Payment Unsuccessful");
                    break;
                case 2:
                    paymentProcessor = new CardPaymentProcessor();
                    paymentResult=paymentProcessor.pay(totalFare);
                    if(paymentResult.isPaymentStatus()){
                        log.info("Payment Successful");
                        return paymentResult;

                    }
                    log.info("Payment Unsuccessful");
                    break;

                case 3:
                    paymentProcessor = new CashPaymentProcessor();
                    paymentResult=paymentProcessor.pay(totalFare);
                    log.info("Payment Successful");
                    return paymentResult;
                default:
                    log.info("Invalid Option");
                    break;
            }
        }


    }

    public static void main(String[] args) {
        //Initializing Parking System
        ParkingSystem parkingSystem = initializeParkingSystem();
        log.info("Enter Number of Entry Gates");
        int entryGatesCount=sc.nextInt();
        log.info("Enter Number of Exit Gates");
        int exitGatesCount = sc.nextInt();
        Map<Integer, Gate> entryGatesRegistry = new HashMap<>();
        for(int i=1;i<=entryGatesCount;i++){
            entryGatesRegistry.put(i,new Gate(i, GateType.ENTRY,parkingSystem));
        }
        Map<Integer, Gate> exitGatesRegistry = new HashMap<>();
        for(int i=1;i<=exitGatesCount;i++){
            exitGatesRegistry.put(i,new Gate(i, GateType.EXIT,parkingSystem));
        }
        while(true){
           log.info("----------\nEnter an Option:\n1) Enter Vehicle\n2) Exit Vehicle");
           int option = sc.nextInt();
           switch(option){
               case 1:

                   //Selecting Vehicle Type and Entry Gate Id
                   log.info("Enter Entry Gate Id (Max: :"+entryGatesCount+")");
                   Integer entryGateId = sc.nextInt();
                   Gate entryGate = entryGatesRegistry.get(entryGateId);

                   //Selecting Vehicle Type
                   VehicleType vehicleType = selectVehicleType();
                   if(vehicleType==null){
                       log.info("Invalid Vehicle Type");
                       break;
                   }
                   // Checking Slot availability
                    if(!parkingSystem.checkSlotAvailability(vehicleType)){
                        log.info("No slots available");
                        break;
                    }
                    //Enter Vehicle Reg No
                   log.info("Enter Registration Number");
                   String registrationNumber = sc.next();
                   Vehicle vehicleDetails = new Vehicle(registrationNumber,vehicleType);

                    //Creating and printing Token at gate
                    entryGate.handleEntry(vehicleDetails);
                    break;

               case 2:
                   log.info("Enter Exit Gate Id (Max: :"+exitGatesCount+")");
                   Integer exitGateId = sc.nextInt();

                   // Get Exit Gate Object
                   Gate exitGate = exitGatesRegistry.get(exitGateId);
                   log.info("Scan the Token");
                   String tokenId = sc.next();
                   Token tokenDetails = parkingSystem.getToken(tokenId);
                   // Checking Token Validity
                   if(!parkingSystem.isTokenActive(tokenId)){
                       log.warning("Exit Attempted at Gate "+exitGateId+" with invalid token.");
                       break;
                   }
                   tokenDetails.setExitTime();
                   //Fare Calculation
                   pricingStrategy pricingStrategy = parkingSystem.getPricingStrategy();
                   BigDecimal totalFare = pricingStrategy.calculateFare(parkingSystem.getToken(tokenId));
                   log.info("Please pay Rs. "+totalFare);

                   //Payment Processing
                   PaymentResult paymentResult=collectPayment(totalFare);
                   //Exit clearance
                   if(paymentResult.isPaymentStatus()){
                       exitGate.handleExit(tokenDetails);
                   }

                   break;
               default:
                   log.info("Invalid Option");
                   break;
           };
       }
    }
}
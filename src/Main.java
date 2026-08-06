import java.util.Scanner;
import java.util.UUID;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter no of bus slots");
        int busSlots = sc.nextInt();
        System.out.println("Enter no of car slots");
        int carSlots = sc.nextInt();
        System.out.println("Enter no of bike slots");
        int bikeSlots = sc.nextInt();
       ParkingSystem parkingSystem = new ParkingSystem(busSlots,carSlots,bikeSlots);

       while(true){
           System.out.println("Enter an Option:\n1) Enter Vehicle\n2) Exit Vehicle");
           int option = sc.nextInt();
           switch(option){
               case 1:
                   System.out.println("Enter Vehicle Type (1/2/3) \n1.CAR\n2.BIKE\n3.BUS\n4.Previous Menu");
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
                       case 4:
                           continue;
                       default:
                           System.out.println("Invalid Vehicle Type");
                           break;
                   };

                    if(!parkingSystem.checkSlotAvailability(vehicleType)){
                        System.out.println("No slots availble");
                        break;
                    }

                   System.out.println("Enter Registration Number");
                   String registrationNumber = sc.next();
                   Vehicle vehicleDetails = new Vehicle(registrationNumber,vehicleType);
                   Token tokenDetails = parkingSystem.createToken(vehicleDetails);
                   tokenDetails.printToken();
                    break;

               case 2:
                   System.out.println("Scan the Token\n");
                   String tokenId = sc.next();
                   if(!parkingSystem.isTokenActive(tokenId)){
                       System.out.print("Invalid Token\n");
                       break;
                   }
                   tokenDetails = parkingSystem.getToken(tokenId);
                   parkingSystem.exitVehicle(tokenDetails);
                   break;
               default:
                   System.out.println("Invalid Token");
                   break;
           };
       }
    }
}
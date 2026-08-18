package PricingStrategies;
import ParkingSystem.Token;
import ParkingSystem.Vehicle;
import ParkingSystem.VehicleType;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class hourlyPricingStrategy implements pricingStrategy {
    private Map<VehicleType, BigDecimal> hourlyPrices;
    public hourlyPricingStrategy(){
        hourlyPrices = new HashMap<>();
        hourlyPrices.put(VehicleType.CAR, BigDecimal.valueOf(40.0));
        hourlyPrices.put(VehicleType.BIKE, BigDecimal.valueOf(20.0));
        hourlyPrices.put(VehicleType.BUS, BigDecimal.valueOf(80.0));
    }

    @Override
    public BigDecimal getHourlyPrice(VehicleType vehicleType){
        return hourlyPrices.get(vehicleType);
    }

    @Override
    public BigDecimal calculateFare(Token tokenDetails){
        Duration totalDuration = Duration.between(tokenDetails.getEntryTime(), tokenDetails.getExitTime());
        Vehicle vehicleDetails = tokenDetails.getVehicleDetails();
        VehicleType vehicleType = vehicleDetails.getVehicleType();
        BigDecimal totalHours = BigDecimal.valueOf(totalDuration.toMinutes() / 60.0);
        BigDecimal pricePerHour = hourlyPrices.get(vehicleType);
        log.info("Total Duration :"+ totalDuration.toHours()+ " hours "+totalDuration.toMinutes()%60+ "Mins.\n");
        BigDecimal totalFare = totalHours.multiply(pricePerHour);
        return totalFare;
    }
}
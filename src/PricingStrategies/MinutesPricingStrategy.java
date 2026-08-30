package PricingStrategies;

import ParkingSystem.Token;
import ParkingSystem.Vehicle;
import ParkingSystem.VehicleType;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class MinutesPricingStrategy implements PricingStrategy {
    private Map<VehicleType, BigDecimal> minutesPrices;
    public MinutesPricingStrategy(){
        minutesPrices = new HashMap<>();
        minutesPrices.put(VehicleType.CAR, BigDecimal.valueOf(1.0));
        minutesPrices.put(VehicleType.BIKE, BigDecimal.valueOf(0.5));
        minutesPrices.put(VehicleType.BUS, BigDecimal.valueOf(2.0));
    }

    @Override
    public BigDecimal getHourlyPrice(VehicleType vehicleType){
        return minutesPrices.get(vehicleType);
    }

    @Override
    public BigDecimal calculateFare(Token tokenDetails){
        Duration totalDuration = Duration.between(tokenDetails.getEntryTime(), tokenDetails.getExitTime());
        Vehicle vehicleDetails = tokenDetails.getVehicleDetails();
        VehicleType vehicleType = vehicleDetails.getVehicleType();
        BigDecimal totalMinutes = BigDecimal.valueOf(totalDuration.toMinutes());
        BigDecimal pricePerMinute = minutesPrices.get(vehicleType);
        log.info("Total Duration : "+totalDuration.toMinutes()+ "Mins.\n");
        BigDecimal totalFare = totalMinutes.multiply(pricePerMinute);
        return totalFare;
    }
}

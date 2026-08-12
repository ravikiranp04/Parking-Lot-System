import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PricingStrategy {
    private Map<VehicleType, BigDecimal> hourlyPrices;
    PricingStrategy(){
        hourlyPrices = new HashMap<>();
        hourlyPrices.put(VehicleType.CAR, BigDecimal.valueOf(40.0));
        hourlyPrices.put(VehicleType.BIKE, BigDecimal.valueOf(20.0));
        hourlyPrices.put(VehicleType.BUS, BigDecimal.valueOf(80.0));
    }
    BigDecimal getHourlyPrice(VehicleType vehicleType){
        return hourlyPrices.get(vehicleType);
    }
    void setHourlyPrices(VehicleType vehicleType, BigDecimal newPrice){
        hourlyPrices.put(vehicleType,newPrice);
    }
}

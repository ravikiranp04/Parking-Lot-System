package PricingStrategies;
import ParkingSystem.Token;
import ParkingSystem.VehicleType;

import java.math.BigDecimal;
import java.util.logging.Logger;

public interface pricingStrategy {
    static final Logger log = Logger.getLogger(pricingStrategy.class.getName());

    BigDecimal calculateFare(Token tokenDetails);

    BigDecimal getHourlyPrice(VehicleType vehicleType);
}

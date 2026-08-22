package PricingStrategies;
import ParkingSystem.Token;
import ParkingSystem.VehicleType;

import java.math.BigDecimal;
import java.util.logging.Logger;

public interface PricingStrategy {
    static final Logger log = Logger.getLogger(PricingStrategy.class.getName());

    BigDecimal calculateFare(Token tokenDetails);

    BigDecimal getHourlyPrice(VehicleType vehicleType);
}

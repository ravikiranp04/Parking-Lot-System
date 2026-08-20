package PaymentProcessor;

import java.math.BigDecimal;
import java.util.logging.Logger;

public interface PaymentProcessor {
    static final Logger log = Logger.getLogger(PaymentProcessor.class.getName());
    PaymentResult pay(BigDecimal amt);
}

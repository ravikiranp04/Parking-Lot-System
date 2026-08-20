package PaymentProcessor;

import java.math.BigDecimal;
import java.util.logging.Logger;

public interface paymentProcessor {
    static final Logger log = Logger.getLogger(paymentProcessor.class.getName());
    void pay(BigDecimal amt);
}

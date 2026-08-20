package PaymentProcessor;

import java.math.BigDecimal;
import java.util.UUID;

public class UpiPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentResult pay(BigDecimal amount){
        log.info("Paid Rs "+amount+" via UPI");
        return new PaymentResult(false, UUID.randomUUID().toString());
    }
}

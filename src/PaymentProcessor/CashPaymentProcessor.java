package PaymentProcessor;

import java.math.BigDecimal;
import java.util.UUID;

public class CashPaymentProcessor implements PaymentProcessor{
    @Override
    public PaymentResult pay(BigDecimal amount){
        log.info("Paid Rs "+amount+" via Cash");
        return new PaymentResult(true, UUID.randomUUID().toString());
    }
}

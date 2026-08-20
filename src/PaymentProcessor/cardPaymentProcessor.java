package PaymentProcessor;

import java.math.BigDecimal;

public class cardPaymentProcessor implements paymentProcessor {

    @Override
    public void pay(BigDecimal amount){
        log.info("Paid Rs {} via Card" + amount);
    }
}


package PaymentProcessor;

import java.math.BigDecimal;

public class upiPaymentProcessor implements paymentProcessor{

    @Override
    public void pay(BigDecimal amount){
        log.info("Paid Rs {} via UPI" + amount);
    }
}

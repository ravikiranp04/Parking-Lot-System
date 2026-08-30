package PaymentProcessor;

public class PaymentResult {
    private boolean paymentStatus;
    private String transactionId;
    PaymentResult(boolean paymentStatus, String transactionId){
        this.paymentStatus=paymentStatus;
        this.transactionId=transactionId;
    }

    public boolean isPaymentStatus() {
        return paymentStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }
}

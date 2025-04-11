package kafka.streams.message;

public class PaymentCreatedMessage {
    
    public String id;
    public String user_id;
    public double amount;
    public String created_at;

    
    public PaymentCreatedMessage(String id, String user_id, double amount, String created_at) {
        this.id = id;
        this.user_id = user_id;
        this.amount = amount;
        this.created_at = created_at;
    }
}

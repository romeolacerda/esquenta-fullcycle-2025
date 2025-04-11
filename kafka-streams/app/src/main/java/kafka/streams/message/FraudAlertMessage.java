package kafka.streams.message;

public class FraudAlertMessage {
    
    private String userId;
    private String alertMessage;

    public FraudAlertMessage(String userId, String alertMessage) {
        this.userId = userId;
        this.alertMessage = alertMessage;
    }

    public String getUserId() {
        return userId;
    }

    public String getAlertMessage() {
        return alertMessage;
    }
}

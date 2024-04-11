package chatbot.entity;

public class ReportRequest {
    private Long messageId;
    private String reason;

    public Long getMessageId() {
        return messageId;
    }
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
    
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }


    

}
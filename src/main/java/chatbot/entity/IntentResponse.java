package chatbot.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "intent_response")
public class IntentResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intent_name")
    private String intentName;

    @Column(name = "response_text", columnDefinition="TEXT")
    private String responseText;


    public IntentResponse() {
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIntentName() {
        return intentName;
    }

    public void setIntentName(String intentName) {
        this.intentName = intentName;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }
}

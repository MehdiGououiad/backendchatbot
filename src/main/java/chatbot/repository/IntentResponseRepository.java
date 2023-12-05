package chatbot.repository;

import chatbot.entity.IntentResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntentResponseRepository extends JpaRepository<IntentResponse, Long> {

    IntentResponse findByIntentName(String intentName);

}

package chatbot.repository;

import chatbot.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository  extends JpaRepository<Conversation, Long> {

        List<Conversation> findByUserId(Long userId);

}

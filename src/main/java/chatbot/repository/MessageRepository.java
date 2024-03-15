package chatbot.repository;

import chatbot.entity.Conversation;
import chatbot.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // Find messages by conversation
    List<Message> findByConversation(Conversation conversation);
}
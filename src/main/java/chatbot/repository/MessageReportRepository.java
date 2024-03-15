package chatbot.repository;

import chatbot.entity.Message;
import chatbot.entity.MessageReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageReportRepository extends JpaRepository<MessageReport, Long> {
    // Find MessageReports by Message
    List<MessageReport> findByMessage(Message message);
}
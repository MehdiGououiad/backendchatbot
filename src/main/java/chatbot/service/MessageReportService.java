package chatbot.service;

import chatbot.entity.Message;
import chatbot.entity.MessageReport;
import chatbot.repository.MessageReportRepository;
import chatbot.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class MessageReportService {

    private final MessageReportRepository messageReportRepository;
    private final MessageRepository messageRepository;

    @Autowired
    public MessageReportService(MessageReportRepository messageReportRepository, MessageRepository messageRepository) {
        this.messageReportRepository = messageReportRepository;
        this.messageRepository = messageRepository;
    }

    public MessageReport reportMessage(Long messageId, String reason) {
        if (messageId == null) {
            throw new IllegalArgumentException("Message ID must not be null");
        }
        // Fetch the Message entity
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (!messageOptional.isPresent()) {
            // Handle the case where the message doesn't exist, e.g., throw an exception
            throw new RuntimeException("Message not found with id: " + messageId); // Consider using a more specific exception type
        }
        Message message = messageOptional.get();

        // Create and populate the MessageReport entity
        MessageReport messageReport = new MessageReport();
        messageReport.setMessage(message); // Associate the fetched Message with the MessageReport
        messageReport.setReportType(reason);

        // Save and return the MessageReport entity
        return messageReportRepository.save(messageReport);
    }
}

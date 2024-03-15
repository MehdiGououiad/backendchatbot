package chatbot.service;

import chatbot.entity.Conversation;
import chatbot.entity.Message;
import chatbot.entity.MessageReport;
import chatbot.entity.User;
import chatbot.repository.ConversationRepository;
import chatbot.repository.MessageReportRepository;
import chatbot.repository.MessageRepository;
import chatbot.repository.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MessageReportRepository messageReportRepository; // Assume this exists

    @Autowired
    public ConversationService(ConversationRepository conversationRepository,
                               UserRepository userRepository,
                               MessageRepository messageRepository,
                               MessageReportRepository messageReportRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageReportRepository = messageReportRepository; // Initialize in constructor
    }
    public Conversation createConversation(Long userId) throws Exception {

        // Find the user by userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found with id: " + userId));

        // Create a new conversation and set its title and user
        Conversation conversation = new Conversation();
        conversation.setUser(user);




        // Save the conversation to the repository
        return conversationRepository.save(conversation);
    }
    public Conversation modifyConversationTitle( Long conversationId, String newTitle) throws Exception {
        // Find the conversation by conversationId
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new Exception("Conversation not found with id: " + conversationId));


        // Modify the conversation (e.g., update the title)
        conversation.setTitle(newTitle);

        // Save the modified conversation
        return conversationRepository.save(conversation);
    }
    public List<Conversation> getAllConversationsByUserId(Long userId) {
        // Retrieve all conversations associated with the specified user ID

        // Call the repository to retrieve conversations by user ID
        List<Conversation> conversations = conversationRepository.findByUserId(userId);

        // Optionally, you can perform additional processing or filtering here if needed
        return conversations;
    }

    public List<Message> getMessagesByConversationId(Long conversationId) throws Exception {
        // Retrieve the conversation by its ID
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new Exception("Conversation not found with ID: " + conversationId));

        // Get the messages associated with the conversation
        List<Message> messages = new ArrayList<>(conversation.getMessages());

        // Sort the messages by timestamp in ascending order
        messages.sort(Comparator.comparing(Message::getTimestamp));

        return messages;
    }

    @Transactional
    public void deleteConversationById(Long conversationId) throws Exception {
        // Check if the conversation exists
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new Exception("Conversation not found with ID: " + conversationId));

        // Find all Messages associated with the Conversation and delete them
        // (and their associated MessageReports if applicable)
        List<Message> messagesToDelete = messageRepository.findByConversation(conversation);
       for (Message message : messagesToDelete) {
    // Fetch associated MessageReports
    List<MessageReport> reportsToDelete = messageReportRepository.findByMessage(message);

    // Delete fetched MessageReportsv
    messageReportRepository.deleteAll(reportsToDelete);
}

        // Now delete all messages for the conversation
        messageRepository.deleteAll(messagesToDelete);

        // Finally, delete the conversation itself
        conversationRepository.delete(conversation);
    }


}

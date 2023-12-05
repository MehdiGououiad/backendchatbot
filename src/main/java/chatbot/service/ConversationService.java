package chatbot.service;

import chatbot.entity.Conversation;
import chatbot.entity.Message;
import chatbot.entity.User;
import chatbot.repository.ConversationRepository;
import chatbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;

    @Autowired
    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }


    @Autowired
    private UserRepository userRepository;

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
    public Conversation modifyConversationTitle(Long userId, Long conversationId, String newTitle) throws Exception {
        // Find the user by userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found with id: " + userId));

        // Find the conversation by conversationId
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new Exception("Conversation not found with id: " + conversationId));

        // Check if the conversation is associated with the specified user
        if (!conversation.getUser().equals(user)) {
            throw new Exception("User does not have permission to modify this conversation");
        }

        // Modify the conversation (e.g., update the title)
        conversation.setTitle(newTitle);

        // Save the modified conversation
        return conversationRepository.save(conversation);
    }
    public List<Conversation> getAllConversationsByUserId(Long userId) {
        // Retrieve all conversations associated with the specified user ID
        System.out.println("userId: " + userId);
        // Call the repository to retrieve conversations by user ID
        List<Conversation> conversations = conversationRepository.findByUserId(userId);

        // Optionally, you can perform additional processing or filtering here if needed
System.out.println("conversations: " + conversations);
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

    public void deleteConversationById(Long conversationId) throws Exception {
        // Check if the conversation exists
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new Exception("Conversation not found with ID: " + conversationId));
//        List<Message> messagesToDelete = messageRepository.findByConversation(conversation);
//        messageRepository.deleteAll(messagesToDelete);

        // Delete the conversation
        conversationRepository.delete(conversation);
    }

    //TODO: Implement the methods for the ConversationService class
    //i need a method to get messages of one conversation id
    // i need a method to get all conversations of one user id
    // i need a method to get all conversations
    // i need a method to create a conversation
    // i need a method to delete a conversation


}

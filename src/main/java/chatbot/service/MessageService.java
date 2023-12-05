package chatbot.service;

import chatbot.entity.Conversation;
import chatbot.entity.Message;
import chatbot.repository.ConversationRepository;
import chatbot.repository.MessageRepository;
import chatbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.parser.Entity;

@Service
public class MessageService {
    private final MessageRepository messageRepository;


    @Autowired
    private  ConversationRepository conversationRepository;


    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void saveQuestionFromUser(String question,Long conversationId) throws Exception {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new Exception("Conversation not found"));
        Message message = new Message();
        message.setContent(question);
        message.setMessageType("Question"); // Set the message type as needed
        message.setConversation(conversation); // Set the conversation for the message

        // You might also want to set other properties like conversation, timestamp, etc.

        // Save the message to the database
        messageRepository.save(message);
    }

}

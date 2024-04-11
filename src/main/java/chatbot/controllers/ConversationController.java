package chatbot.controllers;

import chatbot.entity.Conversation;
import chatbot.entity.Message;
import chatbot.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;
    @Autowired
    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }
    @PostMapping("/create")
    public ResponseEntity<List<Conversation>> createConversation(@RequestParam("user_id") Long userId) {
        try {
            // Call the service method to create a conversation with the specified user_id and title
            Conversation conversation = conversationService.createConversation(userId);
            

            // Return the created conversation with a 201 Created status
            List<Conversation> conversations = conversationService.getAllConversationsByUserId(userId);
return ResponseEntity.ok(conversations);
        }  catch (Exception e) {
            // Handle other exceptions, such as validation errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @GetMapping("/conversationsByUserId")
    public ResponseEntity<List<Conversation>> getAllConversationsByUserId(@RequestParam Long userId) {
        List<Conversation> conversations = conversationService.getAllConversationsByUserId(userId);

        if (conversations.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(conversations);
        }
    }
//     Refactor to message controller
    @GetMapping("/messagesByConversationId")
    public ResponseEntity<List<Message>> getMessagesByConversationId(@RequestParam Long conversationId ) throws Exception {
        List<Message> messages = conversationService.getMessagesByConversationId(conversationId);

        if (messages.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(messages);
        }
    }

    @PutMapping("/updateTitle")
    public ResponseEntity<Conversation> modifyTitle(
            @RequestParam Long conversationId,
            @RequestParam String newTitle) {
        try {
            Conversation updatedConversation = conversationService.modifyConversationTitle(conversationId, newTitle);
            return ResponseEntity.ok(updatedConversation);
        } catch (Exception e) {
            // Handle different exceptions accordingly
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

}

    @DeleteMapping("/deleteByConversationId")
    public ResponseEntity<List<Conversation>> deleteConversationById(@RequestParam Long conversationId,    @RequestParam Long userId
    ) throws Exception {
        conversationService.deleteConversationById(conversationId);
        List<Conversation> conversations = conversationService.getAllConversationsByUserId(userId);
        return ResponseEntity.ok(conversations);
    }


}
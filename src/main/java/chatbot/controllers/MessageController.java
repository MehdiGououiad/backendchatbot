package chatbot.controllers;

import chatbot.entity.IntentResponse;
import chatbot.entity.Message;
import chatbot.repository.ConversationRepository;
import chatbot.repository.IntentResponseRepository;
import chatbot.repository.MessageRepository;
import chatbot.service.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/questions")
public class MessageController {

    private final MessageService messageService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final IntentResponseRepository intentResponseRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;




    public MessageController(MessageService messageService, ObjectMapper objectMapper, IntentResponseRepository intentResponseRepository, MessageRepository messageRepository, ConversationRepository conversationRepository) {
        this.messageService = messageService;
        this.intentResponseRepository = intentResponseRepository;
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();

    }
    @PostMapping("/ask")
    public ResponseEntity<String> askQuestion(@RequestParam String question, @RequestParam Long conversationId) {
        try {
            messageService.saveQuestionFromUser(question, conversationId);

            // Send the question to the Rasa API and handle the response as a string
            // URL for Rasa API
            String rasaApiUrl = "http://localhost:5005/model/parse";



// Create a Map for your request body
            Map<String, String> map = new HashMap<>();
            map.put("text", question);

// Use Jackson ObjectMapper to serialize the map to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(map);

// Print the JSON request body

// Set headers to indicate JSON content type with UTF-8 encoding
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

// Create an HttpEntity with headers and request body
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

// Use RestTemplate to send the POST request
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(rasaApiUrl, requestEntity, String.class);

// Print the response status code


            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                // Handle the successful response
                JsonNode rasaResponse = objectMapper.readTree(responseEntity.getBody());
                System.out.println(rasaResponse);
                String intent = rasaResponse.get("intent").get("name").asText();
                String confidence = rasaResponse.get("intent").get("confidence").asText();
                double num = Double.parseDouble(confidence);

                IntentResponse intentResponse = intentResponseRepository.findByIntentName(intent);
                if (intentResponse != null && num>0.7) {
                    // Store the response in the Message entity (assuming you have a Message entity)
                    Message message = new Message();
                    message.setContent(intentResponse.getResponseText());
                    message.setConfidence(confidence);
                    message.setIntentName(intent);
                    // Set other properties like messageType, conversation, etc., as needed

                    message.setMessageType("Response");
                    message.setConversation(conversationRepository.findById(conversationId).orElseThrow(() -> new Exception("Conversation not found")));
                    messageRepository.save(message);

                    // Save the message in your database (use your message repository)
                    // messageRepository.save(message);

                    // Handle the response as needed
                    return ResponseEntity.ok(intentResponse.getResponseText());
                } else {
                    // Handle the case where there's no matching response
                    Message message = new Message();
                    message.setContent("Pour le moment, je ne suis pas en mesure de répondre à ta question, car je suis toujours en cours d'entraînement.");
                    // Set other properties like messageType, conversation, etc., as needed

                    message.setMessageType("Response");
                    message.setConversation(conversationRepository.findById(conversationId).orElseThrow(() -> new Exception("Conversation not found")));
                    messageRepository.save(message);
                    return ResponseEntity.ok().body("No response found for intent: " + intent);
                }
            } else {
                // Handle non-successful response (e.g., 4xx or 5xx status code)
                return ResponseEntity.status(responseEntity.getStatusCode()).body(null);
            }
        } catch (Exception e) {
            // Handle any exceptions, e.g., Conversation not found
            return ResponseEntity.badRequest().body(null);
        }
    }
    }

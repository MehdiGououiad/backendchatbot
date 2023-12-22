package chatbot.controllers;

import chatbot.entity.IntentResponse;
import chatbot.entity.Message;
import chatbot.repository.ConversationRepository;
import chatbot.repository.IntentResponseRepository;
import chatbot.repository.MessageRepository;
import chatbot.service.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
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


            // Your Flask API URL (update the IP address and port as needed)
            String flaskApiUrl = "http://localhost:5000/get_intent";

// No encoding is applied to the sentence
            String completeUrl = flaskApiUrl + "?sentence=" + question;

// Set headers to indicate JSON content type with UTF-8 encoding
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

// Create an HttpEntity with headers (no request body needed for GET request)
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

// Use RestTemplate to send the GET request
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(completeUrl, String.class, requestEntity);

// Print the response status code


            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode flaskResponse = objectMapper.readTree(responseEntity.getBody());
                JsonNode intents = flaskResponse.get("intents");

                // Check if there are at least two intents to compare
                if (intents.size() > 1) {
                    double firstIntentConfidence = intents.get(0).get("confidence").asDouble();
                    double secondIntentConfidence = intents.get(1).get("confidence").asDouble();

                    // Check if the difference in confidence is greater than 0.5
                    if (firstIntentConfidence - secondIntentConfidence < 0.05) {
                        // Return the top three intents as JSON
                        // Query the database for the top intents and return their responses
                        ArrayNode responses = objectMapper.createArrayNode();
                        for (int i = 0; i < Math.min(3, intents.size()); i++) {
                            String intentName = intents.get(i).get("intent").asText();
                            IntentResponse intentResponse = intentResponseRepository.findByIntentName(intentName);
                            if (intentResponse != null) {
                                ObjectNode responseInfo = objectMapper.createObjectNode();
                                responseInfo.put("intent", intentName);
                                responseInfo.put("response", intentResponse.getResponseText());
                                responses.add(responseInfo);
                            }
                        }
                        return ResponseEntity.ok().body(responses.toString());
                    }
                }

                String intent = flaskResponse.get("intents").get(0).get("intent").asText();
                System.out.println("Intent: " + intent);    // Print the intent

                IntentResponse intentResponse = intentResponseRepository.findByIntentName(intent);
                if (intentResponse != null) {
                    Message message = new Message();
                    message.setContent(intentResponse.getResponseText());
                    message.setIntentName(intent);
                    message.setConfidence(flaskResponse.get("intents").get(0).get("confidence").asText());
                    message.setMessageType("Response");
                    message.setConversation(conversationRepository.findById(conversationId).orElseThrow(() -> new Exception("Conversation not found")));
                    messageRepository.save(message);
                    // Handle the response as needed
                    return ResponseEntity.ok(String.valueOf(message.getId()));
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

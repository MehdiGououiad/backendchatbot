package chatbot.controllers;

import chatbot.entity.IntentResponse;
import chatbot.entity.Message;
import chatbot.entity.MessageReport;
import chatbot.entity.QuestionRequest;
import chatbot.entity.ReportRequest;
import chatbot.repository.ConversationRepository;
import chatbot.repository.IntentResponseRepository;
import chatbot.repository.MessageRepository;
import chatbot.service.MessageReportService;
import chatbot.service.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final MessageReportService messageReportService;

    private final ObjectMapper objectMapper;
    private final IntentResponseRepository intentResponseRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;


    public MessageController(MessageService messageService , MessageReportService messageReportService,  ObjectMapper objectMapper, IntentResponseRepository intentResponseRepository, MessageRepository messageRepository, ConversationRepository conversationRepository) {
        this.messageService = messageService;
        this.objectMapper = objectMapper;
        this.intentResponseRepository = intentResponseRepository;
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.messageReportService = messageReportService;

    }

  @PostMapping("/ask")
public ResponseEntity<String> askQuestion(@RequestBody QuestionRequest request) {
    try {
        messageService.saveQuestionFromUser(request.getQuestion(), request.getConversationId());

        if ("2".equals(request.getVersion())) {
            return handleVersion2(request.getQuestion(), request.getConversationId());
        } else {
            return handleVersion1(request.getQuestion(), request.getConversationId());
        }
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
}


@PostMapping("/report")
public ResponseEntity<MessageReport> reportMessage(@RequestBody ReportRequest request) {

        Long messageId = request.getMessageId();
        String reason = request.getReason();
        MessageReport reportedMessage = messageReportService.reportMessage(messageId, reason);
        return ResponseEntity.ok(reportedMessage);
        
    }


    private ResponseEntity<String> handleVersion1(String question, Long conversationId) throws Exception {
        // Logic for version 1
        String flaskApiUrl = "http://localhost:5000/get_intent";
        String completeUrl = flaskApiUrl + "?sentence=" + question;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(completeUrl, String.class, requestEntity);
            
            // Process the response assuming it's successful
            JsonNode flaskResponse = objectMapper.readTree(responseEntity.getBody());
            return processFlaskResponse(flaskResponse, conversationId);
        
        } catch (Exception e) {
            // Log the exception for debugging
            System.out.println("Exception occurred while calling Embedding API: " + e.getMessage());
            
            // Return a different status code, e.g., 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Error: Embedding API is currently unavailable or returned an error.");
        }
        
    }

    private ResponseEntity<String> handleVersion2(String question, Long conversationId) throws Exception {
        // Logic for version 2
        String apiUrl = "http://localhost:8000/query";
        String jsonPayload = "{\"text\":\"" + question + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);

        RestTemplate restTemplate = new RestTemplate();
       try {
    ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
    
    // Process the response assuming it's successful
    JsonNode llmResponse = objectMapper.readTree(responseEntity.getBody());
    return processLLMResponse(llmResponse, conversationId);

} catch (Exception e) {
    // Log the exception for debugging
    System.out.println("Exception occurred: " + e.getMessage());
    
    // Handle the case where the LLM API is down, not reachable, or returns a non-2xx response
    System.out.println("LLM API down or returned a non-2xx response");
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Error: LLM API is currently unavailable or returned an error.");
}

    }

    private ResponseEntity<String> processFlaskResponse(JsonNode flaskResponse, Long conversationId) throws Exception {
        JsonNode intents = flaskResponse.get("intents");
        if (intents.size() == 0) {
            throw new Exception("No intents found in Flask response");
        }
    
        
        // Check for multiple intents with close confidence levels
        boolean isCloseConfidence = false;
        if (intents.size() > 1) {
            double firstConfidence = intents.get(0).get("confidence").asDouble();
            double secondConfidence = intents.get(1).get("confidence").asDouble();
    
            // Define the threshold for "close confidence"
            final double CONFIDENCE_THRESHOLD = 0.03;
            if (Math.abs(firstConfidence - secondConfidence) < CONFIDENCE_THRESHOLD) {
                isCloseConfidence = true;
            }
        }
    
        if (isCloseConfidence) {
            // Handle case where multiple intents have close confidence levels
            StringBuilder responseBuilder = new StringBuilder();
            for (JsonNode intentNode : intents) {
                String intentName = intentNode.get("intent").asText();
                IntentResponse intentResponse = intentResponseRepository.findByIntentName(intentName);
                if (intentResponse != null) {
                    responseBuilder.append(intentResponse.getResponseText()).append("\n");
                }
            }
            
            Message message = new Message();
            message.setContent(responseBuilder.toString());
            message.setMessageType("Responsemultiple");
            message.setConversation(conversationRepository.findById(conversationId).orElseThrow(() -> new Exception("Conversation not found")));
            messageRepository.save(message);
    
            return ResponseEntity.ok(String.valueOf(message.getId()));
        } else {
            // Single intent handling (similar to previous implementation)
            JsonNode topIntent = intents.get(0);
            String intentName = topIntent.get("intent").asText();
            double confidence = topIntent.get("confidence").asDouble();
    
            IntentResponse intentResponse = intentResponseRepository.findByIntentName(intentName);
            if (intentResponse == null) {
                throw new Exception("No response found for intent: " + intentName);
            }
    
            Message message = new Message();
            message.setContent(intentResponse.getResponseText());
            message.setIntentName(intentName);
            message.setConfidence(Double.toString(confidence));
            message.setMessageType("Response");
            message.setConversation(conversationRepository.findById(conversationId).orElseThrow(() -> new Exception("Conversation not found")));
            messageRepository.save(message);
            return ResponseEntity.ok(String.valueOf(message.getId()));
        }
    }
    private ResponseEntity<String> processLLMResponse(JsonNode llmResponse, Long conversationId) throws Exception {
        JsonNode responseNode = llmResponse.get("response");
        if (responseNode == null) {
            throw new Exception("No response found in LLM response");
        }
        String responseText = responseNode.asText();
        Message message = new Message();
        message.setContent(responseText);
        message.setMessageType("ResponseGenerative");
        message.setConversation(conversationRepository.findById(conversationId).orElseThrow(() -> new Exception("Conversation not found")));
        messageRepository.save(message);
        return ResponseEntity.ok(String.valueOf(message.getId()));
    }
    // Additional private helper methods as needed
}
package com.example.pdfreader.controller;

import com.example.pdfreader.controller.response.EstimateResponse;
import com.example.pdfreader.service.PdfService;
import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class PdfController {

    @Autowired
    private PdfService pdfService;


    @Value("${openai.api.key}")
    private String openaiApiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @PostMapping("/extract")
    public ResponseEntity<EstimateResponse> extractTextFromPdf(@RequestParam("file") MultipartFile file) {
        try {
            String extractedText = pdfService.extractText(file);
            Map<String, String> response = new HashMap<>();

            RestTemplate restTemplate = new RestTemplate();
            String userMessageContent = "You are an expert in project planning. Based on the following input, provide a detailed estimate plan including timeline, resources, and potential risks. Here are the project details:\n" + extractedText;

            JSONObject openaiRequest = new JSONObject();
//        openaiRequest.put("model", "gpt-3.5-turbo");
            openaiRequest.put("model", "gpt-4");

            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are an expert in project planning.");

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", userMessageContent);

            messages.put(systemMessage);
            messages.put(userMessage);

            openaiRequest.put("messages", messages);
            openaiRequest.put("max_tokens", 150);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openaiApiKey);

            HttpEntity<String> entity = new HttpEntity<>(openaiRequest.toString(), headers);

            ResponseEntity<String> response1 = restTemplate.postForEntity(OPENAI_API_URL, entity, String.class);

            String responseBody = response1.getBody();
            JSONObject jsonResponse = new JSONObject(responseBody);
            String openaiResponseText = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

            String[] lines = openaiResponseText.split("\n");
            String timeEstimate = lines[0].trim();
            String costEstimate = lines[1].trim();

            EstimateResponse estimateResponse = new EstimateResponse();
            estimateResponse.setTimeEstimate(timeEstimate);
            estimateResponse.setCostEstimate(costEstimate);
            estimateResponse.setDescription(openaiResponseText);
            System.out.println(estimateResponse);

            return new ResponseEntity<>(estimateResponse, HttpStatus.OK);

        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to extract text from PDF");
            return new ResponseEntity<>(new EstimateResponse(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

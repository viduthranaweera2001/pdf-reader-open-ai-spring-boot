package com.example.pdfreader.controller;

import com.example.pdfreader.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
//@RequestMapping("/api/pdf")
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @PostMapping("/extract")
    public ResponseEntity<Map<String, String>> extractTextFromPdf(@RequestParam("file") MultipartFile file) {
        try {
            String extractedText = pdfService.extractText(file);
            Map<String, String> response = new HashMap<>();
            response.put("text", extractedText);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to extract text from PDF");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

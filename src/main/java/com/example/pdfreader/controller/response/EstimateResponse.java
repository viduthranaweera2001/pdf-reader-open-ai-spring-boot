package com.example.pdfreader.controller.response;

import lombok.Data;

@Data
public class EstimateResponse {
    private String timeEstimate;
    private String costEstimate;
    private String description;
}

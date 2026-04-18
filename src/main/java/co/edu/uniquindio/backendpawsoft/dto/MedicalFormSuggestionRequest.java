package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Data;

@Data
public class MedicalFormSuggestionRequest {
    private String symptoms;
    private String animalType;
    private String age;
    private String weight;
    private String breed;
    private String additionalInfo;
}
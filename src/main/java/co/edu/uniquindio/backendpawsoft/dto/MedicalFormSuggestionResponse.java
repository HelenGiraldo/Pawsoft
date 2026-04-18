package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Data;
import java.util.List;

@Data
public class MedicalFormSuggestionResponse {
    private String suggestedDiagnosis;
    private List<String> differentialDiagnoses;
    private String recommendedTreatment;
    private List<String> medications;
    private List<String> complementaryExams;
    private String prognosis;
    private List<String> ownerRecommendations;
    private boolean success;
    private String errorMessage;
}
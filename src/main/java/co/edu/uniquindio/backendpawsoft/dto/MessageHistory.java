package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Data;

@Data
public class MessageHistory {
    private String role; // "user" o "model"
    private String text;
}

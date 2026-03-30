package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    private String message;
    private List<MessageHistory> history;

}

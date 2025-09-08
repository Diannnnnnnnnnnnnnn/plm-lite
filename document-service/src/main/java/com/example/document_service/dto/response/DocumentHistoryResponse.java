package com.example.document_service.dto.response;

import java.time.LocalDateTime;

public class DocumentHistoryResponse {
    private Long id;
    private String documentId;
    private String action;
    private String user;
    private String oldValue;
    private String newValue;
    private String comment;
    private LocalDateTime timestamp;

    public DocumentHistoryResponse() {}

    public DocumentHistoryResponse(Long id, String documentId, String action, String user,
                                   String oldValue, String newValue, String comment, LocalDateTime timestamp) {
        this.id = id;
        this.documentId = documentId;
        this.action = action;
        this.user = user;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

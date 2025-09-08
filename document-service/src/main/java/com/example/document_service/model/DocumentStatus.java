package com.example.document_service.model;

public class DocumentStatus {
    public static final String DRAFT = "DRAFT";
    public static final String UNDER_REVIEW = "UNDER_REVIEW";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String OBSOLETE = "OBSOLETE";

    private DocumentStatus() {} // Prevent instantiation
}

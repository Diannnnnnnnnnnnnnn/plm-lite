package com.example.plm.common.model;

public enum Status {
    DRAFT,
    IN_WORK,
    IN_REVIEW,
    IN_TECHNICAL_REVIEW,  // Two-stage review: between initial and technical review
    APPROVED,
    RELEASED,
    OBSOLETE
}

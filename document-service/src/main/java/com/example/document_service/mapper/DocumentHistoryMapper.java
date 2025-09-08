package com.example.document_service.mapper;

import com.example.document_service.dto.response.DocumentHistoryResponse;
import com.example.document_service.model.DocumentHistory;

public class DocumentHistoryMapper {

    private DocumentHistoryMapper() {}

    public static DocumentHistoryResponse toResponse(DocumentHistory history) {
        return new DocumentHistoryResponse(
                history.getId(),
                history.getDocumentId(),
                history.getAction(),
                history.getUser(),
                history.getOldValue(),
                history.getNewValue(),
                history.getComment(),
                history.getTimestamp()
        );
    }
}

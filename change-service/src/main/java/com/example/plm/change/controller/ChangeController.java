package com.example.plm.change.controller;

import com.example.plm.change.dto.CreateChangeRequest;
import com.example.plm.change.dto.ChangeResponse;
import com.example.plm.common.model.Status;
import com.example.plm.change.model.ChangeSearchDocument;
import com.example.plm.change.service.ChangeService;
import com.example.plm.change.service.ChangeServiceDev;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/changes")
@CrossOrigin(origins = "*")
public class ChangeController {

    @Autowired(required = false)
    private ChangeService changeService;

    @Autowired(required = false)
    private ChangeServiceDev changeServiceDev;

    @PostMapping
    public ResponseEntity<ChangeResponse> createChange(@Valid @RequestBody CreateChangeRequest request) {
        try {
            ChangeResponse response = changeServiceDev != null ?
                changeServiceDev.createChange(request) :
                changeService.createChange(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{changeId}/submit-review")
    public ResponseEntity<ChangeResponse> submitForReview(@PathVariable String changeId) {
        try {
            ChangeResponse response = changeServiceDev != null ?
                changeServiceDev.submitForReview(changeId) :
                changeService.submitForReview(changeId);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{changeId}/approve")
    public ResponseEntity<ChangeResponse> approveChange(@PathVariable String changeId) {
        try {
            ChangeResponse response = changeServiceDev != null ?
                changeServiceDev.approveChange(changeId) :
                changeService.approveChange(changeId);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{changeId}")
    public ResponseEntity<ChangeResponse> getChangeById(@PathVariable String changeId) {
        Optional<ChangeResponse> change = changeServiceDev != null ?
            changeServiceDev.getChangeById(changeId) :
            changeService.getChangeById(changeId);
        return change.map(ResponseEntity::ok)
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<ChangeResponse>> getAllChanges() {
        List<ChangeResponse> changes = changeServiceDev != null ?
            changeServiceDev.getAllChanges() :
            changeService.getAllChanges();
        return ResponseEntity.ok(changes);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ChangeResponse>> getChangesByStatus(@PathVariable Status status) {
        List<ChangeResponse> changes = changeServiceDev != null ?
            changeServiceDev.getChangesByStatus(status) :
            changeService.getChangesByStatus(status);
        return ResponseEntity.ok(changes);
    }

    @GetMapping("/creator/{creator}")
    public ResponseEntity<List<ChangeResponse>> getChangesByCreator(@PathVariable String creator) {
        List<ChangeResponse> changes = changeServiceDev != null ?
            changeServiceDev.getChangesByCreator(creator) :
            changeService.getChangesByCreator(creator);
        return ResponseEntity.ok(changes);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ChangeResponse>> searchChanges(@RequestParam String keyword) {
        List<ChangeResponse> changes = changeServiceDev != null ?
            changeServiceDev.searchChanges(keyword) :
            changeService.searchChanges(keyword);
        return ResponseEntity.ok(changes);
    }

    @GetMapping("/search/elastic")
    public ResponseEntity<List<ChangeSearchDocument>> searchChangesElastic(@RequestParam String query) {
        List<ChangeSearchDocument> changes = changeServiceDev != null ?
            changeServiceDev.searchChangesElastic(query) :
            changeService.searchChangesElastic(query);
        return ResponseEntity.ok(changes);
    }
}
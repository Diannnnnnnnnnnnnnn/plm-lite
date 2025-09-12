package com.example.bom_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.bom_service.service.impl.BomServiceImpl;

@Component
public class ScheduledTasks {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    
    @Autowired
    private BomServiceImpl bomService;
    
    // Run once a day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void purgeOldDeletedBoms() {
        logger.info("Starting scheduled purge of deleted BOMs");
        // Keep deleted BOMs for 30 days before purging
        bomService.purgeDeletedBoms(30);
        logger.info("Completed purging deleted BOMs");
    }
}

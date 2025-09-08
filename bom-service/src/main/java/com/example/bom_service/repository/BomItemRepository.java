package com.example.bom_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bom_service.model.BomItem;

public interface BomItemRepository extends JpaRepository<BomItem, String> {
}

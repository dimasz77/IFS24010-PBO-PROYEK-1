package org.delcom.todos.services;

import org.delcom.todos.entities.CashFlow;
import org.delcom.todos.repositories.CashFlowRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CashFlowService {

    private final CashFlowRepository cashFlowRepository;

    // Constructor Injection (Wajib untuk lulus Test TA)
    public CashFlowService(CashFlowRepository cashFlowRepository) {
        this.cashFlowRepository = cashFlowRepository;
    }

    // 1. CREATE
    public CashFlow createCashFlow(String type, String source, String label, Integer amount, String description) {
        CashFlow newFlow = new CashFlow(type, source, label, amount, description);
        newFlow.onCreate();
        return cashFlowRepository.save(newFlow);
    }

    // 2. READ ALL / SEARCH
    public List<CashFlow> getAllCashFlows(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return cashFlowRepository.findByKeyword(keyword.trim());
        }
        return cashFlowRepository.findAll();
    }

    // 3. READ BY ID
    public CashFlow getCashFlowById(UUID id) {
        return cashFlowRepository.findById(id).orElse(null);
    }

    // 4. GET LABELS
    public List<String> getCashFlowLabels() {
        return cashFlowRepository.findDistinctLabels();
    }

    // 5. UPDATE
    public CashFlow updateCashFlow(UUID id, String type, String source, String label, Integer amount, String description) {
        Optional<CashFlow> existingFlowOpt = cashFlowRepository.findById(id);

        if (existingFlowOpt.isPresent()) {
            CashFlow existingFlow = existingFlowOpt.get();
            
            existingFlow.setType(type);
            existingFlow.setSource(source);
            existingFlow.setLabel(label);
            existingFlow.setAmount(amount);
            existingFlow.setDescription(description);
            
            existingFlow.onUpdate();
            return cashFlowRepository.save(existingFlow);
        }
        return null;
    }

    // 6. DELETE
    public boolean deleteCashFlow(UUID id) {
        if (cashFlowRepository.existsById(id)) {
            cashFlowRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
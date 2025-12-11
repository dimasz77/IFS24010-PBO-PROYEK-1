package org.delcom.todos.controllers;

import org.delcom.todos.configs.ApiResponse; // Pastikan ini sudah ada dari modul bawaan
import org.delcom.todos.entities.CashFlow;
import org.delcom.todos.services.CashFlowService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cash-flows")
public class CashFlowController {

    private final CashFlowService cashFlowService;

    // Constructor Injection (Wajib untuk lulus Test TA)
    public CashFlowController(CashFlowService cashFlowService) {
        this.cashFlowService = cashFlowService;
    }

    // Helper untuk validasi (sesuai Test Controller TA)
    private boolean isValidCashFlow(CashFlow flow) {
        return flow.getType() != null && !flow.getType().trim().isEmpty() &&
               flow.getSource() != null && !flow.getSource().trim().isEmpty() &&
               flow.getLabel() != null && !flow.getLabel().trim().isEmpty() &&
               flow.getAmount() != null && flow.getAmount() > 0 &&
               flow.getDescription() != null && !flow.getDescription().trim().isEmpty();
    }

    // 1. POST /cash-flows : CREATE
    @PostMapping
    public ApiResponse<Map<String, UUID>> createCashFlow(@RequestBody CashFlow cashFlow) {
        if (!isValidCashFlow(cashFlow)) {
            return new ApiResponse<>("fail", "Data Cash Flow tidak valid.", null);
        }

        CashFlow created = cashFlowService.createCashFlow(
            cashFlow.getType(), 
            cashFlow.getSource(), 
            cashFlow.getLabel(), 
            cashFlow.getAmount(), 
            cashFlow.getDescription()
        );

        Map<String, UUID> data = new HashMap<>();
        data.put("id", created.getId());
        
        return new ApiResponse<>("success", "Berhasil menambahkan transaksi cash flow.", data);
    }

    // 2. GET /cash-flows : READ ALL / SEARCH
    @GetMapping
    public ApiResponse<Map<String, List<CashFlow>>> getAllCashFlows(@RequestParam(required = false) String keyword) {
        List<CashFlow> cashFlows = cashFlowService.getAllCashFlows(keyword);
        
        Map<String, List<CashFlow>> data = new HashMap<>();
        data.put("cashFLows", cashFlows); 
        
        return new ApiResponse<>("success", "Berhasil mengambil semua data transaksi cash flow.", data);
    }

    // 3. GET /cash-flows/{id} : READ BY ID
    @GetMapping("/{id}")
    public ApiResponse<Map<String, CashFlow>> getCashFlowById(@PathVariable UUID id) {
        CashFlow cashFlow = cashFlowService.getCashFlowById(id);

        if (cashFlow != null) {
            Map<String, CashFlow> data = new HashMap<>();
            data.put("cashFlow", cashFlow);
            return new ApiResponse<>("success", "Berhasil mengambil data transaksi.", data);
        } else {
            return new ApiResponse<>("fail", "Transaksi dengan ID " + id + " tidak ditemukan.", null);
        }
    }
    
    // 4. PUT /cash-flows/{id} : UPDATE
    @PutMapping("/{id}")
    public ApiResponse<CashFlow> updateCashFlow(@PathVariable UUID id, @RequestBody CashFlow cashFlowDetails) {
        if (!isValidCashFlow(cashFlowDetails)) {
            return new ApiResponse<>("fail", "Data Cash Flow tidak valid.", null);
        }

        CashFlow updatedCashFlow = cashFlowService.updateCashFlow(
            id, 
            cashFlowDetails.getType(), 
            cashFlowDetails.getSource(), 
            cashFlowDetails.getLabel(), 
            cashFlowDetails.getAmount(), 
            cashFlowDetails.getDescription()
        );
        
        if (updatedCashFlow != null) {
            return new ApiResponse<>("success", "Berhasil memperbarui transaksi cash flow.", updatedCashFlow);
        } else {
            return new ApiResponse<>("fail", "Transaksi dengan ID " + id + " tidak ditemukan.", null);
        }
    }

    // 5. DELETE /cash-flows/{id} : DELETE
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteCashFlow(@PathVariable UUID id) {
        boolean deleted = cashFlowService.deleteCashFlow(id);
        
        if (deleted) {
            return new ApiResponse<>("success", "Berhasil menghapus transaksi dengan ID " + id + ".", null);
        } else {
            return new ApiResponse<>("fail", "Gagal menghapus transaksi: ID " + id + " tidak ditemukan.", null);
        }
    }

    // 6. GET /cash-flows/labels : GET LABELS
    @GetMapping("/labels")
    public ApiResponse<Map<String, List<String>>> getCashFlowLabels() {
        List<String> labels = cashFlowService.getCashFlowLabels();
        
        Map<String, List<String>> data = new HashMap<>();
        data.put("labels", labels);
        
        return new ApiResponse<>("success", "Berhasil mengambil data label.", data);
    }
}
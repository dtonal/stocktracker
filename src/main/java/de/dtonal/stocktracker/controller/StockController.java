package de.dtonal.stocktracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.dtonal.stocktracker.dto.StockSearchResult;
import de.dtonal.stocktracker.service.StockService;

@RestController
@RequestMapping("/api/stocks")  
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/search")
    public ResponseEntity<StockSearchResult> searchStocks(@RequestParam String query) {
        StockSearchResult result = stockService.searchStocks(query);
        return ResponseEntity.ok(result);
    }
}

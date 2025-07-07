package de.dtonal.stocktracker.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.dtonal.stocktracker.dto.PortfolioCreateRequest;
import de.dtonal.stocktracker.dto.PortfolioResponse;
import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.dto.StockTransactionResponse;
import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.service.PortfolioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PortfolioResponse> createPortfolio(@Valid @RequestBody PortfolioCreateRequest request) {
        Portfolio portfolio = portfolioService.createPortfolio(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PortfolioResponse(portfolio));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PortfolioResponse>> getPortfoliosForCurrentUser() {
        List<PortfolioResponse> portfolios = portfolioService.findPortfoliosForCurrentUser().stream()
                .map(PortfolioResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(portfolios);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PortfolioResponse> getPortfolioById(@PathVariable String id) {
        return portfolioService.findById(id)
                .map(portfolio -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new PortfolioResponse(portfolio)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Portfolio not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable String id) {
        portfolioService.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{portfolioId}/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StockTransactionResponse> addTransaction(
            @PathVariable String portfolioId,
            @Valid @RequestBody StockTransactionRequest request) {
        StockTransaction transaction = portfolioService.addStockTransaction(portfolioId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new StockTransactionResponse(transaction));
    }

    @GetMapping("/{portfolioId}/stocks/{stockSymbol}/quantity")
    public ResponseEntity<BigDecimal> getStockQuantity(
            @PathVariable String portfolioId,
            @PathVariable String stockSymbol) {
        BigDecimal quantity = portfolioService.getStockQuantity(portfolioId, stockSymbol);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(quantity);
    }
}
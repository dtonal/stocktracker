package de.dtonal.stocktracker.controller;

import de.dtonal.stocktracker.dto.*;
import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.service.PortfolioService;
import de.dtonal.stocktracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserService userService;

    @Autowired
    public PortfolioController(PortfolioService portfolioService, UserService userService) {
        this.portfolioService = portfolioService;
        this.userService = userService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PortfolioResponse> createPortfolio(@Valid @RequestBody PortfolioCreateRequest request,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Portfolio portfolio = portfolioService.createPortfolio(request.getName(), request.getDescription(), currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PortfolioResponse(portfolio));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PortfolioResponse>> getPortfoliosForCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        List<PortfolioResponse> portfolios = portfolioService.findByUser(currentUser).stream()
                .map(PortfolioResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(portfolios);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PortfolioResponse> getPortfolioById(@PathVariable Long id) {
        return portfolioService.findById(id)
                .map(portfolio -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new PortfolioResponse(portfolio)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
    }

    @PostMapping(value = "/{portfolioId}/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StockTransactionResponse> addTransaction(
            @PathVariable Long portfolioId,
            @Valid @RequestBody StockTransactionRequest request) {
        StockTransaction transaction = portfolioService.addTransaction(portfolioId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new StockTransactionResponse(transaction));
    }

    @GetMapping("/{portfolioId}/stocks/{stockSymbol}/quantity")
    public ResponseEntity<BigDecimal> getStockQuantity(
            @PathVariable Long portfolioId,
            @PathVariable String stockSymbol) {
        BigDecimal quantity = portfolioService.getStockQuantity(portfolioId, stockSymbol);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(quantity);
    }
} 
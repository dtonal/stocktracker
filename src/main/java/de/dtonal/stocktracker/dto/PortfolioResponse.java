package de.dtonal.stocktracker.dto;

import de.dtonal.stocktracker.model.Portfolio;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {
    private String id;
    private String name;
    private String description;
    private String userId;
    private List<StockTransactionResponse> transactions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PortfolioResponse(Portfolio portfolio) {
        this.id = portfolio.getId();
        this.name = portfolio.getName();
        this.description = portfolio.getDescription();
        this.userId = portfolio.getUser().getId();
        this.transactions = portfolio.getTransactions().stream()
                .map(StockTransactionResponse::new)
                .collect(Collectors.toList());
        this.createdAt = portfolio.getCreatedAt();
        this.updatedAt = portfolio.getUpdatedAt();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
} 
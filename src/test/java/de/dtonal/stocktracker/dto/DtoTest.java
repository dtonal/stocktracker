package de.dtonal.stocktracker.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import de.dtonal.stocktracker.model.Stock;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // --- PortfolioCreateRequest Tests ---

    @Test
    void portfolioCreateRequest_shouldBeValid() {
        PortfolioCreateRequest request = new PortfolioCreateRequest("Valid Name", "Valid Description");
        Set<ConstraintViolation<PortfolioCreateRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void portfolioCreateRequest_shouldFail_whenNameIsBlank() {
        PortfolioCreateRequest request = new PortfolioCreateRequest("", "Description");
        Set<ConstraintViolation<PortfolioCreateRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(2);

        Set<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(messages).contains("Portfolio name cannot be blank.", "Portfolio name must be between 3 and 100 characters.");
    }
    
    @Test
    void portfolioCreateRequest_shouldFail_whenNameIsTooShort() {
        PortfolioCreateRequest request = new PortfolioCreateRequest("A", "Description");
        Set<ConstraintViolation<PortfolioCreateRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Portfolio name must be between 3 and 100 characters.");
    }

    @Test
    void portfolioCreateRequest_shouldFail_whenDescriptionIsTooLong() {
        String longDescription = "a".repeat(256);
        PortfolioCreateRequest request = new PortfolioCreateRequest("Valid Name", longDescription);
        Set<ConstraintViolation<PortfolioCreateRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Description cannot be longer than 255 characters.");
    }

    @Test
    void testPortfolioCreateRequestEqualsAndHashCode() {
        PortfolioCreateRequest request1 = new PortfolioCreateRequest("Test", "Desc");
        PortfolioCreateRequest request2 = new PortfolioCreateRequest("Test", "Desc");
        PortfolioCreateRequest request3 = new PortfolioCreateRequest("Test2", "Desc");

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isNotEqualTo(request3.hashCode());
    }

    // --- StockResponse Tests ---

    @Test
    void stockResponse_shouldMapFromStockEntityCorrectly() {
        Stock stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stock.setId("stock-id");
        // Manually set timestamps as they are normally set by @PrePersist
        stock.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0));
        stock.setUpdatedAt(LocalDateTime.of(2023, 1, 2, 11, 0));

        StockResponse response = new StockResponse(stock);

        assertThat(response.getId()).isEqualTo("stock-id");
        assertThat(response.getSymbol()).isEqualTo("AAPL");
        assertThat(response.getName()).isEqualTo("Apple Inc.");
        assertThat(response.getExchange()).isEqualTo("NASDAQ");
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2023, 1, 1, 10, 0));
        assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.of(2023, 1, 2, 11, 0));
    }
} 
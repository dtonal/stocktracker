package de.dtonal.stocktracker.validation;

import de.dtonal.stocktracker.dto.StockTransactionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RequireStockIdentifierValidator implements ConstraintValidator<RequireStockIdentifier, StockTransactionRequest> {

    @Override
    public void initialize(RequireStockIdentifier constraintAnnotation) {
    }

    @Override
    public boolean isValid(StockTransactionRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean stockIdPresent = request.getStockId() != null && !request.getStockId().trim().isEmpty();
        boolean stockSymbolPresent = request.getStockSymbol() != null && !request.getStockSymbol().trim().isEmpty();

        return stockIdPresent || stockSymbolPresent;
    }
}
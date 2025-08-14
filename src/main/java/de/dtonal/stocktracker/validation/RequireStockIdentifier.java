package de.dtonal.stocktracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = RequireStockIdentifierValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireStockIdentifier {
    String message() default "Either stockId or stockSymbol must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
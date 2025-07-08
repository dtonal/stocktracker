package de.dtonal.stocktracker.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import de.dtonal.stocktracker.dto.PriceData;

@SpringBootTest
@Tag("finnhub_integration")
@EnabledIfEnvironmentVariable(named = "FINNHUB_API_KEY", matches = ".+")
@TestPropertySource(properties = {
    "finnhub.api.url=https://finnhub.io/api/v1",
    "finnhub.api.key=${FINNHUB_API_KEY}"
})
public class FinnhubStockDataServiceTest {

    @Autowired
    private StockDataService stockDataService;

    @Test
    void getLatestPriceData_shouldReturnPriceData_forValidSymbol() {
        // Arrange
        String symbol = "AAPL"; // Using a well-known symbol

        // Act
        Optional<PriceData> result = stockDataService.getLatestPriceData(symbol);

        // Assert
        assertThat(result).isPresent();
         PriceData priceData = result.get();
         System.out.println(priceData);
       assertThat(priceData.getCurrentPrice()).isNotNull();
         assertThat(priceData.getCurrentPrice().doubleValue()).isGreaterThan(0);
       assertThat(priceData.getPreviousClosePrice()).isNotNull();
   }
}
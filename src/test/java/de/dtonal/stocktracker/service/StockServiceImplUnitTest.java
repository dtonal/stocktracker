package de.dtonal.stocktracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.dtonal.stocktracker.dto.CompanyProfile;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.repository.StockRepository;

@ExtendWith(MockitoExtension.class)
class StockServiceImplUnitTest {

	@Mock
	private StockDataService stockDataService;

	@Mock
	private StockRepository stockRepository;

	@Mock
	private StockPriceUpdateService stockPriceUpdateService;

	@InjectMocks
	private StockServiceImpl stockService;

	@Test
	void getOrCreateStock_shouldReturnExisting_whenSymbolExists() {
		// Arrange
		Stock existing = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
		existing.setId("s1");
		when(stockRepository.findBySymbol("AAPL")).thenReturn(java.util.List.of(existing));

		// Act
		Stock result = stockService.getOrCreateStock("AAPL");

		// Assert
		assertThat(result).isSameAs(existing);
		verify(stockRepository, never()).save(any());
		verifyNoInteractions(stockDataService);
		verify(stockPriceUpdateService, never()).updateStockPrice(any());
	}

	@Test
	void getOrCreateStock_shouldCreateNew_whenNotExists_andProfilePresent() {
		// Arrange
		when(stockRepository.findBySymbol("MSFT")).thenReturn(Collections.emptyList());

		CompanyProfile profile = new CompanyProfile();
		profile.setName("Microsoft Corporation");
		profile.setCountry("US");
		profile.setExchange("US");
		profile.setCurrency("USD");
		when(stockDataService.getStockProfile("MSFT")).thenReturn(Optional.of(profile));

		Stock persisted = new Stock("MSFT", "Microsoft Corporation", "US", "USD");
		persisted.setId("msft-id");
		when(stockRepository.save(any(Stock.class))).thenReturn(persisted);

		// Act
		Stock result = stockService.getOrCreateStock("MSFT");

        // Assert saved entity fields
		ArgumentCaptor<Stock> toSaveCaptor = ArgumentCaptor.forClass(Stock.class);
		verify(stockRepository).save(toSaveCaptor.capture());
		Stock toSave = toSaveCaptor.getValue();
		assertThat(toSave.getSymbol()).isEqualTo("MSFT");
		assertThat(toSave.getName()).isEqualTo("Microsoft Corporation");
        assertThat(toSave.getExchange()).isEqualTo("US");
		assertThat(toSave.getCurrency()).isEqualTo("USD");

		// Assert returned and side effects
		assertThat(result).isSameAs(persisted);
		verify(stockPriceUpdateService).updateStockPrice(persisted);
	}

	@Test
	void getOrCreateStock_shouldThrow_whenProfileMissing() {
		// Arrange
		when(stockRepository.findBySymbol("ZZZZ")).thenReturn(Collections.emptyList());
		when(stockDataService.getStockProfile("ZZZZ")).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> stockService.getOrCreateStock("ZZZZ"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Stock with symbol ZZZZ not found.");
		verify(stockRepository, never()).save(any());
		verify(stockPriceUpdateService, never()).updateStockPrice(any());
	}
}



package de.dtonal.stocktracker.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSearchItem {
    private String description;
    private String displaySymbol;
    private String symbol;
    private String type;
    private boolean isSavedInDb;

    public StockSearchItem(String description, String displaySymbol, String symbol, String type) {
        this.description = description;
        this.displaySymbol = displaySymbol;
        this.symbol = symbol;
        this.type = type;
        this.isSavedInDb = false;
    }



}

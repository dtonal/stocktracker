package de.dtonal.stocktracker.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinnhubSearchItem {
    private String description;
    private String displaySymbol;
    private String symbol;
    private String type;
}

package de.dtonal.stocktracker.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSearchResult {
    private int count;
    private List<StockSearchItem> result;
}

package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class StockLocationAttrsServiceImpl implements StockLocationAttrsService {

  protected final StockLocationRepository stockLocationRepository;
  protected static final int MAX_DEPTH = 100;

  @Inject
  public StockLocationAttrsServiceImpl(StockLocationRepository stockLocationRepository) {
    this.stockLocationRepository = stockLocationRepository;
  }

  @Override
  public String getParentStockLocationDomain(StockLocation stockLocation) {
    Objects.requireNonNull(stockLocation);

    StringJoiner domain = new StringJoiner(" AND ");
    if (stockLocation.getCompany() != null) {
      domain.add(String.format("self.company.id = %s", stockLocation.getCompany().getId()));
    }
    if (stockLocation.getSite() != null) {
      domain.add(String.format("self.site.id = %s", stockLocation.getSite().getId()));
    }
    if (stockLocation.getId() != null) {
      domain.add(String.format("self.id != %s", stockLocation.getId()));
      addSubStockLocationsInDomain(domain, stockLocation, 0);
    }

    return domain.toString();
  }

  protected void addSubStockLocationsInDomain(
      StringJoiner domain, StockLocation stockLocation, int depth) {

    if (depth >= MAX_DEPTH) {
      return;
    }

    List<StockLocation> subStockLocations =
        stockLocationRepository
            .all()
            .filter("self.parentStockLocation = :stockLocation")
            .bind("stockLocation", stockLocation)
            .fetch();

    subStockLocations.stream()
        .forEach(
            subStockLocation -> {
              domain.add(String.format("self.id != %s", subStockLocation.getId()));
              addSubStockLocationsInDomain(domain, subStockLocation, depth + 1);
            });
  }
}

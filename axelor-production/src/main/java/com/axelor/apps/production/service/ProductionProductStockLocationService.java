package com.axelor.apps.production.service;

public interface ProductionProductStockLocationService {

  /**
   * Create a query to find product's consume and missing qty of a specific/all company and a
   * specific/all stock location in a Manuf Order
   *
   * @param productId, companyId and stockLocationId
   * @return the query.
   */
  String getConsumeAndMissingQtyForAProduct(Long productId, Long companyId, Long stockLocationId);

  /**
   * Create a query to find product's building qty of a specific/all company and a specific/all
   * stock location in a Manuf Order
   *
   * @param productId, companyId and stockLocationId
   * @return the query.
   */
  String getBuildingQtyForAProduct(Long productId, Long companyId, Long stockLocationId);
}

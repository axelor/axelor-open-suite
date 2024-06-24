package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import java.util.Objects;

public class PickedProductAttrsServiceImpl implements PickedProductAttrsService {
  @Override
  public String getStockLocationDomain(PickedProduct pickedProduct, MassStockMove massStockMove) {
    Objects.requireNonNull(pickedProduct);
    Objects.requireNonNull(massStockMove);

    if (massStockMove.getCompany() != null) {
      return String.format(
          "self.company.id = %d and self.typeSelect != %d",
          massStockMove.getCompany().getId(), StockLocationRepository.TYPE_VIRTUAL);
    }

    return String.format("self.typeSelect != %d", StockLocationRepository.TYPE_VIRTUAL);
  }
}

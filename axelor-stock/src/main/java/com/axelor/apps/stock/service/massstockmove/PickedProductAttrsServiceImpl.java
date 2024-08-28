package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.PickedProduct;
import java.util.Objects;

public class PickedProductAttrsServiceImpl implements PickedProductAttrsService {
  @Override
  public String getPickedProductDomain(PickedProduct pickedProduct) {
    Objects.requireNonNull(pickedProduct);

    if (pickedProduct.getFromStockLocation() != null) {
      String domain =
          "(self IN (SELECT stockLocationLine.product FROM StockLocationLine stockLocationLine WHERE stockLocationLine.currentQty > 0 AND stockLocationLine.stockLocation = %d))";

      return String.format(domain, pickedProduct.getFromStockLocation().getId());
    }
    return "self.id IN (0)";
  }
}

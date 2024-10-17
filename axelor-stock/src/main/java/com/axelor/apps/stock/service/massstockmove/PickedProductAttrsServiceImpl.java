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

  @Override
  public String getTrackingNumberDomain(PickedProduct pickedProduct) {

    Objects.requireNonNull(pickedProduct);

    if (pickedProduct.getPickedProduct() != null && pickedProduct.getFromStockLocation() != null) {
      String domain =
          "self.product.id = %d AND"
              + " (self IN (SELECT stockLocationLine.trackingNumber FROM StockLocationLine stockLocationLine WHERE stockLocationLine.detailsStockLocation = %d AND stockLocationLine.currentQty > 0))";

      return String.format(
          domain,
          pickedProduct.getPickedProduct().getId(),
          pickedProduct.getFromStockLocation().getId());
    }

    // Must not be able to select
    return "self.id IN (0)";
  }
}

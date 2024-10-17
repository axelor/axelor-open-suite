package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.StoredProduct;
import java.util.Objects;

public class StoredProductAttrsServiceImpl implements StoredProductAttrsService {
  @Override
  public String getStoredProductDomain(MassStockMove massStockMove) {

    Objects.requireNonNull(massStockMove);

    if (massStockMove.getCartStockLocation() != null) {
      String domain =
          "(self IN (SELECT stockLocationLine.product FROM StockLocationLine stockLocationLine WHERE stockLocationLine.currentQty > 0 AND stockLocationLine.stockLocation = %d))";

      return String.format(domain, massStockMove.getCartStockLocation().getId());
    }

    // Must not be able to select
    return "self.id IN (0)";
  }

  @Override
  public String getTrackingNumberDomain(StoredProduct storedProduct, MassStockMove massStockMove) {

    Objects.requireNonNull(massStockMove);
    Objects.requireNonNull(storedProduct);

    if (storedProduct.getStoredProduct() != null && massStockMove.getCartStockLocation() != null) {
      String domain =
          "self.product.id = %d AND"
              + " (self IN (SELECT stockLocationLine.trackingNumber FROM StockLocationLine stockLocationLine WHERE stockLocationLine.detailsStockLocation = %d AND stockLocationLine.currentQty > 0))";

      return String.format(
          domain,
          storedProduct.getStoredProduct().getId(),
          massStockMove.getCartStockLocation().getId());
    }

    // Must not be able to select
    return "self.id IN (0)";
  }
}

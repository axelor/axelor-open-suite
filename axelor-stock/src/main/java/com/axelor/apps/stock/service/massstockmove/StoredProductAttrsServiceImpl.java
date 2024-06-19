package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.utils.helpers.StringHelper;
import java.util.Objects;
import java.util.stream.Collectors;

public class StoredProductAttrsServiceImpl implements StoredProductAttrsService {
  @Override
  public String getStoredProductDomain(MassStockMove massStockMove) {

    Objects.requireNonNull(massStockMove);

    return String.format(
        "self.id IN ( %s )",
        StringHelper.getIdListString(
            massStockMove.getPickedProductList().stream()
                .map(PickedProduct::getPickedProduct)
                .collect(Collectors.toList())));
  }

  @Override
  public String getTrackingNumberDomain(StoredProduct storedProduct, MassStockMove massStockMove) {

    Objects.requireNonNull(massStockMove);

    if (storedProduct == null) {
      return String.format(
          "self.id IN ( %s )",
          StringHelper.getIdListString(
              massStockMove.getPickedProductList().stream()
                  .map(PickedProduct::getTrackingNumber)
                  .collect(Collectors.toList())));
    }

    return String.format(
        "self.id IN ( %s )",
        StringHelper.getIdListString(
            massStockMove.getPickedProductList().stream()
                .filter(
                    pickedProduct ->
                        pickedProduct.getPickedProduct().equals(storedProduct.getStoredProduct()))
                .map(PickedProduct::getTrackingNumber)
                .collect(Collectors.toList())));
  }
}

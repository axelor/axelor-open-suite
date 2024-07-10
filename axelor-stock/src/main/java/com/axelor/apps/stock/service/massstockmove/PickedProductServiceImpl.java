package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StoredProduct;
import java.math.BigDecimal;

public class PickedProductServiceImpl implements PickedProductService {

  @Override
  public StoredProduct createFromPickedProduct(PickedProduct pickedProduct) {

    var storedProduct = new StoredProduct();

    storedProduct.setMassStockMove(pickedProduct.getMassStockMove());
    storedProduct.setStoredProduct(pickedProduct.getPickedProduct());
    storedProduct.setStoredQty(BigDecimal.ZERO);
    storedProduct.setTrackingNumber(pickedProduct.getTrackingNumber());
    storedProduct.setUnit(pickedProduct.getUnit());
    storedProduct.setCurrentQty(BigDecimal.ZERO);
    pickedProduct.addStoredProductListItem(storedProduct);

    return storedProduct;
  }
}

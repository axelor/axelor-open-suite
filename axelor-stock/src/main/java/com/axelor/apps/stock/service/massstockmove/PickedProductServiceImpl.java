package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StoredProduct;
import java.math.BigDecimal;

public class PickedProductServiceImpl implements PickedProductService {

  @Override
  public StoredProduct createFromPickedProduct(PickedProduct pickedProduct) throws AxelorException {

    var storedProduct = new StoredProduct();

    storedProduct.setMassStockMove(pickedProduct.getMassStockMove());
    storedProduct.setStoredProduct(pickedProduct.getPickedProduct());
    storedProduct.setStoredQty(BigDecimal.ZERO);
    storedProduct.setTrackingNumber(pickedProduct.getTrackingNumber());
    storedProduct.setUnit(pickedProduct.getUnit());
    pickedProduct.addStoredProductListItem(storedProduct);
    storedProduct.setToStockLocation(storedProduct.getMassStockMove().getCommonToStockLocation());

    return storedProduct;
  }

  @Override
  public PickedProduct createPickedProduct(
      MassStockMove massStockMove, Product product, StockLocation stockLocation, BigDecimal qty) {

    var pickedProduct = new PickedProduct();
    massStockMove.addPickedProductListItem(pickedProduct);
    pickedProduct.setPickedProduct(product);
    pickedProduct.setFromStockLocation(stockLocation);
    pickedProduct.setPickedQty(qty);
    pickedProduct.setUnit(product.getUnit());

    return pickedProduct;
  }
}

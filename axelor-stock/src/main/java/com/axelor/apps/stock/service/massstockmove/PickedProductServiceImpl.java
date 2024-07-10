package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StoredProduct;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class PickedProductServiceImpl implements PickedProductService {

  protected final MassStockMovableProductQuantityService massStockMovableProductQuantityService;

  @Inject
  public PickedProductServiceImpl(
      MassStockMovableProductQuantityService massStockMovableProductQuantityService) {
    this.massStockMovableProductQuantityService = massStockMovableProductQuantityService;
  }

  @Override
  public StoredProduct createFromPickedProduct(PickedProduct pickedProduct) throws AxelorException {

    var storedProduct = new StoredProduct();

    storedProduct.setMassStockMove(pickedProduct.getMassStockMove());
    storedProduct.setStoredProduct(pickedProduct.getPickedProduct());
    storedProduct.setStoredQty(BigDecimal.ZERO);
    storedProduct.setTrackingNumber(pickedProduct.getTrackingNumber());
    storedProduct.setUnit(pickedProduct.getUnit());
    storedProduct.setCurrentQty(BigDecimal.ZERO);
    storedProduct.setCurrentQty(
        massStockMovableProductQuantityService.getCurrentAvailableQty(
            storedProduct, storedProduct.getMassStockMove().getCartStockLocation()));
    pickedProduct.addStoredProductListItem(storedProduct);

    return storedProduct;
  }
}

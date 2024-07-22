package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;
import java.util.List;

public interface MassStockMovableProductService {

  void realize(List<? extends MassStockMovableProduct> massStockMovableProducts)
      throws AxelorException;

  void realize(MassStockMovableProduct movableProduct) throws AxelorException;

  void cancel(List<? extends MassStockMovableProduct> massStockMovableProducts)
      throws AxelorException;

  void cancel(MassStockMovableProduct movableProduct) throws AxelorException;
}

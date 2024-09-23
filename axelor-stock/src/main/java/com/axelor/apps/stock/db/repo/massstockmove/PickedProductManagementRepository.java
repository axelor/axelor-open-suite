package com.axelor.apps.stock.db.repo.massstockmove;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductQuantityService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;

public class PickedProductManagementRepository extends PickedProductRepository {

  protected final MassStockMovableProductQuantityService massStockMovableProductQuantityService;
  protected final AppBaseService appBaseService;

  @Inject
  public PickedProductManagementRepository(
      MassStockMovableProductQuantityService massStockMovableProductQuantityService,
      AppBaseService appBaseService) {
    this.massStockMovableProductQuantityService = massStockMovableProductQuantityService;
    this.appBaseService = appBaseService;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    if (context.get("_model") != null
        && context.get("_model").toString().equals(PickedProduct.class.getName())
        && json.get("id") != null) {

      var id = (Long) json.get("id");
      var pickedProduct = find(id);
      var sourceStockLocation = pickedProduct.getFromStockLocation();

      try {
        json.put(
            "currentQty",
            massStockMovableProductQuantityService.getCurrentAvailableQty(
                pickedProduct, sourceStockLocation));
        json.put("nbDecimalQty", appBaseService.getNbDecimalDigitForQty());
      } catch (Exception e) {
        json.put("currentQty", BigDecimal.ZERO);
        TraceBackService.trace(e);
      }
    } else {
      json.put("currentQty", BigDecimal.ZERO);
    }
    return super.populate(json, context);
  }
}

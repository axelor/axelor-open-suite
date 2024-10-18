package com.axelor.apps.stock.db.repo.massstockmove;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductQuantityService;
import com.axelor.apps.stock.service.massstockmove.StoredProductLocationServiceImpl;
import com.axelor.apps.stock.translation.ITranslation;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;

public class StoredProductManagementRepository extends StoredProductRepository {

  protected final MassStockMovableProductQuantityService massStockMovableProductQuantityService;
  protected final StoredProductLocationServiceImpl storedProductLocationService;
  protected final AppBaseService appBaseService;

  @Inject
  public StoredProductManagementRepository(
      MassStockMovableProductQuantityService massStockMovableProductQuantityService,
      StoredProductLocationServiceImpl storedProductLocationService,
      AppBaseService appBaseService) {
    this.massStockMovableProductQuantityService = massStockMovableProductQuantityService;
    this.storedProductLocationService = storedProductLocationService;
    this.appBaseService = appBaseService;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    if (context.get("_model") != null
        && context.get("_model").toString().equals(StoredProduct.class.getName())
        && json.get("id") != null) {

      var id = (Long) json.get("id");
      var storedProduct = find(id);
      var sourceStockLocation = storedProductLocationService.getFromStockLocation(storedProduct);

      try {
        json.put(
            "currentQty",
            massStockMovableProductQuantityService.getCurrentAvailableQty(
                storedProduct, sourceStockLocation));
        json.put("nbDecimalQty", appBaseService.getNbDecimalDigitForQty());
        json.put(
            "state",
            storedProduct.getStockMoveLine() != null
                ? I18n.get(ITranslation.MASS_STOCK_MOVE_NEED_STORED)
                : I18n.get(ITranslation.MASS_STOCK_MOVE_NEED_TO_STORE));
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

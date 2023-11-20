package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.service.ProductReservationService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.Map;

public class ProductReservationRepository extends AbstractProductReservationRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (json != null && json.get("id") != null) {
      Long id = (Long) json.get("id");
      ProductReservation productReservation = find(id);
      BigDecimal availableQty = null;
      try {
        availableQty =
            Beans.get(ProductReservationService.class).getAvailableQty(productReservation);
      } catch (AxelorException e) {
        throw new RuntimeException(e);
      }
      json.put("$availableQty", availableQty);// specifications says  not a field, but computed on load
    }
    return super.populate(json, context);
  }
}

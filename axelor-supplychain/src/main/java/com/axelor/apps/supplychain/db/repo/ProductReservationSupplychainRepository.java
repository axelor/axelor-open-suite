package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.service.ProductReservationService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.Map;
import javax.persistence.PersistenceException;

public class ProductReservationSupplychainRepository extends ProductReservationRepository {

  @Override
  public ProductReservation save(ProductReservation entity) {
    try {
      if (entity.getStatus() == null || entity.getStatus() == 0) {
        entity = Beans.get(ProductReservationService.class).updateStatus(entity);
      }
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e);
    }
    return super.save(entity);
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (json != null && json.get("id") != null) {
      Long id = (Long) json.get("id");
      ProductReservation productReservation = find(id);
      try {
        if (context.get("_field") != null
            && context.get("_field").equals("productReservationList")) {

          BigDecimal availableQty =
              Beans.get(ProductReservationService.class).getAvailableQty(productReservation);
          json.put("$availableQty", availableQty);
        }
      } catch (AxelorException e) {
        throw new RuntimeException(e);
      }
    }
    return super.populate(json, context);
  }
}

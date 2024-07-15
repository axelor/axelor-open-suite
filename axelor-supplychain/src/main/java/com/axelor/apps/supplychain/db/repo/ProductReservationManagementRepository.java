package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import javax.persistence.PersistenceException;

public class ProductReservationManagementRepository extends ProductReservationRepository {

  @Override
  public ProductReservation save(ProductReservation entity) {
    try {
      if (entity.getStatus() == 0 && entity.getTypeSelect() == 2) {
        boolean isTracked = (entity.getProduct().getTrackingNumberConfiguration() != null);
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                (isTracked
                    ? SupplychainExceptionMessage.ALLOCATION_QTY_BY_TRACKING_NUMBER_IS_NOT_AVAILABLE
                    : SupplychainExceptionMessage.ALLOCATION_QTY_BY_PRODUCT_IS_NOT_AVAILABLE)),
            entity);
      } else if (entity.getStatus() == 0 && entity.getTypeSelect() == 1) {
        if (entity.getReservationTypeSelect().equals("3")) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(SupplychainExceptionMessage.QTY_TO_CONSUME_IS_NOT_AVAILABLE),
              entity);
        }
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.RESERVED_QTY_IS_NOT_AVAILABLE),
            entity);
      }
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
    return super.save(entity);
  }
}

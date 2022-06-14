package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class InventoryProductServiceImpl implements InventoryProductService {

  @Override
  public void checkDuplicate(Inventory inventory) throws AxelorException {
    Query query =
        JPA.em()
            .createQuery(
                "select COUNT(*) FROM InventoryLine self WHERE self.inventory.id = :invent GROUP BY self.product, self.trackingNumber HAVING COUNT(self) > 1");

    Long duplicateCounter = Long.valueOf(0);
    try {
      duplicateCounter = (Long) query.setParameter("invent", inventory.getId()).getSingleResult();
    } catch (NoResultException e) {
      // if control came here means no duplicate product.
    }
    if (duplicateCounter > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INVENTORY_PRODUCT_TRACKING_NUMBER_ERROR));
    }
  }
}

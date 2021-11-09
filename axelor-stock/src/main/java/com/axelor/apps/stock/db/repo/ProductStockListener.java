package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import javax.persistence.PersistenceException;
import javax.persistence.PostLoad;
import javax.persistence.PreUpdate;
import org.apache.commons.lang3.SerializationUtils;

public class ProductStockListener {

  @PostLoad
  private void saveState(Product product) {
    product.setOldCode(SerializationUtils.clone(product.getCode()));
  }

  @PreUpdate
  public void onPreUpdate(Product product) {
    if (!product.getOldCode().equals(product.getCode())) {
      long stockMoveLineLinkedCount =
          JPA.all(StockMoveLine.class)
              .autoFlush(false)
              .filter("self.product = :product")
              .bind("product", product.getId())
              .count();
      if (stockMoveLineLinkedCount > 0) {
        try {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.PRODUCT_CODE_CAN_NOT_BE_CHANGED),
              null);
        } catch (AxelorException e) {
          throw new PersistenceException(e);
        }
      }
    }
  }
}

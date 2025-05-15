package com.axelor.apps.sale.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import javax.persistence.PreRemove;

public class SaleOrderLineListener {

  @PreRemove
  public void preRemove(SaleOrderLine saleOrderLine) throws AxelorException {
    if (saleOrderLine.getOrderedQty().compareTo(BigDecimal.ZERO) > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_QUOTATION_DELETE_LINE_WITH_ORDERED_QTY_ERROR));
    }
  }
}

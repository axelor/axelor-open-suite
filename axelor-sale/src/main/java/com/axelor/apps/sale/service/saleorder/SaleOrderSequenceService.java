package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderSequenceService {

  String getQuotationSequence(SaleOrder saleOrder) throws AxelorException;
}

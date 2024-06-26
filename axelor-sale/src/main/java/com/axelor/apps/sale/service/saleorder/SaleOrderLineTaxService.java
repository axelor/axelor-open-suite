package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;
import java.util.Set;

public interface SaleOrderLineTaxService {
  Set<TaxLine> getTaxLineSet(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  Map<String, Object> setTaxEquiv(SaleOrder saleOrder, SaleOrderLine saleOrderLine);
}

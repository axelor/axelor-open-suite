package com.axelor.apps.sale.service.saleorder.views;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import java.util.Map;

public interface SaleOrderViewService {

  Map<String, Map<String, Object>> getOnNewAttrs(SaleOrder saleOrder) throws AxelorException;

  Map<String, Map<String, Object>> getOnLoadAttrs(SaleOrder saleOrder) throws AxelorException;

  Map<String, Map<String, Object>> getPartnerOnChangeAttrs(SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Map<String, Object>> getCompanyAttrs(SaleOrder saleOrder);
}

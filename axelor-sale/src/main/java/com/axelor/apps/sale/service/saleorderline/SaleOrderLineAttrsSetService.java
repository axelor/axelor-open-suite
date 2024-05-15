package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineAttrsSetService {

  void showPriceDiscounted(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void manageHiddenAttrForPrices(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void displayAndSetLanguages(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void setHiddenAttrForDeliveredQty(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void setDiscountAmountTitle(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void setScaleAttrs(Map<String, Map<String, Object>> attrsMap);

  void hideFieldsForClientUser(Map<String, Map<String, Object>> attrsMap);

  void hideQtyWarningLabel(Map<String, Map<String, Object>> attrsMap);

  void hidePanels(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void hideAllPanelTabForClients(Map<String, Map<String, Object>> attrsMap);

  void defineTypesToSelect(Map<String, Map<String, Object>> attrsMap);
}

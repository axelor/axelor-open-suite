package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface XService {
  public String setProjectDomain(SaleOrder saleOrder);

  void showPriceDiscounted(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void manageHiddenAttrForPrices(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void displayAndSetLanguages(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void setHiddenAttrForDeliveredQty(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void setBillOfMaterialDomain(Map<String, Map<String, Object>> attrsMap);

  void setProdProcessDomain(Map<String, Map<String, Object>> attrsMap);

  void setDiscountAmountTitle(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void setCompanyCurrencyValue(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void setCurrencyValue(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void setScaleAttrs(Map<String, Map<String, Object>> attrsMap);

  void hideFieldsForClientUser(Map<String, Map<String, Object>> attrsMap);

  void setAvailabilityRequestValue(Map<String, Map<String, Object>> attrsMap);

  void hideQtyWarningLabel(Map<String, Map<String, Object>> attrsMap);

  void hidePanels(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void hideAllPanelTabForClients(Map<String, Map<String, Object>> attrsMap);

  void defineTypesToSelect(Map<String, Map<String, Object>> attrsMap);

  void initDummyFields(Map<String, Map<String, Object>> attrsMap);

  void setNonNegotiableValue(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void setInitialQty(Map<String, Map<String, Object>> attrsMap);

  void resetInvoicingMode(SaleOrderLine saleOrderLine);

  void setProjectTitle(Map<String, Map<String, Object>> attrsMap);

  void showDeliveryPanel(SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void setEstimatedDateValue(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void setIsReadOnlyValue(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void hideBillOfMaterialAndProdProcess(Map<String, Map<String, Object>> attrsMap);

  void setProjectValue(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void hideUpdateAllocatedQtyBtn(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void setRequestedReservedQtyTOReadOnly(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void updateRequestedReservedQty(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);
}

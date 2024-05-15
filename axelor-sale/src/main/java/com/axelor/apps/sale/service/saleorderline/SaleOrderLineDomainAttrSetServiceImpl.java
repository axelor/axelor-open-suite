package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.helper.SaleOrderLineHelper;
import java.util.Map;

public class SaleOrderLineDomainAttrSetServiceImpl implements SaleOrderLineDomainAttrSetService {

  @Override
  public void setBillOfMaterialDomain(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr(
        "billOfMaterial",
        "domain",
        "&quot;(self.product.id = "
            + saleOrderLine.getProduct().getParentProduct().getId()
            + " OR self.product.id ="
            + saleOrderLine.getProduct().getId()
            + ") AND self.defineSubBillOfMaterial = true &quot;",
        attrsMap);
  }

  @Override
  public void setProdProcessDomain(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr(
        "prodProcess",
        "domain",
        "&quot;(self.product.id ="
            + saleOrderLine.getProduct().getParentProduct().getId()
            + " OR self.product.id ="
            + saleOrderLine.getProduct().getId()
            + " &quot;",
        attrsMap);
  }

  @Override
  public String setProjectDomain(SaleOrder saleOrder) {
    return "self.clientPartner.id ="
        + saleOrder.getClientPartner().getId()
        + "AND self.isBusinessProject = true";
  }
}

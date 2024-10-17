package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.common.StringUtils;

public class SaleOrderLineDomainProductionServiceImpl
    implements SaleOrderLineDomainProductionService {
  @Override
  public String getBomDomain(SaleOrderLine saleOrderLine) {
    String domain = getProdProcessDomain(saleOrderLine);
    if (StringUtils.isEmpty(domain)) {
      return domain;
    }
    return domain + " AND self.defineSubBillOfMaterial = true";
  }

  @Override
  public String getProdProcessDomain(SaleOrderLine saleOrderLine) {
    StringBuilder domain = new StringBuilder();
    Product product = saleOrderLine.getProduct();
    if (product == null) {
      return "";
    }
    domain.append("self.product.id = ");
    domain.append(product.getId());

    Product parentProduct = product.getParentProduct();
    if (parentProduct != null) {
      domain.append(" OR self.product.id = ");
      domain.append(parentProduct.getId());
    }

    return domain.toString();
  }
}

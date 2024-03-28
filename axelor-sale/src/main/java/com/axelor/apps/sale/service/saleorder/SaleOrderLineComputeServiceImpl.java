package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.util.Objects;

public class SaleOrderLineComputeServiceImpl implements SaleOrderLineComputeService {
  @Override
  public void compute(SaleOrderLine saleOrderLine) {
    Objects.requireNonNull(saleOrderLine);

    if (saleOrderLine.getSubSaleOrderLineList() != null) {
      saleOrderLine.getSubSaleOrderLineList().forEach(this::compute);

      saleOrderLine.setQty(
          saleOrderLine.getSubSaleOrderLineList().stream()
              .map(SaleOrderLine::getQty)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO));
      saleOrderLine.setPrice(
          saleOrderLine.getSubSaleOrderLineList().stream()
              .map(SaleOrderLine::getPrice)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO));
      saleOrderLine.setInTaxPrice(
          saleOrderLine.getSubSaleOrderLineList().stream()
              .map(SaleOrderLine::getInTaxPrice)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO));
      saleOrderLine.setCompanyInTaxTotal(
          saleOrderLine.getSubSaleOrderLineList().stream()
              .map(SaleOrderLine::getCompanyInTaxTotal)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO));
    }
  }
}

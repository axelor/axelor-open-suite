package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.sale.db.SaleOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleOrderTaxNumberServiceImpl implements SaleOrderTaxNumberService {

  @Override
  public Map<String, Object> getTaxNumber(SaleOrder saleOrder) {
    Map<String, Object> saleOrderMap = new HashMap<>();
    Company company = saleOrder.getCompany();
    if (company != null) {
      List<TaxNumber> taxNumberList = company.getTaxNumberList();
      if (taxNumberList.size() == 1) {
        saleOrder.setTaxNumber(taxNumberList.stream().findFirst().orElse(null));
      }
      saleOrderMap.put("taxNumber", saleOrder.getTaxNumber());
    }

    return saleOrderMap;
  }
}

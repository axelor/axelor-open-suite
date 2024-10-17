package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderBankDetailsServiceImpl implements SaleOrderBankDetailsService {

  protected BankDetailsService bankDetailsService;

  @Inject
  public SaleOrderBankDetailsServiceImpl(BankDetailsService bankDetailsService) {
    this.bankDetailsService = bankDetailsService;
  }

  @Override
  public Map<String, Object> getBankDetails(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    Company company = saleOrder.getCompany();
    if (company != null) {
      saleOrder.setCompanyBankDetails(
          bankDetailsService.getDefaultCompanyBankDetails(
              saleOrder.getCompany(), null, saleOrder.getClientPartner(), null));
      saleOrderMap.put("companyBankDetails", saleOrder.getCompanyBankDetails());
    }

    return saleOrderMap;
  }
}

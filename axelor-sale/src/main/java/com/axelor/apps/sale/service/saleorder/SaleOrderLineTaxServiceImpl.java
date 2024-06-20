package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.Set;

public class SaleOrderLineTaxServiceImpl implements SaleOrderLineTaxService {

  protected AccountManagementService accountManagementService;

  @Inject
  public SaleOrderLineTaxServiceImpl(AccountManagementService accountManagementService) {
    this.accountManagementService = accountManagementService;
  }

  @Override
  public Set<TaxLine> getTaxLineSet(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    return accountManagementService.getTaxLineSet(
        saleOrder.getCreationDate(),
        saleOrderLine.getProduct(),
        saleOrder.getCompany(),
        saleOrder.getFiscalPosition(),
        false);
  }
}

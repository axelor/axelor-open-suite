package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineTaxServiceImpl implements SaleOrderLineTaxService {

  protected AccountManagementService accountManagementService;
  protected FiscalPositionService fiscalPositionService;

  @Inject
  public SaleOrderLineTaxServiceImpl(
      AccountManagementService accountManagementService,
      FiscalPositionService fiscalPositionService) {
    this.accountManagementService = accountManagementService;
    this.fiscalPositionService = fiscalPositionService;
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

  @Override
  public Map<String, Object> setTaxEquiv(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.put("taxEquiv", null);
    saleOrderLine.setTaxEquiv(null);
    if (saleOrder == null
        || saleOrder.getClientPartner() == null
        || CollectionUtils.isEmpty(saleOrderLine.getTaxLineSet())) {
      return saleOrderLineMap;
    }
    TaxEquiv taxEquiv =
        fiscalPositionService.getTaxEquivFromTaxLines(
            saleOrder.getFiscalPosition(), saleOrderLine.getTaxLineSet());
    saleOrderLine.setTaxEquiv(taxEquiv);
    saleOrderLineMap.put("taxEquiv", taxEquiv);
    return saleOrderLineMap;
  }
}

/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.sale.service.saleorderline.tax;

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

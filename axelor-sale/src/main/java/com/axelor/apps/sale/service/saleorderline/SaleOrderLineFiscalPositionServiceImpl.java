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
package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.tax.SaleOrderLineTaxService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineFiscalPositionServiceImpl implements SaleOrderLineFiscalPositionService {

  protected AccountManagementService accountManagementService;
  protected SaleOrderLineTaxService saleOrderLineTaxService;
  protected ProductCompanyService productCompanyService;
  protected TaxService taxService;
  protected AppBaseService appBaseService;

  @Inject
  public SaleOrderLineFiscalPositionServiceImpl(
      AccountManagementService accountManagementService,
      SaleOrderLineTaxService saleOrderLineTaxService,
      ProductCompanyService productCompanyService,
      TaxService taxService,
      AppBaseService appBaseService) {
    this.accountManagementService = accountManagementService;
    this.saleOrderLineTaxService = saleOrderLineTaxService;
    this.productCompanyService = productCompanyService;
    this.taxService = taxService;
    this.appBaseService = appBaseService;
  }

  public List<SaleOrderLine> updateLinesAfterFiscalPositionChange(SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();

    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return null;
    }

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {

      // Skip line update if product is not filled
      if (saleOrderLine.getProduct() == null) {
        continue;
      }

      updateLinePrice(saleOrder, saleOrderLine);
    }
    return saleOrderLineList;
  }

  protected void updateLinePrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    FiscalPosition fiscalPosition = saleOrder.getFiscalPosition();
    Set<TaxLine> taxLineSet = saleOrderLineTaxService.getTaxLineSet(saleOrder, saleOrderLine);
    saleOrderLine.setTaxLineSet(taxLineSet);

    TaxEquiv taxEquiv =
        accountManagementService.getProductTaxEquiv(
            saleOrderLine.getProduct(), saleOrder.getCompany(), fiscalPosition, false);

    saleOrderLine.setTaxEquiv(taxEquiv);

    BigDecimal exTaxTotal = saleOrderLine.getExTaxTotal();

    BigDecimal companyExTaxTotal = saleOrderLine.getCompanyExTaxTotal();

    BigDecimal salePrice =
        (BigDecimal)
            productCompanyService.get(
                saleOrderLine.getProduct(), "salePrice", saleOrder.getCompany());

    saleOrderLine.setInTaxTotal(
        taxService.convertUnitPrice(
            false, taxLineSet, exTaxTotal, appBaseService.getNbDecimalDigitForUnitPrice()));
    saleOrderLine.setCompanyInTaxTotal(
        taxService.convertUnitPrice(
            false, taxLineSet, companyExTaxTotal, appBaseService.getNbDecimalDigitForUnitPrice()));
    saleOrderLine.setInTaxPrice(
        taxService.convertUnitPrice(
            false, taxLineSet, salePrice, appBaseService.getNbDecimalDigitForUnitPrice()));
  }
}

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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.SubProduct;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.CurrencyScaleServiceSale;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.SaleInvoicingStateService;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SaleOrderLineProjectServiceImpl extends SaleOrderLineServiceSupplyChainImpl
    implements SaleOrderLineProjectService {

  @Inject
  public SaleOrderLineProjectServiceImpl(
      CurrencyService currencyService,
      PriceListService priceListService,
      ProductMultipleQtyService productMultipleQtyService,
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      AccountManagementService accountManagementService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderService saleOrderService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      PricingService pricingService,
      TaxService taxService,
      SaleOrderMarginService saleOrderMarginService,
      InvoiceLineRepository invoiceLineRepository,
      SaleInvoicingStateService saleInvoicingStateService,
      AnalyticLineModelService analyticLineModelService,
      CurrencyScaleServiceSale currencyScaleServiceSale) {
    super(
        currencyService,
        priceListService,
        productMultipleQtyService,
        appBaseService,
        appSaleService,
        accountManagementService,
        saleOrderLineRepo,
        saleOrderService,
        appAccountService,
        analyticMoveLineService,
        appSupplychainService,
        accountConfigService,
        pricingService,
        taxService,
        saleOrderMarginService,
        invoiceLineRepository,
        saleInvoicingStateService,
        analyticLineModelService,
        currencyScaleServiceSale);
  }

  @Transactional
  @Override
  public void setProject(List<Long> saleOrderLineIds, Project project) {

    if (saleOrderLineIds != null) {

      List<SaleOrderLine> saleOrderLineList =
          saleOrderLineRepo.all().filter("self.id in ?1", saleOrderLineIds).fetch();

      for (SaleOrderLine line : saleOrderLineList) {
        line.setProject(project);
        saleOrderLineRepo.save(line);
      }
    }
  }

  @Override
  public SaleOrderLine updateAnalyticDistributionWithProject(SaleOrderLine saleOrderLine) {
    for (AnalyticMoveLine analyticMoveLine : saleOrderLine.getAnalyticMoveLineList()) {
      analyticMoveLine.setProject(saleOrderLine.getProject());
    }
    return saleOrderLine;
  }

  @Override
  public SaleOrderLine createLinesForSubProducts(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Set<SubProduct> productSet = saleOrderLine.getProduct().getSubProductList();
    if (productSet == null || productSet.isEmpty()) {
      return saleOrderLine;
    }
    for (SubProduct subProduct : productSet) {
      SaleOrderLine relatedSaleOrderLine = createSaleOrderline(subProduct, saleOrder);
      saleOrderLine.addSaleOrderLineListItem(relatedSaleOrderLine);
      saleOrderLine.setSaleOrderLineListSize(saleOrderLine.getSaleOrderLineList().size());
      relatedSaleOrderLine.setToInvoice(true);
      relatedSaleOrderLine.setInvoicingModeSelect(3);
      relatedSaleOrderLine.setLineIndex(
          saleOrderLine.getLineIndex() + "." + (saleOrderLine.getSaleOrderLineListSize()));
      createLinesForSubProducts(relatedSaleOrderLine, saleOrder);
    }
    return saleOrderLine;
  }

  public SaleOrderLine createSaleOrderline(SubProduct subProduct, SaleOrder saleOrder)
      throws AxelorException {
    SaleOrderLine saleOrderLine = new SaleOrderLine();
    saleOrderLine.setProduct(subProduct.getProduct());
    saleOrderLine.setQty(subProduct.getQty());
    Beans.get(SaleOrderLineService.class).computeProductInformation(saleOrderLine, saleOrder);
    Beans.get(SaleOrderLineService.class).computeValues(saleOrder, saleOrderLine);
    if (Objects.equals(saleOrderLine.getPriceBeforeUpdate(), BigDecimal.ZERO)) {
      saleOrderLine.setPriceBeforeUpdate(saleOrderLine.getPrice());
    }
    return saleOrderLine;
  }
}

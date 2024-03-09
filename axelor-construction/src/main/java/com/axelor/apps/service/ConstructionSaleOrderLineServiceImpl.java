package com.axelor.apps.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.businessproduction.service.SaleOrderLineBusinessProductionServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.CurrencyScaleServiceSale;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.SaleInvoicingStateService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

public class ConstructionSaleOrderLineServiceImpl
    extends SaleOrderLineBusinessProductionServiceImpl {

  @Inject
  public ConstructionSaleOrderLineServiceImpl(
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

  @Override
  public void computeProductInformation(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {

    saleOrderLine.setGrossMarging(saleOrder.getCompany().getSaleConfig().getDefaultGrossMarging());
    saleOrderLine.setGeneralExpenses(
        saleOrderLine
            .getProduct()
            .getShippingCoef()
            .add(saleOrderLine.getProduct().getManagPriceCoef()));
    saleOrderLine.setCostPrice(saleOrderLine.getProduct().getCostPrice());
    super.computeProductInformation(saleOrderLine, saleOrder);
  }

  @Override
  protected BigDecimal getUnitPrice(
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      Set<TaxLine> taxLineSet,
      boolean resultInAti)
      throws AxelorException {
    Product product = saleOrderLine.getProduct();

    Boolean productInAti =
        (Boolean) productCompanyService.get(product, "inAti", saleOrder.getCompany());

    BigDecimal productSalePrice =
        saleOrderLine
            .getCostPrice()
            .multiply(saleOrderLine.getGrossMarging())
            .multiply(saleOrderLine.getGeneralExpenses());

    BigDecimal price =
        (productInAti == resultInAti)
            ? productSalePrice
            : taxService.convertUnitPrice(
                productInAti, taxLineSet, productSalePrice, AppBaseService.COMPUTATION_SCALING);

    /*return currencyService
        .getAmountCurrencyConvertedAtDate(
            (Currency) productCompanyService.get(product, "saleCurrency", saleOrder.getCompany()),
            saleOrder.getCurrency(),
            price,
            saleOrder.getCreationDate())
        .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);*/

    return price;
  }
}

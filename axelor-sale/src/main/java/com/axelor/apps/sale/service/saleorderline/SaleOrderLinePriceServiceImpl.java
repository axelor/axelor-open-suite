package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductPriceService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SaleOrderLinePriceServiceImpl implements SaleOrderLinePriceService {

  protected CurrencyScaleService currencyScaleService;
  protected CurrencyService currencyService;
  protected ProductCompanyService productCompanyService;
  protected TaxService taxService;
  protected AppSaleService appSaleService;
  protected ProductPriceService productPriceService;

  @Inject
  public SaleOrderLinePriceServiceImpl(
      CurrencyScaleService currencyScaleService,
      CurrencyService currencyService,
      ProductCompanyService productCompanyService,
      TaxService taxService,
      AppSaleService appSaleService,
      ProductPriceService productPriceService) {
    this.currencyScaleService = currencyScaleService;
    this.currencyService = currencyService;
    this.productCompanyService = productCompanyService;
    this.taxService = taxService;
    this.appSaleService = appSaleService;
    this.productPriceService = productPriceService;
  }

  @Override
  public void resetPrice(SaleOrderLine line) {
    if (!line.getEnableFreezeFields()) {
      line.setPrice(null);
      line.setInTaxPrice(null);
    }
  }

  @Override
  public BigDecimal getExTaxUnitPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException {
    return this.getUnitPrice(saleOrder, saleOrderLine, taxLineSet, false);
  }

  @Override
  public BigDecimal getInTaxUnitPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException {
    return this.getUnitPrice(saleOrder, saleOrderLine, taxLineSet, true);
  }

  @Override
  public BigDecimal getCompanyCostPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    Product product = saleOrderLine.getProduct();
    Company company = saleOrder.getCompany();

    return currencyScaleService.getCompanyScaledValue(
        saleOrder,
        currencyService.getAmountCurrencyConvertedAtDate(
            (Currency) productCompanyService.get(product, "purchaseCurrency", company),
            company != null ? company.getCurrency() : null,
            (BigDecimal) productCompanyService.get(product, "costPrice", company),
            saleOrder.getCreationDate()));
  }

  @Override
  public Map<String, Object> updateInTaxPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    if (saleOrder == null
        || saleOrderLine.getProduct() == null
        || saleOrderLine.getPrice() == null
        || saleOrderLine.getInTaxPrice() == null) {
      return saleOrderLineMap;
    }

    BigDecimal price = saleOrderLine.getPrice();

    BigDecimal inTaxPrice =
        taxService.convertUnitPrice(
            false,
            saleOrderLine.getTaxLineSet(),
            price,
            appSaleService.getNbDecimalDigitForUnitPrice());
    saleOrderLine.setInTaxPrice(inTaxPrice);
    saleOrderLineMap.put("inTaxPrice", inTaxPrice);
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> updatePrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    if (saleOrder == null
        || saleOrderLine.getProduct() == null
        || saleOrderLine.getPrice() == null
        || saleOrderLine.getInTaxPrice() == null) {
      return saleOrderLineMap;
    }

    BigDecimal inTaxPrice = saleOrderLine.getInTaxPrice();
    BigDecimal price =
        taxService.convertUnitPrice(
            true,
            saleOrderLine.getTaxLineSet(),
            inTaxPrice,
            appSaleService.getNbDecimalDigitForUnitPrice());
    saleOrderLine.setPrice(price);
    saleOrderLineMap.put("price", price);
    return saleOrderLineMap;
  }

  /**
   * A function used to get the unit price of a sale order line, either in ati or wt
   *
   * @param saleOrder the sale order containing the sale order line
   * @param saleOrderLine
   * @param taxLineSet the tax applied to the unit price
   * @param resultInAti whether you want the result in ati or not
   * @return the unit price of the sale order line
   * @throws AxelorException
   */
  protected BigDecimal getUnitPrice(
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      Set<TaxLine> taxLineSet,
      boolean resultInAti)
      throws AxelorException {
    Product product = saleOrderLine.getProduct();
    Company company = saleOrder.getCompany();

    // Consider price if already computed from pricing scale else get it from product
    BigDecimal productSalePrice = saleOrderLine.getPrice();

    Map<String, Object> map = productPriceService.getSaleUnitPrice(product, company);
    Currency fromCurrency = (Currency) map.get("currency");

    if (productSalePrice.compareTo(BigDecimal.ZERO) == 0) {
      productSalePrice = (BigDecimal) map.get("price");
    }

    return productPriceService.getConvertedPrice(
        company,
        product,
        taxLineSet,
        resultInAti,
        saleOrder.getCreationDate(),
        productSalePrice,
        fromCurrency,
        saleOrder.getCurrency());
  }
}

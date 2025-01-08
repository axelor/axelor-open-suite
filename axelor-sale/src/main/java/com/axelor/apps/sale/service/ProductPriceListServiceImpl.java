package com.axelor.apps.sale.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductPriceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.MapUtils;

public class ProductPriceListServiceImpl implements ProductPriceListService {

  protected ProductPriceService productPriceService;
  protected PriceListService priceListService;
  protected SaleConfigService saleConfigService;
  protected TaxService taxService;
  protected AppBaseService appBaseService;
  protected AppSaleService appSaleService;
  protected CompanyService companyService;
  protected AccountManagementService accountManagementService;

  @Inject
  public ProductPriceListServiceImpl(
      SaleConfigService saleConfigService,
      CompanyService companyService,
      TaxService taxService,
      AppBaseService appBaseService,
      ProductPriceService productPriceService,
      AppSaleService appSaleService,
      PriceListService priceListService,
      AccountManagementService accountManagementService) {
    this.productPriceService = productPriceService;
    this.priceListService = priceListService;
    this.accountManagementService = accountManagementService;
    this.appSaleService = appSaleService;
    this.saleConfigService = saleConfigService;
    this.companyService = companyService;
    this.taxService = taxService;
    this.appBaseService = appBaseService;
  }

  @Override
  public BigDecimal applyPriceList(
      Product product, Partner partner, Company company, Currency currency, boolean inAti)
      throws AxelorException {

    BigDecimal price =
        productPriceService.getSaleUnitPrice(company, product, false, partner, currency);
    Map<String, Object> discountMap = fillDiscount(company, price, partner, product);
    BigDecimal exTaxPrice =
        priceListService.computeDiscount(
            price,
            (Integer) discountMap.get("discountTypeSelect"),
            (BigDecimal) discountMap.get("discountAmount"));
    return exTaxPrice;
  }

  public Map<String, Object> fillDiscount(
      Company company, BigDecimal price, Partner partner, Product product) throws AxelorException {
    int discountTypeSelect;
    BigDecimal discountAmount;
    Map<String, Object> discounts = getDiscountsFromPriceLists(partner, price, product);
    Set<TaxLine> taxLineSet =
        accountManagementService.getTaxLineSet(
            appSaleService.getTodayDate(company),
            product,
            company,
            partner.getFiscalPosition(),
            false);
    Map<String, Object> discounMap = new HashMap<>();
    boolean companyInAti = false;
    SaleConfig saleConfig = saleConfigService.getSaleConfig(company);
    Company defaultCompany = companyService.getDefaultCompany(null);
    if (defaultCompany != null) {
      int saleOrderInAtiSelect = saleConfig.getSaleOrderInAtiSelect();
      companyInAti =
          saleOrderInAtiSelect == SaleConfigRepository.SALE_ATI_ALWAYS
              || saleOrderInAtiSelect == SaleConfigRepository.SALE_ATI_DEFAULT;
    }
    if (MapUtils.isNotEmpty(discounts)) {
      if (!product.getInAti().equals(companyInAti)
          && (Integer) discounts.get("discountTypeSelect")
              != PriceListLineRepository.AMOUNT_TYPE_PERCENT) {
        discountAmount =
            taxService.convertUnitPrice(
                product.getInAti(),
                taxLineSet,
                (BigDecimal) discounts.get("discountAmount"),
                appBaseService.getNbDecimalDigitForUnitPrice());
      } else {
        discountAmount = (BigDecimal) discounts.get("discountAmount");
      }
      discountTypeSelect = (Integer) discounts.get("discountTypeSelect");
    } else {
      discountAmount = BigDecimal.ZERO;
      discountTypeSelect = PriceListLineRepository.AMOUNT_TYPE_NONE;
    }

    discounMap.put("discountAmount", discountAmount);
    discounMap.put("discountTypeSelect", discountTypeSelect);
    return discounMap;
  }

  public Map<String, Object> getDiscountsFromPriceLists(
      Partner partner, BigDecimal price, Product product) {

    Map<String, Object> discounts = new HashMap<>();
    PriceList priceList =
        Beans.get(PartnerPriceListService.class)
            .getDefaultPriceList(partner, PriceListRepository.TYPE_SALE);
    if (priceList != null) {
      PriceListLine priceListLine =
          priceListService.getPriceListLine(product, BigDecimal.valueOf(1), priceList, price);
      discounts = priceListService.getReplacedPriceAndDiscounts(priceList, priceListLine, price);
    }

    return discounts;
  }
}

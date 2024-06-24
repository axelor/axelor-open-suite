package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.auth.AuthUtils;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SaleOrderLineProductServiceImpl implements SaleOrderLineProductService {

  protected AppSaleService appSaleService;
  protected AppBaseService appBaseService;
  protected SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService;
  protected InternationalService internationalService;
  protected TaxService taxService;
  protected AccountManagementService accountManagementService;
  protected SaleOrderLinePricingService saleOrderLinePricingService;
  protected SaleOrderLineDiscountService saleOrderLineDiscountService;
  protected SaleOrderLinePriceService saleOrderLinePriceService;
  protected SaleOrderLineTaxService saleOrderLineTaxService;

  @Inject
  public SaleOrderLineProductServiceImpl(
      AppSaleService appSaleService,
      AppBaseService appBaseService,
      SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService,
      InternationalService internationalService,
      TaxService taxService,
      AccountManagementService accountManagementService,
      SaleOrderLinePricingService saleOrderLinePricingService,
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLinePriceService saleOrderLinePriceService,
      SaleOrderLineTaxService saleOrderLineTaxService) {
    this.appSaleService = appSaleService;
    this.appBaseService = appBaseService;
    this.saleOrderLineComplementaryProductService = saleOrderLineComplementaryProductService;
    this.internationalService = internationalService;
    this.taxService = taxService;
    this.accountManagementService = accountManagementService;
    this.saleOrderLinePricingService = saleOrderLinePricingService;
    this.saleOrderLineDiscountService = saleOrderLineDiscountService;
    this.saleOrderLinePriceService = saleOrderLinePriceService;
    this.saleOrderLineTaxService = saleOrderLineTaxService;
  }

  @Override
  public Map<String, Object> computeProductInformation(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {

    Map<String, Object> saleOrderLineMap = resetProductInformation(saleOrderLine);

    if (!saleOrderLine.getEnableFreezeFields()) {
      saleOrderLine.setProductName(saleOrderLine.getProduct().getName());
      saleOrderLineMap.put("productName", saleOrderLine.getProduct().getName());
    }
    saleOrderLine.setUnit(this.getSaleUnit(saleOrderLine));
    saleOrderLineMap.put("unit", saleOrderLine.getUnit());
    if (appSaleService.getAppSale().getIsEnabledProductDescriptionCopy()) {
      saleOrderLine.setDescription(saleOrderLine.getProduct().getDescription());
      saleOrderLineMap.put("description", saleOrderLine.getDescription());
    }

    saleOrderLine.setTypeSelect(SaleOrderLineRepository.TYPE_NORMAL);
    saleOrderLineMap.put("typeSelect", SaleOrderLineRepository.TYPE_NORMAL);

    saleOrderLineMap.putAll(fillPrice(saleOrderLine, saleOrder));
    saleOrderLineMap.putAll(
        saleOrderLineComplementaryProductService.fillComplementaryProductList(saleOrderLine));
    saleOrderLineMap.putAll(translateProductNameAndDescription(saleOrderLine, saleOrder));

    return saleOrderLineMap;
  }

  protected Map<String, Object> translateProductNameAndDescription(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    String userLanguage = AuthUtils.getUser().getLanguage();
    Product product = saleOrderLine.getProduct();
    Partner partner = saleOrder.getClientPartner();

    if (product != null) {
      Map<String, String> translation =
          internationalService.getProductDescriptionAndNameTranslation(
              product, partner, userLanguage);

      String description = translation.get("description");
      String productName = translation.get("productName");

      if (description != null
          && !description.isEmpty()
          && productName != null
          && !productName.isEmpty()) {
        if (appSaleService.getAppSale().getIsEnabledProductDescriptionCopy()) {
          saleOrderLineMap.put("description", description);
        }
        saleOrderLineMap.put("productName", productName);
      }
    }
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> fillPrice(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {

    Map<String, Object> saleOrderLineMap = new HashMap<>();

    // Populate fields from pricing scale before starting process of fillPrice
    if (appBaseService.getAppBase().getEnablePricingScale()) {
      saleOrderLinePricingService.computePricingScale(saleOrderLine, saleOrder);
    }

    saleOrderLineMap.putAll(fillTaxInformation(saleOrderLine, saleOrder));
    saleOrderLine.setCompanyCostPrice(
        saleOrderLinePriceService.getCompanyCostPrice(saleOrder, saleOrderLine));
    BigDecimal exTaxPrice;
    BigDecimal inTaxPrice;
    if (saleOrderLine.getProduct().getInAti()) {
      inTaxPrice =
          saleOrderLinePriceService.getInTaxUnitPrice(
              saleOrder, saleOrderLine, saleOrderLine.getTaxLineSet());
      saleOrderLineMap.putAll(
          saleOrderLineDiscountService.fillDiscount(saleOrderLine, saleOrder, inTaxPrice));
      inTaxPrice =
          saleOrderLineDiscountService.getDiscountedPrice(saleOrderLine, saleOrder, inTaxPrice);
      if (!saleOrderLine.getEnableFreezeFields()) {
        saleOrderLine.setPrice(
            taxService.convertUnitPrice(
                true,
                saleOrderLine.getTaxLineSet(),
                inTaxPrice,
                appBaseService.getNbDecimalDigitForUnitPrice()));
        saleOrderLine.setInTaxPrice(inTaxPrice);
      }
    } else {
      exTaxPrice =
          saleOrderLinePriceService.getExTaxUnitPrice(
              saleOrder, saleOrderLine, saleOrderLine.getTaxLineSet());
      saleOrderLineMap.putAll(
          saleOrderLineDiscountService.fillDiscount(saleOrderLine, saleOrder, exTaxPrice));
      exTaxPrice =
          saleOrderLineDiscountService.getDiscountedPrice(saleOrderLine, saleOrder, exTaxPrice);
      if (!saleOrderLine.getEnableFreezeFields()) {
        saleOrderLine.setPrice(exTaxPrice);
        saleOrderLine.setInTaxPrice(
            taxService.convertUnitPrice(
                false,
                saleOrderLine.getTaxLineSet(),
                exTaxPrice,
                appBaseService.getNbDecimalDigitForUnitPrice()));
      }
    }

    saleOrderLineMap.put("companyCostPrice", saleOrderLine.getCompanyCostPrice());
    saleOrderLineMap.put("inTaxPrice", saleOrderLine.getInTaxPrice());
    saleOrderLineMap.put("price", saleOrderLine.getPrice());
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> fillTaxInformation(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    TaxEquiv taxEquiv = null;
    Set<TaxLine> taxLineSet = Set.of();

    if (saleOrder.getClientPartner() != null) {
      taxLineSet = saleOrderLineTaxService.getTaxLineSet(saleOrder, saleOrderLine);
      saleOrderLine.setTaxLineSet(taxLineSet);

      FiscalPosition fiscalPosition = saleOrder.getFiscalPosition();

      taxEquiv =
          accountManagementService.getProductTaxEquiv(
              saleOrderLine.getProduct(), saleOrder.getCompany(), fiscalPosition, false);

      saleOrderLine.setTaxEquiv(taxEquiv);
    } else {
      saleOrderLine.setTaxLineSet(Sets.newHashSet());
      saleOrderLine.setTaxEquiv(null);
    }

    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.put("taxEquiv", taxEquiv);
    saleOrderLineMap.put("taxLineSet", taxLineSet);
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> resetProductInformation(SaleOrderLine line) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    if (!line.getEnableFreezeFields()) {
      line.setProductName(null);
      line.setPrice(null);
    }
    line.setTaxLineSet(Sets.newHashSet());
    line.setTaxEquiv(null);
    line.setUnit(null);
    line.setCompanyCostPrice(null);
    line.setDiscountAmount(null);
    line.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_NONE);
    line.setInTaxPrice(null);
    line.setExTaxTotal(null);
    line.setInTaxTotal(null);
    line.setCompanyInTaxTotal(null);
    line.setCompanyExTaxTotal(null);
    if (appSaleService.getAppSale().getIsEnabledProductDescriptionCopy()) {
      line.setDescription(null);
    }
    line.setTypeSelect(SaleOrderLineRepository.TYPE_NORMAL);
    line.clearSelectedComplementaryProductList();

    saleOrderLineMap.put("productName", line.getProductName());
    saleOrderLineMap.put("price", line.getPrice());
    saleOrderLineMap.put("unit", line.getUnit());
    saleOrderLineMap.put("companyCostPrice", line.getCompanyCostPrice());
    saleOrderLineMap.put("discountAmount", line.getDiscountAmount());
    saleOrderLineMap.put("discountTypeSelect", line.getDiscountTypeSelect());
    saleOrderLineMap.put("inTaxPrice", line.getInTaxPrice());
    saleOrderLineMap.put("exTaxTotal", line.getExTaxTotal());
    saleOrderLineMap.put("inTaxTotal", line.getInTaxTotal());
    saleOrderLineMap.put("companyInTaxTotal", line.getCompanyInTaxTotal());
    saleOrderLineMap.put("companyExTaxTotal", line.getCompanyExTaxTotal());
    saleOrderLineMap.put("description", line.getDescription());
    saleOrderLineMap.put("typeSelect", line.getTypeSelect());
    saleOrderLineMap.put(
        "selectedComplementaryProductList", line.getSelectedComplementaryProductList());
    saleOrderLineMap.put("taxLineSet", line.getTaxLineSet());
    saleOrderLineMap.put("taxEquiv", line.getTaxEquiv());
    return saleOrderLineMap;
  }

  @Override
  public Unit getSaleUnit(SaleOrderLine saleOrderLine) {
    Unit unit = saleOrderLine.getProduct().getSalesUnit();
    if (unit == null) {
      unit = saleOrderLine.getProduct().getUnit();
    }
    return unit;
  }
}

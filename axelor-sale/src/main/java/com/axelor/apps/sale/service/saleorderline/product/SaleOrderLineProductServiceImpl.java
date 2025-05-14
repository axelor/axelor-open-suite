/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorderline.product;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.pricing.SaleOrderLinePricingService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.tax.SaleOrderLineTaxService;
import com.axelor.db.mapper.Mapper;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

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
  protected ProductCompanyService productCompanyService;
  protected CurrencyScaleService currencyScaleService;

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
      SaleOrderLineTaxService saleOrderLineTaxService,
      ProductCompanyService productCompanyService,
      CurrencyScaleService currencyScaleService) {
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
    this.productCompanyService = productCompanyService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public Map<String, Object> computeProductInformation(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {

    Map<String, Object> saleOrderLineMap = resetProductInformation(saleOrderLine);

    Product product = saleOrderLine.getProduct();
    if (product == null) {
      return saleOrderLineMap;
    }
    if (!saleOrderLine.getEnableFreezeFields()) {
      saleOrderLine.setProductName(product.getName());
      saleOrderLineMap.put("productName", product.getName());
    }
    saleOrderLine.setUnit(this.getSaleUnit(saleOrderLine.getProduct()));
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
    saleOrderLineMap.putAll(
        saleOrderLineComplementaryProductService.setIsComplementaryProductsUnhandledYet(
            saleOrderLine));
    saleOrderLineMap.putAll(fillCostPrice(saleOrderLine, saleOrder));

    return saleOrderLineMap;
  }

  protected Map<String, Object> translateProductNameAndDescription(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    Product product = saleOrderLine.getProduct();
    Partner partner = saleOrder.getClientPartner();

    if (product != null) {
      Map<String, String> translation =
          internationalService.getProductDescriptionAndNameTranslation(product, partner);

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

  protected Map<String, Object> fillCostPrice(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();

    Product product = saleOrderLine.getProduct();
    BigDecimal costPrice =
        ((BigDecimal)
            productCompanyService.get(
                saleOrderLine.getProduct(), "costPrice", saleOrder.getCompany()));
    if (product != null && costPrice.compareTo(BigDecimal.ZERO) != 0) {
      saleOrderLine.setSubTotalCostPrice(
          currencyScaleService.getCompanyScaledValue(
              saleOrder, costPrice.multiply(saleOrderLine.getQty())));
    }
    saleOrderLineMap.put("subTotalCostPrice", saleOrderLine.getSubTotalCostPrice());
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> fillPrice(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {

    Map<String, Object> saleOrderLineMap = new HashMap<>();

    // Populate fields from pricing scale before starting process of fillPrice
    if (appBaseService.getAppBase().getEnablePricingScale()) {
      saleOrderLinePricingService.computePricingScale(saleOrderLine, saleOrder);
      saleOrderLineMap.put("pricingScaleLogs", saleOrderLine.getPricingScaleLogs());
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
    Map<String, Object> saleOrderLineMap = resetProductInformationMap(line);

    for (Map.Entry<String, Object> entry : saleOrderLineMap.entrySet()) {
      Mapper.of(SaleOrderLine.class).set(line, entry.getKey(), entry.getValue());
    }
    return saleOrderLineMap;
  }

  protected Map<String, Object> resetProductInformationMap(SaleOrderLine line) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();

    saleOrderLineMap.put("productName", null);
    saleOrderLineMap.put("price", null);
    saleOrderLineMap.put("priceDiscounted", null);
    saleOrderLineMap.put("unit", null);
    saleOrderLineMap.put("companyCostPrice", null);
    saleOrderLineMap.put("discountAmount", null);
    saleOrderLineMap.put("discountTypeSelect", PriceListLineRepository.AMOUNT_TYPE_NONE);
    saleOrderLineMap.put("inTaxPrice", null);
    saleOrderLineMap.put("exTaxTotal", null);
    saleOrderLineMap.put("inTaxTotal", null);
    saleOrderLineMap.put("companyInTaxTotal", null);
    saleOrderLineMap.put("companyExTaxTotal", null);
    saleOrderLineMap.put("description", null);
    saleOrderLineMap.put("typeSelect", SaleOrderLineRepository.TYPE_NORMAL);
    if (CollectionUtils.isNotEmpty(line.getSelectedComplementaryProductList())) {
      line.clearSelectedComplementaryProductList();
      saleOrderLineMap.put(
          "selectedComplementaryProductList", line.getSelectedComplementaryProductList());
    }
    saleOrderLineMap.put("taxLineSet", Sets.newHashSet());
    saleOrderLineMap.put("taxEquiv", null);

    return saleOrderLineMap;
  }

  @Override
  public Unit getSaleUnit(Product product) {
    if (product == null) {
      return null;
    }
    Unit unit = product.getSalesUnit();
    if (unit == null) {
      unit = product.getUnit();
    }
    return unit;
  }
}

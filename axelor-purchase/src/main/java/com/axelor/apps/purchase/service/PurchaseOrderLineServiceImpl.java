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
package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductMultipleQty;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppPurchase;
import com.axelor.utils.helpers.ContextHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderLineServiceImpl implements PurchaseOrderLineService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject protected CurrencyService currencyService;

  @Inject protected AccountManagementService accountManagementService;

  @Inject protected PriceListService priceListService;

  @Inject protected AppBaseService appBaseService;

  @Inject protected ProductMultipleQtyService productMultipleQtyService;

  @Inject protected AppPurchaseService appPurchaseService;

  @Inject protected SupplierCatalogService supplierCatalogService;

  @Inject protected ProductCompanyService productCompanyService;

  @Inject protected TaxService taxService;

  @Inject protected CurrencyScaleService currencyScaleService;

  @Inject protected FiscalPositionService fiscalPositionService;

  @Deprecated private int sequence = 0;

  @Override
  public Map<String, BigDecimal> compute(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) throws AxelorException {

    HashMap<String, BigDecimal> map = new HashMap<>();
    if (purchaseOrder == null
        || purchaseOrderLine.getPrice() == null
        || purchaseOrderLine.getInTaxPrice() == null
        || purchaseOrderLine.getQty() == null) {
      return map;
    }

    BigDecimal exTaxTotal;
    BigDecimal companyExTaxTotal;
    BigDecimal inTaxTotal;
    BigDecimal companyInTaxTotal;
    BigDecimal priceDiscounted = this.computeDiscount(purchaseOrderLine, purchaseOrder.getInAti());
    BigDecimal taxRate = BigDecimal.ZERO;

    if (CollectionUtils.isNotEmpty(purchaseOrderLine.getTaxLineSet())) {
      taxRate = taxService.getTotalTaxRate(purchaseOrderLine.getTaxLineSet());
    }

    if (!purchaseOrder.getInAti()) {
      exTaxTotal =
          computeAmount(
              purchaseOrderLine.getQty(),
              priceDiscounted,
              currencyScaleService.getScale(purchaseOrder));
      inTaxTotal =
          currencyScaleService.getScaledValue(
              purchaseOrder, exTaxTotal.add(exTaxTotal.multiply(taxRate)));
      companyExTaxTotal = getCompanyExTaxTotal(exTaxTotal, purchaseOrder);
      companyInTaxTotal =
          currencyScaleService.getCompanyScaledValue(
              purchaseOrder, companyExTaxTotal.add(companyExTaxTotal.multiply(taxRate)));
    } else {
      inTaxTotal =
          computeAmount(
              purchaseOrderLine.getQty(),
              priceDiscounted,
              currencyScaleService.getScale(purchaseOrder));
      exTaxTotal =
          inTaxTotal.divide(
              taxRate.add(BigDecimal.ONE),
              currencyScaleService.getScale(purchaseOrder),
              RoundingMode.HALF_UP);
      companyInTaxTotal = getCompanyExTaxTotal(inTaxTotal, purchaseOrder);
      companyExTaxTotal =
          companyInTaxTotal.divide(
              taxRate.add(BigDecimal.ONE),
              currencyScaleService.getCompanyScale(purchaseOrder),
              RoundingMode.HALF_UP);
    }

    if (purchaseOrderLine.getProduct() != null) {
      map.put("maxPurchasePrice", getPurchaseMaxPrice(purchaseOrder, purchaseOrderLine));
    }
    map.put("exTaxTotal", exTaxTotal);
    map.put("inTaxTotal", inTaxTotal);
    map.put("companyExTaxTotal", companyExTaxTotal);
    map.put("companyInTaxTotal", companyInTaxTotal);
    map.put("priceDiscounted", priceDiscounted);
    purchaseOrderLine.setExTaxTotal(exTaxTotal);
    purchaseOrderLine.setInTaxTotal(inTaxTotal);
    purchaseOrderLine.setPriceDiscounted(priceDiscounted);
    purchaseOrderLine.setCompanyExTaxTotal(companyExTaxTotal);
    purchaseOrderLine.setCompanyInTaxTotal(companyInTaxTotal);
    purchaseOrderLine.setMaxPurchasePrice(getPurchaseMaxPrice(purchaseOrder, purchaseOrderLine));
    return map;
  }

  /**
   * Calculer le montant HT d'une ligne de commande.
   *
   * @param quantity Quantité.
   * @param price Le prix.
   * @return Le montant HT de la ligne.
   */
  public static BigDecimal computeAmount(BigDecimal quantity, BigDecimal price, int scale) {

    BigDecimal amount = quantity.multiply(price).setScale(scale, RoundingMode.HALF_UP);

    LOG.debug(
        "Computation of amount W.T. with a quantity of {} for {} : {}",
        new Object[] {quantity, price, amount});

    return amount;
  }

  /**
   * Returns the ex. tax unit price of the purchase order line or null if the product is not
   * available for purchase at the supplier of the purchase order
   */
  @Override
  public BigDecimal getExTaxUnitPrice(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException {
    return supplierCatalogService.getUnitPrice(
        purchaseOrderLine.getProduct(),
        purchaseOrder.getSupplierPartner(),
        purchaseOrder.getCompany(),
        purchaseOrder.getCurrency(),
        purchaseOrder.getOrderDate(),
        taxLineSet,
        false);
  }

  /**
   * Returns the incl. tax unit price of the purchase order line or null if the product is not
   * available for purchase at the supplier of the purchase order
   */
  @Override
  public BigDecimal getInTaxUnitPrice(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException {
    return supplierCatalogService.getUnitPrice(
        purchaseOrderLine.getProduct(),
        purchaseOrder.getSupplierPartner(),
        purchaseOrder.getCompany(),
        purchaseOrder.getCurrency(),
        purchaseOrder.getOrderDate(),
        taxLineSet,
        true);
  }

  public PurchaseOrderLine fill(PurchaseOrderLine line, PurchaseOrder purchaseOrder)
      throws AxelorException {
    Preconditions.checkNotNull(line, I18n.get("The line cannot be null."));
    Preconditions.checkNotNull(
        purchaseOrder, I18n.get("You need a purchase order associated to line."));
    Product product = line.getProduct();
    Partner supplierPartner = purchaseOrder.getSupplierPartner();
    Company company = purchaseOrder.getCompany();

    Map<String, String> productSupplierInfos =
        supplierCatalogService.getProductSupplierInfos(supplierPartner, company, product);
    if (!line.getEnableFreezeFields()) {
      line.setProductName(productSupplierInfos.get("productName"));
      line.setQty(supplierCatalogService.getQty(product, supplierPartner, company));
    }
    line.setProductCode(productSupplierInfos.get("productCode"));

    if (line.getProductName() == null || line.getProductName().isEmpty()) {
      line.setProductName(product.getName());
    }

    if (line.getProductCode() == null || line.getProductCode().isEmpty()) {
      line.setProductCode(product.getCode());
    }

    line.setUnit(supplierCatalogService.getUnit(product, supplierPartner, company));

    if (appPurchaseService.getAppPurchase().getIsEnabledProductDescriptionCopy()) {
      line.setDescription(product.getDescription());
    }

    Set<TaxLine> taxLineSet = getTaxLineSet(purchaseOrder, line);
    line.setTaxLineSet(taxLineSet);

    BigDecimal price = getExTaxUnitPrice(purchaseOrder, line, taxLineSet);
    BigDecimal inTaxPrice = getInTaxUnitPrice(purchaseOrder, line, taxLineSet);

    if (price == null || inTaxPrice == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_LINE_NO_SUPPLIER_CATALOG));
    }

    TaxEquiv taxEquiv =
        accountManagementService.getProductTaxEquiv(
            product, purchaseOrder.getCompany(), purchaseOrder.getFiscalPosition(), true);
    line.setTaxEquiv(taxEquiv);

    Map<String, Object> discounts =
        getDiscountsFromPriceLists(
            purchaseOrder, line, purchaseOrder.getInAti() ? inTaxPrice : price);

    if (discounts != null) {
      if (discounts.get("price") != null) {
        BigDecimal discountPrice = (BigDecimal) discounts.get("price");
        if (product.getInAti()) {
          inTaxPrice = discountPrice;
          price =
              taxService.convertUnitPrice(
                  true,
                  line.getTaxLineSet(),
                  discountPrice,
                  appBaseService.getNbDecimalDigitForUnitPrice());
        } else {
          price = discountPrice;
          inTaxPrice =
              taxService.convertUnitPrice(
                  false,
                  line.getTaxLineSet(),
                  discountPrice,
                  appBaseService.getNbDecimalDigitForUnitPrice());
        }
      }
      if (product.getInAti() != purchaseOrder.getInAti()
          && (Integer) discounts.get("discountTypeSelect")
              != PriceListLineRepository.AMOUNT_TYPE_PERCENT) {
        line.setDiscountAmount(
            taxService.convertUnitPrice(
                product.getInAti(),
                line.getTaxLineSet(),
                (BigDecimal) discounts.get("discountAmount"),
                appBaseService.getNbDecimalDigitForUnitPrice()));
      } else {
        line.setDiscountAmount((BigDecimal) discounts.get("discountAmount"));
      }
      line.setDiscountTypeSelect((Integer) discounts.get("discountTypeSelect"));
    }
    if (!line.getEnableFreezeFields()) {
      line.setPrice(price);
    }
    line.setInTaxPrice(inTaxPrice);

    line.setMaxPurchasePrice(getPurchaseMaxPrice(purchaseOrder, line));
    return line;
  }

  @Override
  public PurchaseOrderLine reset(PurchaseOrderLine line) {
    if (!line.getEnableFreezeFields()) {
      line.setQty(BigDecimal.ZERO);
      line.setPrice(null);
      line.setProductName(null);
    }
    line.setTaxLineSet(Sets.newHashSet());
    line.setUnit(null);
    line.setDiscountAmount(null);
    line.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_NONE);
    line.setInTaxPrice(null);
    line.setMaxPurchasePrice(null);
    line.setExTaxTotal(null);
    line.setInTaxTotal(null);
    line.setCompanyInTaxTotal(null);
    line.setCompanyExTaxTotal(null);
    line.setProductCode(null);
    if (appPurchaseService.getAppPurchase().getIsEnabledProductDescriptionCopy()) {
      line.setDescription(null);
    }
    return line;
  }

  @Override
  public BigDecimal getPurchaseMaxPrice(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException {

    try {

      Product product = purchaseOrderLine.getProduct();

      if (product == null
          || !((Boolean)
              productCompanyService.get(product, "sellable", purchaseOrder.getCompany()))) {
        return BigDecimal.ZERO;
      }

      Set<TaxLine> saleTaxLineSet =
          accountManagementService.getTaxLineSet(
              purchaseOrder.getOrderDate(),
              purchaseOrderLine.getProduct(),
              purchaseOrder.getCompany(),
              purchaseOrder.getFiscalPosition(),
              false);

      BigDecimal price;
      if (purchaseOrder.getInAti()
          != (Boolean) productCompanyService.get(product, "inAti", purchaseOrder.getCompany())) {
        price =
            taxService.convertUnitPrice(
                (Boolean) productCompanyService.get(product, "inAti", purchaseOrder.getCompany()),
                saleTaxLineSet,
                ((BigDecimal)
                        productCompanyService.get(product, "salePrice", purchaseOrder.getCompany()))
                    .divide(
                        product.getManagPriceCoef().signum() == 0
                            ? BigDecimal.ONE
                            : product.getManagPriceCoef(),
                        appBaseService.getNbDecimalDigitForUnitPrice(),
                        RoundingMode.HALF_UP),
                AppBaseService.COMPUTATION_SCALING);
      } else {
        price =
            ((BigDecimal)
                    productCompanyService.get(product, "salePrice", purchaseOrder.getCompany()))
                .divide(
                    product.getManagPriceCoef().signum() == 0
                        ? BigDecimal.ONE
                        : product.getManagPriceCoef(),
                    appBaseService.getNbDecimalDigitForUnitPrice(),
                    RoundingMode.HALF_UP);
      }
      return currencyService
          .getAmountCurrencyConvertedAtDate(
              (Currency)
                  productCompanyService.get(product, "saleCurrency", purchaseOrder.getCompany()),
              purchaseOrder.getCurrency(),
              price,
              purchaseOrder.getOrderDate())
          .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);

    } catch (Exception e) {
      return BigDecimal.ZERO;
    }
  }

  @Override
  public Set<TaxLine> getTaxLineSet(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException {

    return accountManagementService.getTaxLineSet(
        purchaseOrder.getOrderDate(),
        purchaseOrderLine.getProduct(),
        purchaseOrder.getCompany(),
        purchaseOrder.getFiscalPosition(),
        true);
  }

  @Override
  public BigDecimal computePurchaseOrderLine(PurchaseOrderLine purchaseOrderLine) {

    return purchaseOrderLine.getExTaxTotal();
  }

  @Override
  public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, PurchaseOrder purchaseOrder)
      throws AxelorException {

    return currencyScaleService.getCompanyScaledValue(
        purchaseOrder,
        currencyService.getAmountCurrencyConvertedAtDate(
            purchaseOrder.getCurrency(),
            purchaseOrder.getCompany().getCurrency(),
            exTaxTotal,
            purchaseOrder.getOrderDate()));
  }

  @Override
  public PriceListLine getPriceListLine(
      PurchaseOrderLine purchaseOrderLine, PriceList priceList, BigDecimal price) {

    return priceListService.getPriceListLine(
        purchaseOrderLine.getProduct(), purchaseOrderLine.getQty(), priceList, price);
  }

  @Override
  public BigDecimal computeDiscount(PurchaseOrderLine purchaseOrderLine, Boolean inAti) {

    BigDecimal price = inAti ? purchaseOrderLine.getInTaxPrice() : purchaseOrderLine.getPrice();

    return priceListService.computeDiscount(
        price, purchaseOrderLine.getDiscountTypeSelect(), purchaseOrderLine.getDiscountAmount());
  }

  @Override
  public PurchaseOrderLine createPurchaseOrderLine(
      PurchaseOrder purchaseOrder,
      Product product,
      String productName,
      String description,
      BigDecimal qty,
      Unit unit)
      throws AxelorException {

    PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine();
    purchaseOrderLine.setPurchaseOrder(purchaseOrder);

    purchaseOrderLine.setEstimatedReceiptDate(purchaseOrder.getEstimatedReceiptDate());

    if (product != null) {
      purchaseOrderLine.setProduct(product);
      fill(purchaseOrderLine, purchaseOrder);
    }

    if (description != null) {
      purchaseOrderLine.setDescription(description);
    }

    purchaseOrderLine.setIsOrdered(false);

    if (qty != null) {
      purchaseOrderLine.setQty(qty);
    }
    purchaseOrderLine.setSequence(sequence);
    sequence++;

    if (unit != null) {
      purchaseOrderLine.setUnit(unit);
    }

    if (productName != null) {
      purchaseOrderLine.setProductName(productName);
    }

    compute(purchaseOrderLine, purchaseOrder);

    return purchaseOrderLine;
  }

  @Override
  public SupplierCatalog getSupplierCatalog(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException {

    Product product = purchaseOrderLine.getProduct();

    SupplierCatalog supplierCatalog =
        supplierCatalogService.getSupplierCatalog(
            product, purchaseOrder.getSupplierPartner(), purchaseOrder.getCompany());

    //		If there is no catalog for supplier, then we don't take the default catalog.

    //		if(supplierCatalog == null)  {
    //
    //			supplierCatalog = this.getSupplierCatalog(product, product.getDefaultSupplierPartner());
    //		}

    return supplierCatalog;
  }

  @Override
  public Map<String, Object> updateInfoFromCatalog(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException {
    return supplierCatalogService.updateInfoFromCatalog(
        purchaseOrderLine.getProduct(),
        purchaseOrderLine.getQty(),
        purchaseOrder.getSupplierPartner(),
        purchaseOrder.getCurrency(),
        purchaseOrder.getOrderDate(),
        purchaseOrder.getCompany());
  }

  @Override
  public Map<String, Object> getDiscountsFromPriceLists(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, BigDecimal price) {

    Map<String, Object> discounts = null;

    PriceList priceList = purchaseOrder.getPriceList();

    if (priceList != null) {
      PriceListLine priceListLine = this.getPriceListLine(purchaseOrderLine, priceList, price);
      discounts = priceListService.getReplacedPriceAndDiscounts(priceList, priceListLine, price);
    }

    return discounts;
  }

  @Override
  public int getDiscountTypeSelect(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder, BigDecimal price) {
    PriceList priceList = purchaseOrder.getPriceList();
    if (priceList != null) {
      PriceListLine priceListLine = this.getPriceListLine(purchaseOrderLine, priceList, price);

      return priceListLine.getTypeSelect();
    }
    return 0;
  }

  @Override
  public void checkMultipleQty(
      Company company,
      Partner supplierPartner,
      PurchaseOrderLine purchaseOrderLine,
      ActionResponse response)
      throws AxelorException {
    Product product = purchaseOrderLine.getProduct();
    AppPurchase appPurchase = appPurchaseService.getAppPurchase();

    if (product == null || Boolean.FALSE.equals(appPurchase.getManageMultiplePurchaseQuantity())) {
      return;
    }

    SupplierCatalog supplierCatalog =
        supplierCatalogService.getSupplierCatalog(product, supplierPartner, company);
    List<ProductMultipleQty> productMultipleQties = product.getPurchaseProductMultipleQtyList();

    if (supplierCatalog != null
        && Boolean.FALSE.equals(supplierCatalog.getIsTakeProductMultipleQty())) {
      productMultipleQties = supplierCatalog.getProductMultipleQtyList();
    }

    if (CollectionUtils.isNotEmpty(productMultipleQties)) {
      productMultipleQtyService.checkMultipleQty(
          purchaseOrderLine.getQty(),
          productMultipleQties,
          product.getAllowToForcePurchaseQty(),
          response);
    }
  }

  @Override
  public void checkDifferentSupplier(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, ActionResponse response) {
    if (!appBaseService.getAppBase().getEnableTradingNamesManagement()) {
      return;
    }

    Product product = purchaseOrderLine.getProduct();
    TradingName tradingName = purchaseOrder.getTradingName();

    if (product == null || tradingName == null) {
      return;
    }

    Partner supplierOnPurchaseOrder = purchaseOrder.getSupplierPartner();
    Partner defaultSupplierOnProduct = product.getDefaultSupplierPartner();
    if (defaultSupplierOnProduct == null) {
      return;
    }

    if (supplierOnPurchaseOrder != defaultSupplierOnProduct) {

      String message = String.format(I18n.get(PurchaseExceptionMessage.DIFFERENT_SUPPLIER));
      String title =
          String.format(
              "<span class='label %s'>%s</span>", ContextHelper.SPAN_CLASS_WARNING, message);

      response.setAttr("differentSupplierLabel", "title", title);
      response.setAttr("differentSupplierLabel", "hidden", false);
    } else {
      response.setAttr("differentSupplierLabel", "hidden", true);
    }
  }

  public List<PurchaseOrderLine> updateLinesAfterFiscalPositionChange(PurchaseOrder purchaseOrder)
      throws AxelorException {
    List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();

    if (CollectionUtils.isEmpty(purchaseOrderLineList)) {
      return null;
    }

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {

      // Skip line update if product is not filled
      if (purchaseOrderLine.getProduct() == null) {
        continue;
      }

      FiscalPosition fiscalPosition = purchaseOrder.getFiscalPosition();

      Set<TaxLine> taxLineSet = this.getTaxLineSet(purchaseOrder, purchaseOrderLine);
      purchaseOrderLine.setTaxLineSet(taxLineSet);

      TaxEquiv taxEquiv =
          accountManagementService.getProductTaxEquiv(
              purchaseOrderLine.getProduct(), purchaseOrder.getCompany(), fiscalPosition, true);

      purchaseOrderLine.setTaxEquiv(taxEquiv);

      BigDecimal exTaxTotal = purchaseOrderLine.getExTaxTotal();

      BigDecimal companyExTaxTotal = purchaseOrderLine.getCompanyExTaxTotal();

      BigDecimal purchasePrice =
          (BigDecimal)
              productCompanyService.get(
                  purchaseOrderLine.getProduct(), "purchasePrice", purchaseOrder.getCompany());

      purchaseOrderLine.setInTaxTotal(
          currencyScaleService.getScaledValue(
              purchaseOrder,
              taxService.convertUnitPrice(
                  false, taxLineSet, exTaxTotal, appBaseService.getNbDecimalDigitForUnitPrice())));
      purchaseOrderLine.setCompanyInTaxTotal(
          currencyScaleService.getCompanyScaledValue(
              purchaseOrder,
              taxService.convertUnitPrice(
                  false,
                  taxLineSet,
                  companyExTaxTotal,
                  appBaseService.getNbDecimalDigitForUnitPrice())));
      purchaseOrderLine.setInTaxPrice(
          taxService.convertUnitPrice(
              false, taxLineSet, purchasePrice, appBaseService.getNbDecimalDigitForUnitPrice()));
    }
    return purchaseOrderLineList;
  }

  @Override
  public Map<String, Object> recomputeTax(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException {
    Set<TaxLine> taxLineSet = purchaseOrderLine.getTaxLineSet();
    TaxEquiv taxEquiv = null;
    FiscalPosition fiscalPosition = purchaseOrder.getFiscalPosition();

    Map<String, Object> valuesMap = new HashMap<>();
    if (fiscalPosition == null || CollectionUtils.isEmpty(taxLineSet)) {
      valuesMap.put("taxEquiv", taxEquiv);
      return valuesMap;
    }
    taxEquiv = fiscalPositionService.getTaxEquivFromOrToTaxSet(fiscalPosition, taxLineSet);
    if (taxEquiv != null) {
      taxLineSet = taxService.getTaxLineSet(taxEquiv.getToTaxSet(), purchaseOrder.getOrderDate());
    }

    valuesMap.put("taxLineSet", taxLineSet);
    valuesMap.put("taxEquiv", taxEquiv);
    return valuesMap;
  }
}

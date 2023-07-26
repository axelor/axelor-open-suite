/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCategoryService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.pricing.PricingComputer;
import com.axelor.apps.base.service.pricing.PricingObserver;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.ComplementaryProduct;
import com.axelor.apps.sale.db.ComplementaryProductSelected;
import com.axelor.apps.sale.db.Pack;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.ComplementaryProductRepository;
import com.axelor.apps.sale.db.repo.PackLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.pricing.SaleOrderLinePricingObserver;
import com.axelor.apps.sale.translation.ITranslation;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.meta.loader.ModuleManager;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderLineServiceImpl implements SaleOrderLineService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected CurrencyService currencyService;
  protected PriceListService priceListService;
  protected ProductMultipleQtyService productMultipleQtyService;
  protected AppBaseService appBaseService;
  protected AppSaleService appSaleService;
  protected AccountManagementService accountManagementService;
  protected SaleOrderLineRepository saleOrderLineRepo;
  protected SaleOrderService saleOrderService;
  protected PricingService pricingService;
  protected TaxService taxService;
  protected SaleOrderMarginService saleOrderMarginService;

  @Inject
  public SaleOrderLineServiceImpl(
      CurrencyService currencyService,
      PriceListService priceListService,
      ProductMultipleQtyService productMultipleQtyService,
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      AccountManagementService accountManagementService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderService saleOrderService,
      PricingService pricingService,
      TaxService taxService,
      SaleOrderMarginService saleOrderMarginService) {
    this.currencyService = currencyService;
    this.priceListService = priceListService;
    this.productMultipleQtyService = productMultipleQtyService;
    this.appBaseService = appBaseService;
    this.appSaleService = appSaleService;
    this.accountManagementService = accountManagementService;
    this.saleOrderLineRepo = saleOrderLineRepo;
    this.saleOrderService = saleOrderService;
    this.pricingService = pricingService;
    this.taxService = taxService;
    this.saleOrderMarginService = saleOrderMarginService;
  }

  @Inject protected ProductCategoryService productCategoryService;
  @Inject protected ProductCompanyService productCompanyService;

  @Override
  public void computeProductInformation(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {

    // Reset fields which are going to recalculate in this method
    resetProductInformation(saleOrderLine);

    if (!saleOrderLine.getEnableFreezeFields()) {
      saleOrderLine.setProductName(saleOrderLine.getProduct().getName());
    }
    saleOrderLine.setUnit(this.getSaleUnit(saleOrderLine));
    if (appSaleService.getAppSale().getIsEnabledProductDescriptionCopy()) {
      saleOrderLine.setDescription(saleOrderLine.getProduct().getDescription());
    }

    saleOrderLine.setTypeSelect(SaleOrderLineRepository.TYPE_NORMAL);
    fillPrice(saleOrderLine, saleOrder);
    fillComplementaryProductList(saleOrderLine);
  }

  @Override
  public void computePricingScale(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {

    Optional<Pricing> pricing = getRootPricing(saleOrderLine, saleOrder);
    if (pricing.isPresent() && saleOrderLine.getProduct() != null) {
      PricingComputer pricingComputer =
          getPricingComputer(pricing.get(), saleOrderLine)
              .putInContext("saleOrder", EntityHelper.getEntity(saleOrder));
      pricingComputer.subscribe(getSaleOrderLinePricingObserver(saleOrderLine));
      pricingComputer.apply();
    } else {
      saleOrderLine.setPricingScaleLogs(I18n.get(ITranslation.SALE_ORDER_LINE_OBSERVER_NO_PRICING));
    }
  }

  protected PricingObserver getSaleOrderLinePricingObserver(SaleOrderLine saleOrderLine) {
    return new SaleOrderLinePricingObserver(saleOrderLine);
  }

  protected PricingComputer getPricingComputer(Pricing pricing, SaleOrderLine saleOrderLine)
      throws AxelorException {

    return PricingComputer.of(
        pricing, saleOrderLine, saleOrderLine.getProduct(), SaleOrderLine.class);
  }

  protected Optional<Pricing> getRootPricing(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    // It is supposed that only one pricing match those criteria (because of the configuration)
    // Having more than one pricing matched may result on a unexpected result
    return pricingService.getRandomPricing(
        saleOrder.getCompany(),
        saleOrderLine.getProduct(),
        saleOrderLine.getProduct() != null ? saleOrderLine.getProduct().getProductCategory() : null,
        SaleOrderLine.class.getSimpleName(),
        null);
  }

  @Override
  public boolean hasPricingLine(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {

    Optional<Pricing> pricing = getRootPricing(saleOrderLine, saleOrder);
    if (pricing.isPresent()) {
      return !getPricingComputer(pricing.get(), saleOrderLine)
          .putInContext("saleOrder", EntityHelper.getEntity(saleOrder))
          .getMatchedPricingLines()
          .isEmpty();
    }

    return false;
  }

  @Override
  public void fillPrice(SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {

    // Populate fields from pricing scale before starting process of fillPrice
    if (appSaleService.getAppSale().getEnablePricingScale()) {
      computePricingScale(saleOrderLine, saleOrder);
    }

    fillTaxInformation(saleOrderLine, saleOrder);
    saleOrderLine.setCompanyCostPrice(this.getCompanyCostPrice(saleOrder, saleOrderLine));
    BigDecimal exTaxPrice;
    BigDecimal inTaxPrice;
    if (saleOrderLine.getProduct().getInAti()) {
      inTaxPrice = this.getInTaxUnitPrice(saleOrder, saleOrderLine, saleOrderLine.getTaxLine());
      inTaxPrice = fillDiscount(saleOrderLine, saleOrder, inTaxPrice);
      if (!saleOrderLine.getEnableFreezeFields()) {
        saleOrderLine.setPrice(
            taxService.convertUnitPrice(
                true,
                saleOrderLine.getTaxLine(),
                inTaxPrice,
                appBaseService.getNbDecimalDigitForUnitPrice()));
        saleOrderLine.setInTaxPrice(inTaxPrice);
      }
    } else {
      exTaxPrice = this.getExTaxUnitPrice(saleOrder, saleOrderLine, saleOrderLine.getTaxLine());
      exTaxPrice = fillDiscount(saleOrderLine, saleOrder, exTaxPrice);
      if (!saleOrderLine.getEnableFreezeFields()) {
        saleOrderLine.setPrice(exTaxPrice);
        saleOrderLine.setInTaxPrice(
            taxService.convertUnitPrice(
                false,
                saleOrderLine.getTaxLine(),
                exTaxPrice,
                appBaseService.getNbDecimalDigitForUnitPrice()));
      }
    }
  }

  @Override
  public void fillComplementaryProductList(SaleOrderLine saleOrderLine) {
    if (saleOrderLine.getProduct() != null
        && saleOrderLine.getProduct().getComplementaryProductList() != null) {
      if (saleOrderLine.getSelectedComplementaryProductList() == null) {
        saleOrderLine.setSelectedComplementaryProductList(new ArrayList<>());
      }
      saleOrderLine.clearSelectedComplementaryProductList();
      for (ComplementaryProduct complProduct :
          saleOrderLine.getProduct().getComplementaryProductList()) {
        ComplementaryProductSelected newComplProductLine = new ComplementaryProductSelected();

        newComplProductLine.setProduct(complProduct.getProduct());
        newComplProductLine.setQty(complProduct.getQty());
        newComplProductLine.setOptional(complProduct.getOptional());

        newComplProductLine.setIsSelected(!complProduct.getOptional());
        newComplProductLine.setSaleOrderLine(saleOrderLine);
        saleOrderLine.addSelectedComplementaryProductListItem(newComplProductLine);
      }
    }
  }

  protected BigDecimal fillDiscount(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, BigDecimal price) {

    Map<String, Object> discounts =
        this.getDiscountsFromPriceLists(saleOrder, saleOrderLine, price);

    if (discounts != null) {
      if (discounts.get("price") != null) {
        price = (BigDecimal) discounts.get("price");
      }
      if (saleOrderLine.getProduct().getInAti() != saleOrder.getInAti()
          && (Integer) discounts.get("discountTypeSelect")
              != PriceListLineRepository.AMOUNT_TYPE_PERCENT) {
        saleOrderLine.setDiscountAmount(
            taxService.convertUnitPrice(
                saleOrderLine.getProduct().getInAti(),
                saleOrderLine.getTaxLine(),
                (BigDecimal) discounts.get("discountAmount"),
                appBaseService.getNbDecimalDigitForUnitPrice()));
      } else {
        saleOrderLine.setDiscountAmount((BigDecimal) discounts.get("discountAmount"));
      }
      saleOrderLine.setDiscountTypeSelect((Integer) discounts.get("discountTypeSelect"));
    } else if (!saleOrder.getTemplate()) {
      saleOrderLine.setDiscountAmount(BigDecimal.ZERO);
      saleOrderLine.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_NONE);
    }

    return price;
  }

  protected void fillTaxInformation(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {

    if (saleOrder.getClientPartner() != null) {
      TaxLine taxLine = this.getTaxLine(saleOrder, saleOrderLine);
      saleOrderLine.setTaxLine(taxLine);

      FiscalPosition fiscalPosition = saleOrder.getFiscalPosition();

      TaxEquiv taxEquiv =
          accountManagementService.getProductTaxEquiv(
              saleOrderLine.getProduct(), saleOrder.getCompany(), fiscalPosition, false);

      saleOrderLine.setTaxEquiv(taxEquiv);
    } else {
      saleOrderLine.setTaxLine(null);
      saleOrderLine.setTaxEquiv(null);
    }
  }

  @Override
  public SaleOrderLine resetProductInformation(SaleOrderLine line) {
    if (!line.getEnableFreezeFields()) {
      line.setProductName(null);
      line.setPrice(null);
    }
    line.setTaxLine(null);
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
    line.clearSelectedComplementaryProductList();
    return line;
  }

  @Override
  public void resetPrice(SaleOrderLine line) {
    if (!line.getEnableFreezeFields()) {
      line.setPrice(null);
      line.setInTaxPrice(null);
    }
  }

  @Override
  public Map<String, BigDecimal> computeValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    HashMap<String, BigDecimal> map = new HashMap<>();
    if (saleOrder == null
        || saleOrderLine.getPrice() == null
        || saleOrderLine.getInTaxPrice() == null
        || saleOrderLine.getQty() == null) {
      return map;
    }

    BigDecimal exTaxTotal;
    BigDecimal companyExTaxTotal;
    BigDecimal inTaxTotal;
    BigDecimal companyInTaxTotal;
    BigDecimal priceDiscounted = this.computeDiscount(saleOrderLine, saleOrder.getInAti());
    BigDecimal taxRate = BigDecimal.ZERO;
    BigDecimal subTotalCostPrice = BigDecimal.ZERO;

    if (saleOrderLine.getTaxLine() != null) {
      taxRate = saleOrderLine.getTaxLine().getValue().divide(new BigDecimal(100));
    }

    if (!saleOrder.getInAti()) {
      exTaxTotal = this.computeAmount(saleOrderLine.getQty(), priceDiscounted);
      inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
      companyExTaxTotal = this.getAmountInCompanyCurrency(exTaxTotal, saleOrder);
      companyInTaxTotal = companyExTaxTotal.add(companyExTaxTotal.multiply(taxRate));
    } else {
      inTaxTotal = this.computeAmount(saleOrderLine.getQty(), priceDiscounted);
      exTaxTotal = inTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
      companyInTaxTotal = this.getAmountInCompanyCurrency(inTaxTotal, saleOrder);
      companyExTaxTotal =
          companyInTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
    }

    if (saleOrderLine.getProduct() != null
        && ((BigDecimal)
                    productCompanyService.get(
                        saleOrderLine.getProduct(), "costPrice", saleOrder.getCompany()))
                .compareTo(BigDecimal.ZERO)
            != 0) {
      subTotalCostPrice =
          ((BigDecimal)
                  productCompanyService.get(
                      saleOrderLine.getProduct(), "costPrice", saleOrder.getCompany()))
              .multiply(saleOrderLine.getQty());
    }

    saleOrderLine.setInTaxTotal(inTaxTotal);
    saleOrderLine.setExTaxTotal(exTaxTotal);
    saleOrderLine.setPriceDiscounted(priceDiscounted);
    saleOrderLine.setCompanyInTaxTotal(companyInTaxTotal);
    saleOrderLine.setCompanyExTaxTotal(companyExTaxTotal);
    saleOrderLine.setSubTotalCostPrice(subTotalCostPrice);
    map.put("inTaxTotal", inTaxTotal);
    map.put("exTaxTotal", exTaxTotal);
    map.put("priceDiscounted", priceDiscounted);
    map.put("companyExTaxTotal", companyExTaxTotal);
    map.put("companyInTaxTotal", companyInTaxTotal);
    map.put("subTotalCostPrice", subTotalCostPrice);

    map.putAll(saleOrderMarginService.getSaleOrderLineComputedMarginInfo(saleOrder, saleOrderLine));

    return map;
  }

  /**
   * Compute the excluded tax total amount of a sale order line.
   *
   * @return The excluded tax total amount.
   */
  @Override
  public BigDecimal computeAmount(SaleOrderLine saleOrderLine) {

    BigDecimal price = this.computeDiscount(saleOrderLine, false);

    return computeAmount(saleOrderLine.getQty(), price);
  }

  @Override
  public BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

    BigDecimal amount =
        quantity
            .multiply(price)
            .setScale(AppSaleService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);

    logger.debug(
        "Computation of W.T. amount with a quantity of {} for {} : {}",
        new Object[] {quantity, price, amount});

    return amount;
  }

  @Override
  public BigDecimal getExTaxUnitPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, TaxLine taxLine) throws AxelorException {
    return this.getUnitPrice(saleOrder, saleOrderLine, taxLine, false);
  }

  @Override
  public BigDecimal getInTaxUnitPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, TaxLine taxLine) throws AxelorException {
    return this.getUnitPrice(saleOrder, saleOrderLine, taxLine, true);
  }

  /**
   * A function used to get the unit price of a sale order line, either in ati or wt
   *
   * @param saleOrder the sale order containing the sale order line
   * @param saleOrderLine
   * @param taxLine the tax applied to the unit price
   * @param resultInAti whether you want the result in ati or not
   * @return the unit price of the sale order line
   * @throws AxelorException
   */
  protected BigDecimal getUnitPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, TaxLine taxLine, boolean resultInAti)
      throws AxelorException {
    Product product = saleOrderLine.getProduct();

    Boolean productInAti =
        (Boolean) productCompanyService.get(product, "inAti", saleOrder.getCompany());

    // Consider price if already computed from pricing scale else get it from product
    BigDecimal productSalePrice = saleOrderLine.getPrice();

    if (productSalePrice.compareTo(BigDecimal.ZERO) == 0) {
      productSalePrice =
          (BigDecimal) productCompanyService.get(product, "salePrice", saleOrder.getCompany());
    }

    BigDecimal price =
        (productInAti == resultInAti)
            ? productSalePrice
            : taxService.convertUnitPrice(
                productInAti, taxLine, productSalePrice, AppBaseService.COMPUTATION_SCALING);

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            (Currency) productCompanyService.get(product, "saleCurrency", saleOrder.getCompany()),
            saleOrder.getCurrency(),
            price,
            saleOrder.getCreationDate())
        .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
  }

  @Override
  public TaxLine getTaxLine(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    return accountManagementService.getTaxLine(
        saleOrder.getCreationDate(),
        saleOrderLine.getProduct(),
        saleOrder.getCompany(),
        saleOrder.getFiscalPosition(),
        false);
  }

  @Override
  public BigDecimal getAmountInCompanyCurrency(BigDecimal exTaxTotal, SaleOrder saleOrder)
      throws AxelorException {

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            saleOrder.getCurrency(),
            saleOrder.getCompany().getCurrency(),
            exTaxTotal,
            saleOrder.getCreationDate())
        .setScale(AppSaleService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getCompanyCostPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    Product product = saleOrderLine.getProduct();

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            (Currency)
                productCompanyService.get(product, "purchaseCurrency", saleOrder.getCompany()),
            saleOrder.getCompany().getCurrency(),
            (BigDecimal) productCompanyService.get(product, "costPrice", saleOrder.getCompany()),
            saleOrder.getCreationDate())
        .setScale(AppSaleService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }

  @Override
  public PriceListLine getPriceListLine(
      SaleOrderLine saleOrderLine, PriceList priceList, BigDecimal price) {

    return priceListService.getPriceListLine(
        saleOrderLine.getProduct(), saleOrderLine.getQty(), priceList, price);
  }

  @Override
  public BigDecimal computeDiscount(SaleOrderLine saleOrderLine, Boolean inAti) {

    BigDecimal price = inAti ? saleOrderLine.getInTaxPrice() : saleOrderLine.getPrice();

    return priceListService.computeDiscount(
        price, saleOrderLine.getDiscountTypeSelect(), saleOrderLine.getDiscountAmount());
  }

  @Override
  public Map<String, Object> getDiscountsFromPriceLists(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, BigDecimal price) {

    Map<String, Object> discounts = null;

    PriceList priceList = saleOrder.getPriceList();

    if (priceList != null) {
      PriceListLine priceListLine = this.getPriceListLine(saleOrderLine, priceList, price);
      discounts = priceListService.getReplacedPriceAndDiscounts(priceList, priceListLine, price);

      if (saleOrder.getTemplate()) {
        Integer manualDiscountAmountType = saleOrderLine.getDiscountTypeSelect();
        BigDecimal manualDiscountAmount = saleOrderLine.getDiscountAmount();
        Integer priceListDiscountAmountType = (Integer) discounts.get("discountTypeSelect");
        BigDecimal priceListDiscountAmount = (BigDecimal) discounts.get("discountAmount");

        if (!manualDiscountAmountType.equals(priceListDiscountAmountType)
            && manualDiscountAmountType.equals(PriceListLineRepository.AMOUNT_TYPE_PERCENT)
            && priceListDiscountAmountType.equals(PriceListLineRepository.AMOUNT_TYPE_FIXED)) {
          priceListDiscountAmount =
              priceListDiscountAmount
                  .multiply(new BigDecimal(100))
                  .divide(price, 2, RoundingMode.HALF_UP);
        } else if (!manualDiscountAmountType.equals(priceListDiscountAmountType)
            && manualDiscountAmountType.equals(PriceListLineRepository.AMOUNT_TYPE_FIXED)
            && priceListDiscountAmountType.equals(PriceListLineRepository.AMOUNT_TYPE_PERCENT)) {
          priceListDiscountAmount =
              priceListDiscountAmount
                  .multiply(price)
                  .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        }

        if (manualDiscountAmount.compareTo(priceListDiscountAmount) > 0) {
          discounts.put("discountAmount", manualDiscountAmount);
          discounts.put("discountTypeSelect", manualDiscountAmountType);
        }
      }
    }

    return discounts;
  }

  @Override
  public int getDiscountTypeSelect(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, BigDecimal price) {
    PriceList priceList = saleOrder.getPriceList();
    if (priceList != null) {
      PriceListLine priceListLine = this.getPriceListLine(saleOrderLine, priceList, price);

      return priceListLine.getTypeSelect();
    }
    return 0;
  }

  @Override
  public Unit getSaleUnit(SaleOrderLine saleOrderLine) {
    Unit unit = saleOrderLine.getProduct().getSalesUnit();
    if (unit == null) {
      unit = saleOrderLine.getProduct().getUnit();
    }
    return unit;
  }

  @Override
  public SaleOrder getSaleOrder(Context context) {

    Context parentContext = context.getParent();

    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = saleOrderLine.getSaleOrder();

    if (parentContext != null && !parentContext.getContextClass().equals(SaleOrder.class)) {
      parentContext = parentContext.getParent();
    }

    if (parentContext != null && parentContext.getContextClass().equals(SaleOrder.class)) {
      saleOrder = parentContext.asType(SaleOrder.class);
    }

    return saleOrder;
  }

  @Override
  public BigDecimal getAvailableStock(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    // defined in supplychain
    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getAllocatedStock(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    // defined in supplychain
    return BigDecimal.ZERO;
  }

  @Override
  public void checkMultipleQty(SaleOrderLine saleOrderLine, ActionResponse response) {

    Product product = saleOrderLine.getProduct();

    if (product == null) {
      return;
    }

    productMultipleQtyService.checkMultipleQty(
        saleOrderLine.getQty(),
        product.getSaleProductMultipleQtyList(),
        product.getAllowToForceSaleQty(),
        response);
  }

  @Override
  public SaleOrderLine createSaleOrderLine(
      PackLine packLine,
      SaleOrder saleOrder,
      BigDecimal packQty,
      BigDecimal conversionRate,
      Integer sequence)
      throws AxelorException {

    if (packLine.getTypeSelect() == PackLineRepository.TYPE_START_OF_PACK
        || packLine.getTypeSelect() == PackLineRepository.TYPE_END_OF_PACK) {
      return createStartOfPackAndEndOfPackTypeSaleOrderLine(
          packLine.getPack(), saleOrder, packQty, packLine, packLine.getTypeSelect(), sequence);
    }

    if (packLine.getProductName() != null) {
      SaleOrderLine soLine = new SaleOrderLine();

      Product product = packLine.getProduct();
      soLine.setProduct(product);
      soLine.setProductName(packLine.getProductName());
      if (packLine.getQuantity() != null) {
        soLine.setQty(
            packLine
                .getQuantity()
                .multiply(packQty)
                .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP));
      }
      soLine.setUnit(packLine.getUnit());
      soLine.setTypeSelect(packLine.getTypeSelect());
      soLine.setSequence(sequence);
      if (packLine.getPrice() != null) {
        soLine.setPrice(packLine.getPrice().multiply(conversionRate));
      }

      if (product != null) {
        if (appSaleService.getAppSale().getIsEnabledProductDescriptionCopy()) {
          soLine.setDescription(product.getDescription());
        }
        try {
          this.fillPriceFromPackLine(soLine, saleOrder);
          this.computeValues(saleOrder, soLine);
        } catch (AxelorException e) {
          TraceBackService.trace(e);
        }
      }
      return soLine;
    }
    return null;
  }

  @Override
  public BigDecimal computeMaxDiscount(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Optional<BigDecimal> maxDiscount = Optional.empty();
    Product product = saleOrderLine.getProduct();
    if (product != null && product.getProductCategory() != null) {
      maxDiscount = productCategoryService.computeMaxDiscount(product.getProductCategory());
    }
    if (!maxDiscount.isPresent()
        || saleOrderLine.getDiscountTypeSelect() == PriceListLineRepository.AMOUNT_TYPE_NONE
        || saleOrder == null
        || (saleOrder.getStatusSelect() != SaleOrderRepository.STATUS_DRAFT_QUOTATION
            && (saleOrder.getStatusSelect() != SaleOrderRepository.STATUS_ORDER_CONFIRMED
                || !saleOrder.getOrderBeingEdited()))) {
      return null;
    } else {
      return maxDiscount.get();
    }
  }

  @Override
  public boolean isSaleOrderLineDiscountGreaterThanMaxDiscount(
      SaleOrderLine saleOrderLine, BigDecimal maxDiscount) {
    return (saleOrderLine.getDiscountTypeSelect() == PriceListLineRepository.AMOUNT_TYPE_PERCENT
            && saleOrderLine.getDiscountAmount().compareTo(maxDiscount) > 0)
        || (saleOrderLine.getDiscountTypeSelect() == PriceListLineRepository.AMOUNT_TYPE_FIXED
            && saleOrderLine.getPrice().signum() != 0
            && saleOrderLine
                    .getDiscountAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(saleOrderLine.getPrice(), 2, RoundingMode.HALF_UP)
                    .compareTo(maxDiscount)
                > 0);
  }

  @Override
  public List<SaleOrderLine> createNonStandardSOLineFromPack(
      Pack pack,
      SaleOrder saleOrder,
      BigDecimal packQty,
      List<SaleOrderLine> saleOrderLineList,
      Integer sequence) {
    SaleOrderLine saleOrderLine;
    Set<Integer> packLineTypeSet = getPackLineTypes(pack.getComponents());
    int typeSelect = SaleOrderLineRepository.TYPE_START_OF_PACK;
    for (int i = 0; i < 2; i++) {
      if (packLineTypeSet == null || !packLineTypeSet.contains(typeSelect)) {
        saleOrderLine =
            this.createStartOfPackAndEndOfPackTypeSaleOrderLine(
                pack, saleOrder, packQty, null, typeSelect, sequence);
        saleOrderLineList.add(saleOrderLine);
      }
      if (typeSelect == SaleOrderLineRepository.TYPE_START_OF_PACK) {
        sequence += pack.getComponents().size() + 1;
        typeSelect = SaleOrderLineRepository.TYPE_END_OF_PACK;
      }
    }

    return saleOrderLineList;
  }

  @Override
  public SaleOrderLine createStartOfPackAndEndOfPackTypeSaleOrderLine(
      Pack pack,
      SaleOrder saleOrder,
      BigDecimal packqty,
      PackLine packLine,
      Integer typeSelect,
      Integer sequence) {

    SaleOrderLine saleOrderLine = new SaleOrderLine();
    saleOrderLine.setTypeSelect(typeSelect);
    switch (typeSelect) {
      case SaleOrderLineRepository.TYPE_START_OF_PACK:
        saleOrderLine.setProductName(packLine == null ? pack.getName() : packLine.getProductName());
        saleOrderLine.setQty(
            packLine != null && packLine.getQuantity() != null
                ? packLine
                    .getQuantity()
                    .multiply(packqty)
                    .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_EVEN)
                : packqty);
        break;

      case SaleOrderLineRepository.TYPE_END_OF_PACK:
        saleOrderLine.setProductName(
            packLine == null
                ? I18n.get(ITranslation.SALE_ORDER_LINE_END_OF_PACK)
                : packLine.getProductName());
        saleOrderLine.setIsShowTotal(pack.getIsShowTotal());
        saleOrderLine.setIsHideUnitAmounts(pack.getIsHideUnitAmounts());
        break;
      default:
        return null;
    }
    saleOrderLine.setSaleOrder(saleOrder);
    saleOrderLine.setSequence(sequence);
    return saleOrderLine;
  }

  @Override
  public boolean hasEndOfPackTypeLine(List<SaleOrderLine> saleOrderLineList) {
    return ObjectUtils.isEmpty(saleOrderLineList)
        ? Boolean.FALSE
        : saleOrderLineList.stream()
            .anyMatch(
                saleOrderLine ->
                    saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_END_OF_PACK);
  }

  @Override
  public SaleOrderLine updateProductQty(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, BigDecimal oldQty, BigDecimal newQty)
      throws AxelorException {
    if (saleOrderLine.getTypeSelect() != SaleOrderLineRepository.TYPE_NORMAL) {
      return saleOrderLine;
    }
    this.fillPriceFromPackLine(saleOrderLine, saleOrder);
    this.computeValues(saleOrder, saleOrderLine);
    return saleOrderLine;
  }

  @Override
  public boolean isStartOfPackTypeLineQtyChanged(List<SaleOrderLine> saleOrderLineList) {

    if (ObjectUtils.isEmpty(saleOrderLineList)) {
      return false;
    }
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      if (saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_START_OF_PACK
          && saleOrderLine.getId() != null) {
        SaleOrderLine oldSaleOrderLine = saleOrderLineRepo.find(saleOrderLine.getId());
        if (oldSaleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_START_OF_PACK
            && saleOrderLine.getQty().compareTo(oldSaleOrderLine.getQty()) != 0) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void fillPriceFromPackLine(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    this.fillTaxInformation(saleOrderLine, saleOrder);
    saleOrderLine.setCompanyCostPrice(this.getCompanyCostPrice(saleOrder, saleOrderLine));
    BigDecimal exTaxPrice;
    BigDecimal inTaxPrice;
    if (saleOrderLine.getProduct().getInAti()) {
      inTaxPrice =
          this.getInTaxUnitPriceFromPackLine(saleOrder, saleOrderLine, saleOrderLine.getTaxLine());
      inTaxPrice = fillDiscount(saleOrderLine, saleOrder, inTaxPrice);
      if (!saleOrderLine.getEnableFreezeFields()) {
        saleOrderLine.setPrice(
            taxService.convertUnitPrice(
                true,
                saleOrderLine.getTaxLine(),
                inTaxPrice,
                appBaseService.getNbDecimalDigitForUnitPrice()));
        saleOrderLine.setInTaxPrice(inTaxPrice);
      }
    } else {
      exTaxPrice =
          this.getExTaxUnitPriceFromPackLine(saleOrder, saleOrderLine, saleOrderLine.getTaxLine());
      exTaxPrice = fillDiscount(saleOrderLine, saleOrder, exTaxPrice);
      if (!saleOrderLine.getEnableFreezeFields()) {
        saleOrderLine.setPrice(exTaxPrice);
        saleOrderLine.setInTaxPrice(
            taxService.convertUnitPrice(
                false,
                saleOrderLine.getTaxLine(),
                exTaxPrice,
                appBaseService.getNbDecimalDigitForUnitPrice()));
      }
    }
  }

  @Override
  public BigDecimal getExTaxUnitPriceFromPackLine(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, TaxLine taxLine) throws AxelorException {
    return this.getUnitPriceFromPackLine(saleOrder, saleOrderLine, taxLine, false);
  }

  @Override
  public BigDecimal getInTaxUnitPriceFromPackLine(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, TaxLine taxLine) throws AxelorException {
    return this.getUnitPriceFromPackLine(saleOrder, saleOrderLine, taxLine, true);
  }

  /**
   * A method used to get the unit price of a sale order line from pack line, either in ati or wt
   *
   * @param saleOrder the sale order containing the sale order line
   * @param saleOrderLine
   * @param taxLine the tax applied to the unit price
   * @param resultInAti whether you want the result in ati or not
   * @return the unit price of the sale order line
   * @throws AxelorException
   */
  protected BigDecimal getUnitPriceFromPackLine(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, TaxLine taxLine, boolean resultInAti)
      throws AxelorException {

    Product product = saleOrderLine.getProduct();

    Boolean productInAti =
        (Boolean) productCompanyService.get(product, "inAti", saleOrder.getCompany());
    BigDecimal productSalePrice = saleOrderLine.getPrice();

    BigDecimal price =
        (productInAti == resultInAti)
            ? productSalePrice
            : taxService.convertUnitPrice(
                productInAti, taxLine, productSalePrice, AppBaseService.COMPUTATION_SCALING);

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            (Currency) productCompanyService.get(product, "saleCurrency", saleOrder.getCompany()),
            saleOrder.getCurrency(),
            price,
            saleOrder.getCreationDate())
        .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
  }

  @Override
  public Set<Integer> getPackLineTypes(List<PackLine> packLineList) {
    Set<Integer> packLineTypeSet = new HashSet<>();
    packLineList.stream()
        .forEach(
            packLine -> {
              if (packLine.getTypeSelect() == PackLineRepository.TYPE_START_OF_PACK) {
                packLineTypeSet.add(PackLineRepository.TYPE_START_OF_PACK);
              } else if (packLine.getTypeSelect() == PackLineRepository.TYPE_END_OF_PACK) {
                packLineTypeSet.add(PackLineRepository.TYPE_END_OF_PACK);
              }
            });
    return packLineTypeSet;
  }

  @Override
  public String computeProductDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    String domain =
        "self.isModel = false"
            + " and (self.endDate = null or self.endDate > :__date__)"
            + " and self.dtype = 'Product'";

    if (appBaseService.getAppBase().getCompanySpecificProductFieldsSet() != null
        && appBaseService.getAppBase().getCompanySpecificProductFieldsSet().stream()
            .anyMatch(it -> "sellable".equals(it.getName()))
        && saleOrder != null
        && saleOrder.getCompany() != null) {
      domain +=
          " and (SELECT sellable "
              + "FROM ProductCompany productCompany "
              + "WHERE productCompany.product.id = self.id "
              + "AND productCompany.company.id = "
              + saleOrder.getCompany().getId()
              + ") IS TRUE ";
    } else {
      domain += " and self.sellable = true ";
    }

    if (appSaleService.getAppSale().getEnableSalesProductByTradName()
        && saleOrder != null
        && saleOrder.getTradingName() != null
        && saleOrder.getCompany() != null
        && saleOrder.getCompany().getTradingNameSet() != null
        && !saleOrder.getCompany().getTradingNameSet().isEmpty()) {
      domain +=
          " AND " + saleOrder.getTradingName().getId() + " member of self.tradingNameSellerSet";
    }

    // The standard way to do this would be to override the method in HR module.
    // But here, we have to do this because overriding a sale service in hr module will prevent the
    // override in supplychain, business-project, and business production module.
    if (ModuleManager.isInstalled("axelor-human-resource")) {
      domain += " AND self.expense = false ";
    }

    return domain;
  }

  @Override
  public List<SaleOrderLine> manageComplementaryProductSaleOrderLine(
      ComplementaryProduct complementaryProduct, SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    List<SaleOrderLine> newComplementarySOLines = new ArrayList<>();
    if (saleOrderLine.getMainSaleOrderLine() != null) {
      return newComplementarySOLines;
    }

    if (saleOrderLine.getComplementarySaleOrderLineList() == null) {
      saleOrderLine.setComplementarySaleOrderLineList(new ArrayList<>());
    }

    SaleOrderLine complementarySOLine =
        getOrCreateComplementryLine(
            complementaryProduct.getProduct(), saleOrderLine, newComplementarySOLines);

    complementarySOLine.setQty(complementaryProduct.getQty());
    complementarySOLine.setIsComplementaryPartnerProductsHandled(
        complementaryProduct.getGenerationTypeSelect()
            == ComplementaryProductRepository.GENERATION_TYPE_SALE_ORDER);
    this.computeProductInformation(complementarySOLine, saleOrder);
    this.computeValues(saleOrder, complementarySOLine);
    saleOrderLineRepo.save(complementarySOLine);
    return newComplementarySOLines;
  }

  protected SaleOrderLine getOrCreateComplementryLine(
      Product product, SaleOrderLine saleOrderLine, List<SaleOrderLine> newComplementarySOLines) {
    SaleOrderLine complementarySOLine;
    Optional<SaleOrderLine> complementarySOLineOpt =
        saleOrderLine.getComplementarySaleOrderLineList().stream()
            .filter(
                line -> line.getMainSaleOrderLine() != null && line.getProduct().equals(product))
            .findFirst();
    if (complementarySOLineOpt.isPresent()) {
      complementarySOLine = complementarySOLineOpt.get();
    } else {
      complementarySOLine = new SaleOrderLine();
      complementarySOLine.setSequence(saleOrderLine.getSequence());
      complementarySOLine.setProduct(product);
      complementarySOLine.setMainSaleOrderLine(saleOrderLine);
      newComplementarySOLines.add(complementarySOLine);
    }
    return complementarySOLine;
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

      FiscalPosition fiscalPosition = saleOrder.getFiscalPosition();
      TaxLine taxLine = this.getTaxLine(saleOrder, saleOrderLine);
      saleOrderLine.setTaxLine(taxLine);

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
              false, taxLine, exTaxTotal, appBaseService.getNbDecimalDigitForUnitPrice()));
      saleOrderLine.setCompanyInTaxTotal(
          taxService.convertUnitPrice(
              false, taxLine, companyExTaxTotal, appBaseService.getNbDecimalDigitForUnitPrice()));
      saleOrderLine.setInTaxPrice(
          taxService.convertUnitPrice(
              false, taxLine, salePrice, appBaseService.getNbDecimalDigitForUnitPrice()));
    }
    return saleOrderLineList;
  }
}

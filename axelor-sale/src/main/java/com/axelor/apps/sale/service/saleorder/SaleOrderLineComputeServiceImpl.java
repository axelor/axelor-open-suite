package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderLineComputeServiceImpl implements SaleOrderLineComputeService {
  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected TaxService taxService;
  protected CurrencyScaleService currencyScaleService;
  protected ProductCompanyService productCompanyService;
  protected SaleOrderMarginService saleOrderMarginService;
  protected CurrencyService currencyService;
  protected PriceListService priceListService;
  protected SaleOrderLineService saleOrderLineService;

  @Inject
  public SaleOrderLineComputeServiceImpl(
      TaxService taxService,
      CurrencyScaleService currencyScaleService,
      ProductCompanyService productCompanyService,
      SaleOrderMarginService saleOrderMarginService,
      CurrencyService currencyService,
      PriceListService priceListService,
      SaleOrderLineService saleOrderLineService) {
    this.taxService = taxService;
    this.currencyScaleService = currencyScaleService;
    this.productCompanyService = productCompanyService;
    this.saleOrderMarginService = saleOrderMarginService;
    this.currencyService = currencyService;
    this.priceListService = priceListService;
    this.saleOrderLineService = saleOrderLineService;
  }

  @Override
  public Map<String, Object> computeValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    HashMap<String, Object> map = new HashMap<>();
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

    if (CollectionUtils.isNotEmpty(saleOrderLine.getTaxLineSet())) {
      taxRate = taxService.getTotalTaxRate(saleOrderLine.getTaxLineSet());
    }

    if (!saleOrder.getInAti()) {
      exTaxTotal =
          this.computeAmount(
              saleOrderLine.getQty(), priceDiscounted, currencyScaleService.getScale(saleOrder));
      inTaxTotal =
          currencyScaleService.getScaledValue(
              saleOrder, exTaxTotal.add(exTaxTotal.multiply(taxRate)));
      companyExTaxTotal = this.getAmountInCompanyCurrency(exTaxTotal, saleOrder);
      companyInTaxTotal =
          currencyScaleService.getCompanyScaledValue(
              saleOrder, companyExTaxTotal.add(companyExTaxTotal.multiply(taxRate)));
    } else {
      inTaxTotal =
          this.computeAmount(
              saleOrderLine.getQty(), priceDiscounted, currencyScaleService.getScale(saleOrder));
      exTaxTotal =
          inTaxTotal.divide(
              taxRate.add(BigDecimal.ONE),
              currencyScaleService.getScale(saleOrder),
              RoundingMode.HALF_UP);
      companyInTaxTotal = this.getAmountInCompanyCurrency(inTaxTotal, saleOrder);
      companyExTaxTotal =
          companyInTaxTotal.divide(
              taxRate.add(BigDecimal.ONE),
              currencyScaleService.getCompanyScale(saleOrder),
              RoundingMode.HALF_UP);
    }

    if (saleOrderLine.getProduct() != null
        && ((BigDecimal)
                    productCompanyService.get(
                        saleOrderLine.getProduct(), "costPrice", saleOrder.getCompany()))
                .compareTo(BigDecimal.ZERO)
            != 0) {
      subTotalCostPrice =
          currencyScaleService.getCompanyScaledValue(
              saleOrder,
              ((BigDecimal)
                      productCompanyService.get(
                          saleOrderLine.getProduct(), "costPrice", saleOrder.getCompany()))
                  .multiply(saleOrderLine.getQty()));
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

  protected BigDecimal computeAmount(BigDecimal quantity, BigDecimal price, int scale) {

    BigDecimal amount = quantity.multiply(price).setScale(scale, RoundingMode.HALF_UP);

    logger.debug(
        "Computation of W.T. amount with a quantity of {} for {} : {}", quantity, price, amount);

    return amount;
  }

  @Override
  public BigDecimal computeDiscount(SaleOrderLine saleOrderLine, Boolean inAti) {

    BigDecimal price = inAti ? saleOrderLine.getInTaxPrice() : saleOrderLine.getPrice();

    return priceListService.computeDiscount(
        price, saleOrderLine.getDiscountTypeSelect(), saleOrderLine.getDiscountAmount());
  }

  @Override
  public BigDecimal getAmountInCompanyCurrency(BigDecimal exTaxTotal, SaleOrder saleOrder)
      throws AxelorException {

    return currencyScaleService.getCompanyScaledValue(
        saleOrder,
        currencyService.getAmountCurrencyConvertedAtDate(
            saleOrder.getCurrency(),
            saleOrder.getCompany().getCurrency(),
            exTaxTotal,
            saleOrder.getCreationDate()));
  }

  @Override
  public Map<String, Object> updateProductQty(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, BigDecimal oldQty, BigDecimal newQty)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    if (saleOrderLine.getTypeSelect() != SaleOrderLineRepository.TYPE_NORMAL) {
      return saleOrderLineMap;
    }
    saleOrderLineService.fillPriceFromPackLine(saleOrderLine, saleOrder);
    saleOrderLineMap.putAll(computeValues(saleOrder, saleOrderLine));
    return saleOrderLineMap;
  }
}

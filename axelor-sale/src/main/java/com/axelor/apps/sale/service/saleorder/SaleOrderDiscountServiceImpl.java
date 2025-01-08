package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SaleOrderDiscountServiceImpl implements SaleOrderDiscountService {

  protected final SaleOrderComputeService saleOrderComputeService;

  @Inject
  public SaleOrderDiscountServiceImpl(SaleOrderComputeService saleOrderComputeService) {
    this.saleOrderComputeService = saleOrderComputeService;
  }

  @Override
  public void applyGlobalDiscountOnLines(SaleOrder saleOrder) throws AxelorException {
    if (saleOrder == null
        || saleOrder.getSaleOrderLineList() == null
        || saleOrder.getSaleOrderLineList().isEmpty()) {
      return;
    }
    computePriceBeforeGlobalDiscount(saleOrder);
    switch (saleOrder.getDiscountTypeSelect()) {
      case PriceListLineRepository.AMOUNT_TYPE_PERCENT:
        applyPercentageGlobalDiscountOnLines(saleOrder);
        break;
      case PriceListLineRepository.AMOUNT_TYPE_FIXED:
        applyFixedGlobalDiscountOnLines(saleOrder);
        break;
    }
  }

  protected void computePriceBeforeGlobalDiscount(SaleOrder saleOrder) {
    saleOrder.setPriceBeforeGlobalDiscount(
        saleOrder.getSaleOrderLineList().stream()
            .map(saleOrderLine -> saleOrderLine.getPrice().multiply(saleOrderLine.getQty()))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO));
  }

  protected void applyPercentageGlobalDiscountOnLines(SaleOrder saleOrder) throws AxelorException {
    saleOrder.getSaleOrderLineList().stream()
        .filter(
            saleOrderLine ->
                saleOrderLine.getTypeSelect().equals(SaleOrderLineRepository.TYPE_NORMAL))
        .forEach(
            saleOrderLine -> {
              saleOrderLine.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_PERCENT);
              saleOrderLine.setDiscountAmount(saleOrder.getDiscountAmount());
            });
    adjustPercentageDiscountOnLastLine(saleOrder);
  }

  protected void applyFixedGlobalDiscountOnLines(SaleOrder saleOrder) throws AxelorException {
    saleOrder.getSaleOrderLineList().stream()
        .filter(
            saleOrderLine ->
                saleOrderLine.getTypeSelect().equals(SaleOrderLineRepository.TYPE_NORMAL))
        .forEach(
            saleOrderLine -> {
              saleOrderLine.setDiscountTypeSelect(saleOrder.getDiscountTypeSelect());
              saleOrderLine.setDiscountAmount(
                  saleOrderLine
                      .getPrice()
                      .divide(saleOrder.getPriceBeforeGlobalDiscount(), RoundingMode.HALF_UP)
                      .multiply(saleOrder.getDiscountAmount()));
            });
    adjustFixedDiscountOnLastLine(saleOrder);
  }

  protected void adjustPercentageDiscountOnLastLine(SaleOrder saleOrder) throws AxelorException {
    saleOrderComputeService.computeSaleOrder(saleOrder);
    BigDecimal priceDiscountedByLine = saleOrder.getExTaxTotal();
    BigDecimal priceDiscountedOnTotal =
        saleOrder
            .getPriceBeforeGlobalDiscount()
            .multiply(BigDecimal.valueOf(100).subtract(saleOrder.getDiscountAmount()))
            .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    if (priceDiscountedByLine.compareTo(priceDiscountedOnTotal) == 0) {
      return;
    }
    BigDecimal differenceInDiscount = priceDiscountedOnTotal.subtract(priceDiscountedByLine);

    SaleOrderLine lastLine =
        saleOrder.getSaleOrderLineList().get(saleOrder.getSaleOrderLineList().size() - 1);

    lastLine.setDiscountAmount(
        BigDecimal.ONE
            .subtract(
                lastLine
                    .getPriceDiscounted()
                    .add(differenceInDiscount)
                    .divide(lastLine.getPrice(), RoundingMode.HALF_UP))
            .multiply(BigDecimal.valueOf(100)));
  }

  protected void adjustFixedDiscountOnLastLine(SaleOrder saleOrder) throws AxelorException {
    saleOrderComputeService.computeSaleOrder(saleOrder);
    BigDecimal priceDiscountedByLine = saleOrder.getExTaxTotal();

    BigDecimal priceDiscountedOnTotal =
        saleOrder.getPriceBeforeGlobalDiscount().subtract(saleOrder.getDiscountAmount());
    if (priceDiscountedByLine.compareTo(priceDiscountedOnTotal) == 0) {
      return;
    }

    BigDecimal differenceInDiscount = priceDiscountedOnTotal.subtract(priceDiscountedByLine);
    SaleOrderLine lastLine =
        saleOrder.getSaleOrderLineList().get(saleOrder.getSaleOrderLineList().size() - 1);
    lastLine.setDiscountAmount(
        lastLine
            .getDiscountAmount()
            .subtract(differenceInDiscount.divide(lastLine.getQty(), RoundingMode.HALF_UP)));
  }
}

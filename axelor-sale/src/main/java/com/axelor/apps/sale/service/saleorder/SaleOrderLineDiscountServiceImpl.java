package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCategoryService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineDiscountServiceImpl implements SaleOrderLineDiscountService {

  protected PriceListService priceListService;
  protected ProductCategoryService productCategoryService;

  @Inject
  public SaleOrderLineDiscountServiceImpl(
      PriceListService priceListService, ProductCategoryService productCategoryService) {
    this.priceListService = priceListService;
    this.productCategoryService = productCategoryService;
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

  protected PriceListLine getPriceListLine(
      SaleOrderLine saleOrderLine, PriceList priceList, BigDecimal price) {

    return priceListService.getPriceListLine(
        saleOrderLine.getProduct(), saleOrderLine.getQty(), priceList, price);
  }
}

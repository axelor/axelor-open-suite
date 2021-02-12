package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.ProductCategoryServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductCategoryServiceSaleImpl extends ProductCategoryServiceImpl
    implements ProductCategorySaleService {

  protected SaleOrderLineRepository saleOrderLineRepository;

  @Inject
  public ProductCategoryServiceSaleImpl(
      ProductCategoryRepository productCategoryRepository,
      SaleOrderLineRepository saleOrderLineRepository) {
    super(productCategoryRepository);
    this.saleOrderLineRepository = saleOrderLineRepository;
  }

  @Override
  @Transactional
  public void updateSaleOrderLines(ProductCategory productCategory) throws AxelorException {
    List<ProductCategory> impactedProductCategories =
        fetchImpactedChildrenProductCategories(productCategory);
    impactedProductCategories.add(productCategory);
    saleOrderLineRepository
        .all()
        .filter(
            // fetch children
            "self.product.productCategory.id IN :productCategoryIds "
                + "AND self.saleOrder.statusSelect != :statusCompleted "
                + "AND self.saleOrder.statusSelect != :statusCanceled")
        .bind(
            "productCategoryIds",
            impactedProductCategories.stream()
                .map(ProductCategory::getId)
                .collect(Collectors.toList()))
        .bind("statusCompleted", SaleOrderRepository.STATUS_ORDER_COMPLETED)
        .bind("statusCanceled", SaleOrderRepository.STATUS_CANCELED)
        .fetchStream()
        .filter(saleOrderLine -> hasDiscountBecameTooHigh(productCategory, saleOrderLine))
        .forEach(saleOrderLine -> saleOrderLine.setDiscountsNeedReview(true));
  }

  /**
   * Fetch impacted children product categories on a category change. To find impacted product
   * categories, we fetch children of given product category. If these children do not have a max
   * discount, they are impacted and their own children can be impacted following the same pattern.
   *
   * @param productCategory a product category
   * @return the computed list of children impacted by a max discount change in given category.
   * @throws AxelorException if there is a configuration error
   */
  protected List<ProductCategory> fetchImpactedChildrenProductCategories(
      ProductCategory productCategory) throws AxelorException {
    // security in case of code error to avoid infinite loop
    int i = 0;
    List<ProductCategory> descendantsProductCategoryList = new ArrayList<>();
    if (productCategory.getId() == null) {
      // if product category is not saved, then it cannot have children
      return descendantsProductCategoryList;
    }
    // product categories with max discounts are not be impacted
    List<ProductCategory> childrenProductCategoryList =
        fetchChildrenWitNoMaxDiscount(productCategory);
    while (!childrenProductCategoryList.isEmpty() && i < MAX_ITERATION) {
      List<ProductCategory> nextChildrenProductCategoryList = new ArrayList<>();
      for (ProductCategory childProductCategory : childrenProductCategoryList) {
        if (descendantsProductCategoryList.contains(childProductCategory)
            || childProductCategory.equals(productCategory)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.PRODUCT_CATEGORY_CHILDREN_CIRCULAR_DEPENDENCY),
              childProductCategory.getCode());
        }
        descendantsProductCategoryList.add(childProductCategory);
        nextChildrenProductCategoryList.addAll(fetchChildrenWitNoMaxDiscount(childProductCategory));
      }
      childrenProductCategoryList.clear();
      childrenProductCategoryList.addAll(nextChildrenProductCategoryList);
      nextChildrenProductCategoryList.clear();
      i++;
    }
    return descendantsProductCategoryList;
  }

  protected List<ProductCategory> fetchChildrenWitNoMaxDiscount(ProductCategory productCategory) {
    return productCategoryRepository
        .all()
        .filter("self.parentProductCategory.id = :productCategoryId AND self.maxDiscount <= 0")
        .bind("productCategoryId", productCategory.getId())
        .fetch();
  }

  protected boolean hasDiscountBecameTooHigh(
      ProductCategory productCategory, SaleOrderLine saleOrderLine) {
    ProductCategory productCategoryIt = productCategory;
    BigDecimal maxDiscount = productCategory.getMaxDiscount();
    while (maxDiscount.signum() == 0 && productCategoryIt.getParentProductCategory() != null) {
      productCategoryIt = productCategory.getParentProductCategory();
      maxDiscount = productCategoryIt.getMaxDiscount();
    }
    if (maxDiscount.signum() == 0) {
      return false;
    }
    // compute discount percent in sale order line
    BigDecimal saleOrderLineDiscount;
    switch (saleOrderLine.getDiscountTypeSelect()) {
      case PriceListLineRepository.AMOUNT_TYPE_PERCENT:
        saleOrderLineDiscount = saleOrderLine.getDiscountAmount();
        break;
      case PriceListLineRepository.AMOUNT_TYPE_FIXED:
        saleOrderLineDiscount =
            saleOrderLine.getPrice().signum() != 0
                ? saleOrderLine
                    .getDiscountAmount()
                    .multiply(new BigDecimal("100"))
                    .divide(saleOrderLine.getPrice(), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        break;
      case PriceListLineRepository.AMOUNT_TYPE_NONE:
      default:
        saleOrderLineDiscount = BigDecimal.ZERO;
        break;
    }
    return saleOrderLineDiscount.compareTo(maxDiscount) > 0;
  }
}

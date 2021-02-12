package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductCategoryServiceImpl implements ProductCategoryService {

  protected ProductCategoryRepository productCategoryRepository;

  protected static final int MAX_ITERATION = 100;

  @Inject
  public ProductCategoryServiceImpl(ProductCategoryRepository productCategoryRepository) {
    this.productCategoryRepository = productCategoryRepository;
  }

  @Override
  public String computeDiscountMessage(ProductCategory productCategory) throws AxelorException {
    BigDecimal maxDiscount = productCategory.getMaxDiscount();
    if (maxDiscount.signum() <= 0) {
      // field is empty: no messages required
      return "";
    }
    List<ProductCategory> parentCategories = fetchParentCategoryList(productCategory);
    List<ProductCategory> childrenCategories = fetchChildrenCategoryList(productCategory);

    StringBuilder discountMessage = new StringBuilder();
    if (!parentCategories.isEmpty() || !childrenCategories.isEmpty()) {
      discountMessage.append(I18n.get("This maximal discount"));
      discountMessage.append(" ");
    }
    if (!parentCategories.isEmpty()) {
      discountMessage.append(I18n.get("will override discounts applied to parents categories"));
      discountMessage.append(" ");
      discountMessage.append(
          parentCategories.stream()
              .map(pc -> String.format("%s (%s%%)", pc.getCode(), pc.getMaxDiscount()))
              .collect(Collectors.joining(", ")));
      if (!childrenCategories.isEmpty()) {
        discountMessage.append(" ");
        discountMessage.append(I18n.get("and"));
        discountMessage.append(" ");
      }
    }
    if (!childrenCategories.isEmpty()) {
      discountMessage.append(I18n.get("will not be applied to following children categories"));
      discountMessage.append(" ");
      discountMessage.append(
          childrenCategories.stream()
              .map(pc -> String.format("%s (%s%%)", pc.getCode(), pc.getMaxDiscount()))
              .collect(Collectors.joining(", ")));
    }
    return discountMessage.toString();
  }

  @Override
  public Optional<BigDecimal> computeMaxDiscount(ProductCategory productCategory)
      throws AxelorException {
    if (productCategory.getMaxDiscount().signum() > 0) {
      return Optional.of(productCategory.getMaxDiscount());
    } else {
      // this works because the returned list is ordered by parent category.
      return fetchParentCategoryList(productCategoryRepository.find(productCategory.getId()))
          .stream()
          .map(ProductCategory::getMaxDiscount)
          .findFirst();
    }
  }

  /**
   * Find parent of given category, and recursively parents of found parents.
   *
   * @param productCategory a product category
   * @return all parents of the category
   */
  protected List<ProductCategory> fetchParentCategoryList(ProductCategory productCategory)
      throws AxelorException {
    // security in case of code error to avoid infinite loop
    int i = 0;
    List<ProductCategory> parentProductCategoryList = new ArrayList<>();
    if (productCategory.getId() != null) {
      productCategory = productCategoryRepository.find(productCategory.getId());
    }
    ProductCategory parentProductCategory = productCategory.getParentProductCategory();
    while (parentProductCategory != null && i < MAX_ITERATION) {
      if (parentProductCategoryList.contains(parentProductCategory)
          || parentProductCategory.equals(productCategory)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.PRODUCT_CATEGORY_PARENTS_CIRCULAR_DEPENDENCY),
            parentProductCategory.getCode());
      }
      parentProductCategoryList.add(parentProductCategory);
      parentProductCategory = parentProductCategory.getParentProductCategory();
      i++;
    }
    return parentProductCategoryList.stream()
        .filter(pc -> pc.getMaxDiscount().signum() > 0)
        .collect(Collectors.toList());
  }

  /**
   * Find child of given category, and recursively children of found children.
   *
   * @param productCategory a product category
   * @return all parents of the category
   */
  protected List<ProductCategory> fetchChildrenCategoryList(ProductCategory productCategory)
      throws AxelorException {
    // security in case of code error to avoid infinite loop
    int i = 0;
    List<ProductCategory> descendantsProductCategoryList = new ArrayList<>();
    if (productCategory.getId() == null) {
      // if product category is not saved, then it cannot have children
      return descendantsProductCategoryList;
    }
    List<ProductCategory> childrenProductCategoryList = fetchChildren(productCategory);
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
        nextChildrenProductCategoryList.addAll(fetchChildren(childProductCategory));
      }
      childrenProductCategoryList.clear();
      childrenProductCategoryList.addAll(nextChildrenProductCategoryList);
      nextChildrenProductCategoryList.clear();
      i++;
    }
    return descendantsProductCategoryList.stream()
        .filter(pc -> pc.getMaxDiscount().signum() > 0)
        .collect(Collectors.toList());
  }

  protected List<ProductCategory> fetchChildren(ProductCategory productCategory) {
    return productCategoryRepository
        .all()
        .filter("self.parentProductCategory.id = :productCategoryId")
        .bind("productCategoryId", productCategory.getId())
        .fetch();
  }
}

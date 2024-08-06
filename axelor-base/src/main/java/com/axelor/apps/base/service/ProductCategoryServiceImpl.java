/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    List<ProductCategory> parentCategories =
        fetchParentCategoryListWithMaxDiscount(productCategory);
    List<ProductCategory> childrenCategories =
        fetchChildrenCategoryListWithMaxDiscount(productCategory);

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
      return fetchParentCategoryListWithMaxDiscount(
              productCategoryRepository.find(productCategory.getId()))
          .stream()
          .map(ProductCategory::getMaxDiscount)
          .findFirst();
    }
  }

  @Override
  public List<ProductCategory> fetchParentCategoryList(ProductCategory productCategory)
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
            I18n.get(BaseExceptionMessage.PRODUCT_CATEGORY_PARENTS_CIRCULAR_DEPENDENCY),
            parentProductCategory.getCode());
      }
      parentProductCategoryList.add(parentProductCategory);
      parentProductCategory = parentProductCategory.getParentProductCategory();
      i++;
    }
    return parentProductCategoryList;
  }

  @Override
  public List<ProductCategory> fetchChildrenCategoryList(ProductCategory productCategory)
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
              I18n.get(BaseExceptionMessage.PRODUCT_CATEGORY_CHILDREN_CIRCULAR_DEPENDENCY),
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
    return descendantsProductCategoryList;
  }

  /**
   * Find parent of given category, and recursively parents of found parents that have a max
   * discount.
   *
   * @param productCategory a product category
   * @return filtered parents of the category
   */
  protected List<ProductCategory> fetchParentCategoryListWithMaxDiscount(
      ProductCategory productCategory) throws AxelorException {
    return fetchParentCategoryList(productCategory).stream()
        .filter(pc -> pc.getMaxDiscount().signum() > 0)
        .collect(Collectors.toList());
  }

  /**
   * Find child of given category, and recursively children of found children that have a max
   * discount.
   *
   * @param productCategory a product category
   * @return filtered children of the category
   */
  protected List<ProductCategory> fetchChildrenCategoryListWithMaxDiscount(
      ProductCategory productCategory) throws AxelorException {
    return fetchChildrenCategoryList(productCategory).stream()
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

  @Override
  public BigDecimal getGrowthCoeff(ProductCategory productCategory) {
    Objects.requireNonNull(productCategory);
    return getGrowthCoeffBis(productCategory, 0);
  }

  protected BigDecimal getGrowthCoeffBis(ProductCategory productCategory, int i) {
    if (productCategory.getGrowthCoef().compareTo(BigDecimal.ONE) != 0
        || productCategory.getParentProductCategory() == null
        || i == MAX_ITERATION) {
      return productCategory.getGrowthCoef();
    }

    return getGrowthCoeffBis(productCategory.getParentProductCategory(), ++i);
  }
}

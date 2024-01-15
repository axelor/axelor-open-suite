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
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class ProductCategoryDomainCreatorServiceImpl
    implements ProductCategoryDomainCreatorService {

  public ProductCategoryServiceImpl productCategoryService;
  public ProductCategoryRepository productCategoryRepository;

  @Inject
  public ProductCategoryDomainCreatorServiceImpl(
      ProductCategoryServiceImpl productCategoryService,
      ProductCategoryRepository productCategoryRepository) {
    this.productCategoryService = productCategoryService;
    this.productCategoryRepository = productCategoryRepository;
  }

  @Override
  public String createProductCategoryDomainFilteringChildren(ProductCategory productCategory)
      throws AxelorException {

    if (productCategory.getId() == null) {
      return null;
    } else {
      productCategory = productCategoryRepository.find(productCategory.getId());
      List<ProductCategory> productCategoryList =
          productCategoryService.fetchChildrenCategoryList(productCategory);
      productCategoryList.add(productCategory);
      String childrenIdList =
          productCategoryList.stream()
              .map(ProductCategory::getId)
              .map(Object::toString)
              .collect(Collectors.joining(","));
      String domain = "self.id NOT IN (" + childrenIdList + ")";
      return domain;
    }
  }
}

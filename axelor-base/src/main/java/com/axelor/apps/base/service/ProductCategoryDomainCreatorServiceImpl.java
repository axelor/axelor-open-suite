package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.exception.AxelorException;
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

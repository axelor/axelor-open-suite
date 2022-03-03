package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ProductCategory;
import com.axelor.exception.AxelorException;

public interface ProductCategoryDomainCreatorService {
  /**
   * Find child of given category, and recursively children of found children.
   * Then create a domain to filter the children from the parent category list.
   * @param productCategory
   * @return
   * @throws AxelorException
   */
  String createProductCategoryDomainFilteringChildren(ProductCategory productCategory)
      throws AxelorException;
}

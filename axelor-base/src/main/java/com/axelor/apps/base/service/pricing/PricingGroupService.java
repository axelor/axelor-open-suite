package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import java.util.Map;

public interface PricingGroupService {
  String computeFormulaField(Product product, ProductCategory productCategory);

  Map<String, Object> clearFieldsRelatedToFormula(Pricing pricing);
}

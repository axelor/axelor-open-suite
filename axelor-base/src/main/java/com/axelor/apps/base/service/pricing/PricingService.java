package com.axelor.apps.base.service.pricing;

import java.util.List;
import java.util.Optional;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;

public interface PricingService {

  /**
   * This method will get a random pricing from pricings filtered with company, product,
   * productCategory, modelName, previousPricing.
   *
   * @param company {@link Company}: can be null
   * @param product {@link Product}: can be null
   * @param productCategory {@link ProductCategory}: can be null
   * @param modelName {@link String}: can be null or empty
   * @param previousPricing {@link Pricing}: can be null
   * @return a {@link Optional} of Pricing.
   */
  Optional<Pricing> getRandomPricing(
      Company company,
      Product product,
      ProductCategory productCategory,
      String modelName,
      Pricing previousPricing);

  /**
   * This method will get all pricings filtered with company, product, productCategory, modelName,
   * previousPricing.
   *
   * @param company {@link Company}: can be null
   * @param product {@link Product}: can be null
   * @param productCategory {@link ProductCategory}: can be null
   * @param modelName {@link String}: can be null or empty
   * @param previousPricing {@link Pricing}: can be null
   * @return a {@link Optional} of Pricing.
   */
  List<Pricing> getPricings(
      Company company,
      Product product,
      ProductCategory productCategory,
      String modelName,
      Pricing previousPricing);

}

package com.axelor.apps.sale.service.pricing;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import java.util.Optional;

public interface PricingService {

  void computePricingScale(SaleOrder saleOrder, SaleOrderLine orderLine) throws AxelorException;

  Query<Pricing> getPricing(
      Product product,
      ProductCategory productCategory,
      Company company,
      String modelName,
      Pricing parentPricing);

  /**
   * Returns an optional containing the default pricing of saleOrderLine, or an empty optional if no
   * one was found.
   */
  Optional<Pricing> getDefaultPricing(SaleOrder saleOrder, SaleOrderLine saleOrderLine);

  /**
   * Methods that get the pricingLine that matches with in pricing.
   *
   * @param saleOrder
   * @param saleOrderLine
   * @param pricing
   * @throws AxelorException
   */
  Optional<PricingLine> getPricingLine(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Pricing pricing) throws AxelorException;
}

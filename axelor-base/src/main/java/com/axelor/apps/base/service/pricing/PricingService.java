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
package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import java.util.List;
import java.util.Optional;

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

  public void historizePricing(Pricing pricing) throws AxelorException;

  public void checkDates(Pricing pricing) throws AxelorException;

  public Pricing recoverPricing(Pricing pricing, Boolean isHistorizeCurrentPricing)
      throws AxelorException;

  public void historizeCurrentPricing(Pricing currentPricing) throws AxelorException;
}

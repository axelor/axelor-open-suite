/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.db.Model;
import java.util.List;
import java.util.Optional;

public interface PricingService {

  /**
   * This method will get a random pricing from pricings filtered with company, product,
   * productCategory, modelName, previousPricing.
   *
   * @param company {@link Company}: can be null
   * @param model {@link Model}
   * @param pricing {@link Pricing}: can be null
   * @param typeSelect : can be null
   * @return a {@link Optional} of Pricing.c
   */
  Optional<Pricing> getRandomPricing(
      Company company, Model model, Pricing pricing, String typeSelect);

  Optional<Pricing> getRootPricingForNextPricings(Company company, Model model, String typeSelect);

  /**
   * This method will get all pricings filtered with company, product, productCategory, modelName,
   * previousPricing.
   *
   * @param company {@link Company}: can be null
   * @param model {@link Model}
   * @param pricing {@link Pricing}: can be null
   * @param typeSelect: can be null
   * @return a {@link Optional} of Pricing.
   */
  List<Pricing> getPricings(Company company, Model model, Pricing pricing, String typeSelect);

  List<Pricing> getAllPricings(Company company, Model model, String typeSelect);

  List<Pricing> appendFormulaFilter(List<Pricing> pricings, Model model);

  public void historizePricing(Pricing pricing) throws AxelorException;

  public void checkDates(Pricing pricing) throws AxelorException;

  public Pricing recoverPricing(Pricing pricing, Boolean isHistorizeCurrentPricing)
      throws AxelorException;

  public void historizeCurrentPricing(Pricing currentPricing) throws AxelorException;
}

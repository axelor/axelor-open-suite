/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.supplychain.db.DepreciationRateConfig;
import java.math.BigDecimal;

/**
 * Resolves and combines every applicable {@link DepreciationRateConfig} for a product across its
 * configured dimensions (product, product category chain, product family, stock rotation category)
 * using a signed multiplicative model.
 */
public interface DepRateAggregationService {

  /**
   * Aggregate every applicable config into a single set of rates. Per-config multipliers are
   * combined as {@code (1 - r/100)} for depreciation and {@code (1 + r/100)} for valorization; the
   * net signed result drives {@link AggregatedRates#typeSelect()}, with bounds taken as the
   * intersection {@code max(mins)} / {@code min(maxes)} of configs that define them.
   */
  AggregatedRates aggregate(Product product) throws AxelorException;

  /** Immutable result of {@link #aggregate(Product)}. */
  record AggregatedRates(
      BigDecimal minRate, BigDecimal maxRate, BigDecimal costToApply, int typeSelect) {}
}

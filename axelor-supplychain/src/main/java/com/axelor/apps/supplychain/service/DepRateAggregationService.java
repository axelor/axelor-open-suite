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
import java.util.List;

/**
 * Resolves and combines every applicable {@link DepreciationRateConfig} for a product across its
 * configured dimensions (product, product category chain, product family, stock rotation category)
 * using a signed multiplicative model.
 */
public interface DepRateAggregationService {

  /**
   * Loads every persisted rate configuration. Callers looping over many products should fetch once
   * and reuse the list across {@link #aggregate(Product, List)} calls instead of re-querying the
   * configuration table for each product.
   */
  List<DepreciationRateConfig> fetchConfigs();

  /**
   * Aggregate every config applicable to the product into a single set of rates. Per-config
   * multipliers are combined as {@code (1 - r/100)} for depreciation and {@code (1 + r/100)} for
   * valorization; the net signed result drives {@link AggregatedRates#typeSelect()}, with bounds
   * taken as the intersection {@code max(mins)} / {@code min(maxes)} of configs that define them.
   *
   * <p>When {@code takeInAccountSubCategories} is true, a config set on a parent category applies
   * to the products of its sub-categories; otherwise the product category must match exactly.
   *
   * <p>The configs must be attached to the current persistence context: their dimensions are lazy,
   * so re-fetch them after any {@code JPA.clear()}.
   */
  AggregatedRates aggregate(
      Product product, List<DepreciationRateConfig> configs, boolean takeInAccountSubCategories)
      throws AxelorException;

  /** Immutable result of {@link #aggregate(Product, List, boolean)}. */
  record AggregatedRates(
      BigDecimal minRate, BigDecimal maxRate, BigDecimal costToApply, int typeSelect) {}
}

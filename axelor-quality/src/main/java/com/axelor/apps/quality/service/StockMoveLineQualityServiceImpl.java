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
package com.axelor.apps.quality.service;

import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.apps.stock.db.StockMoveLine;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class StockMoveLineQualityServiceImpl implements StockMoveLineQualityService {

  protected final QualityImprovementRepository qualityImprovementRepository;

  @Inject
  public StockMoveLineQualityServiceImpl(
      QualityImprovementRepository qualityImprovementRepository) {
    this.qualityImprovementRepository = qualityImprovementRepository;
  }

  @Override
  public List<String> getOpenQualityImprovementSequences(StockMoveLine stockMoveLine) {
    if (stockMoveLine == null || stockMoveLine.getId() == null) {
      return List.of();
    }
    return qualityImprovementRepository
        .findOpenByStockMoveLineId(stockMoveLine.getId())
        .fetch()
        .stream()
        .map(QualityImprovement::getSequence)
        .collect(Collectors.toList());
  }
}

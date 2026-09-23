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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.db.Query;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockMoveLineQualityServiceImplTest {

  private QualityImprovementRepository repository;
  private StockMoveLineQualityService service;

  @BeforeEach
  void setUp() {
    repository = mock(QualityImprovementRepository.class);
    service = new StockMoveLineQualityServiceImpl(repository);
  }

  @Test
  void openFilesOfALineAreListedBySequence() {
    StockMoveLine stockMoveLine = new StockMoveLine();
    stockMoveLine.setId(5L);
    Query<QualityImprovement> query = mock(Query.class);
    when(repository.findOpenByStockMoveLineId(5L)).thenReturn(query);
    when(query.fetch()).thenReturn(List.of(qi("QI1"), qi("QI2")));

    assertEquals(List.of("QI1", "QI2"), service.getOpenQualityImprovementSequences(stockMoveLine));
  }

  @Test
  void unsavedLineHasNoOpenFile() {
    assertTrue(service.getOpenQualityImprovementSequences(new StockMoveLine()).isEmpty());
    assertTrue(service.getOpenQualityImprovementSequences(null).isEmpty());
  }

  private QualityImprovement qi(String sequence) {
    QualityImprovement qi = new QualityImprovement();
    qi.setSequence(sequence);
    return qi;
  }
}

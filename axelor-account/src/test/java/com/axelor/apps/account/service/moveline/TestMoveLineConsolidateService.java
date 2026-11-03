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
package com.axelor.apps.account.service.moveline;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestMoveLineConsolidateService {

  private MoveLineConsolidateServiceImpl moveLineConsolidateService;
  private Account account;
  private AnalyticAxis analyticAxis;
  private AnalyticAccount analyticAccountA;
  private AnalyticAccount analyticAccountB;

  @BeforeEach
  void setUp() {
    moveLineConsolidateService =
        new MoveLineConsolidateServiceImpl(mock(MoveLineToolService.class));
    account = new Account();
    analyticAxis = new AnalyticAxis();
    analyticAccountA = new AnalyticAccount();
    analyticAccountB = new AnalyticAccount();
  }

  @Test
  void testSameAnalyticIsConsolidated() {
    MoveLine moveLine1 = createMoveLine(createAnalyticMoveLine(analyticAccountA, "100"));
    MoveLine moveLine2 = createMoveLine(createAnalyticMoveLine(analyticAccountA, "100"));

    assertSame(moveLine1, findConsolidateMoveLine(moveLine1, moveLine2));
  }

  @Test
  void testDifferentAnalyticSizeIsNotConsolidated() {
    MoveLine moveLine1 = createMoveLine(createAnalyticMoveLine(analyticAccountA, "100"));
    MoveLine moveLine2 =
        createMoveLine(
            createAnalyticMoveLine(analyticAccountA, "50"),
            createAnalyticMoveLine(analyticAccountB, "50"));

    assertNull(findConsolidateMoveLine(moveLine1, moveLine2));
    assertNull(findConsolidateMoveLine(moveLine2, moveLine1));
  }

  protected MoveLine findConsolidateMoveLine(MoveLine existingMoveLine, MoveLine moveLine) {
    List<Object> keys = List.of(account);
    Map<List<Object>, List<MoveLine>> map = new HashMap<>();
    map.put(keys, new ArrayList<>(List.of(existingMoveLine)));
    return moveLineConsolidateService.findConsolidateMoveLine(map, moveLine, keys);
  }

  protected MoveLine createMoveLine(AnalyticMoveLine... analyticMoveLines) {
    MoveLine moveLine = new MoveLine();
    moveLine.setAccount(account);
    moveLine.setAnalyticMoveLineList(new ArrayList<>(List.of(analyticMoveLines)));
    return moveLine;
  }

  protected AnalyticMoveLine createAnalyticMoveLine(
      AnalyticAccount analyticAccount, String percentage) {
    AnalyticMoveLine analyticMoveLine = new AnalyticMoveLine();
    analyticMoveLine.setAnalyticAxis(analyticAxis);
    analyticMoveLine.setAnalyticAccount(analyticAccount);
    analyticMoveLine.setAccount(account);
    analyticMoveLine.setPercentage(new BigDecimal(percentage));
    return analyticMoveLine;
  }
}

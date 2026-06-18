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
package com.axelor.apps.account.service.move.template;

import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.account.service.analytic.AnalyticLineComputeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import jakarta.inject.Inject;
import java.time.LocalDate;
import org.apache.commons.collections.CollectionUtils;

public class MoveTemplateLineComputeAnalyticServiceImpl
    implements MoveTemplateLineComputeAnalyticService {

  protected AnalyticLineComputeService analyticLineComputeService;

  @Inject
  public MoveTemplateLineComputeAnalyticServiceImpl(
      AnalyticLineComputeService analyticLineComputeService) {
    this.analyticLineComputeService = analyticLineComputeService;
  }

  @Override
  public MoveTemplateLine computeAnalyticDistribution(MoveTemplateLine moveTemplateLine) {
    analyticLineComputeService.computeAnalyticDistribution(
        moveTemplateLine, moveTemplateLine.getLineAmount(), LocalDate.now());
    return moveTemplateLine;
  }

  @Override
  public MoveTemplateLine createAnalyticDistributionWithTemplate(
      MoveTemplateLine moveTemplateLine) {
    analyticLineComputeService.createAnalyticDistributionWithTemplate(
        moveTemplateLine, moveTemplateLine.getLineAmount(), LocalDate.now());
    return moveTemplateLine;
  }

  @Override
  public MoveTemplateLine analyzeMoveTemplateLine(
      MoveTemplateLine moveTemplateLine, Company company) throws AxelorException {
    analyticLineComputeService.analyzeAnalyticLine(
        moveTemplateLine, company, moveTemplateLine.getLineAmount(), LocalDate.now());
    return moveTemplateLine;
  }

  @Override
  public MoveTemplateLine clearAnalyticAccounting(MoveTemplateLine moveTemplateLine) {
    clearAnalyticMoveLineList(moveTemplateLine);
    analyticLineComputeService.clearAnalyticAccounting(moveTemplateLine);
    return moveTemplateLine;
  }

  @Override
  public MoveTemplateLine clearAnalyticAccountingIfEmpty(MoveTemplateLine moveTemplateLine) {
    if (moveTemplateLine.getAxis1AnalyticAccount() == null
        && moveTemplateLine.getAxis2AnalyticAccount() == null
        && moveTemplateLine.getAxis3AnalyticAccount() == null
        && moveTemplateLine.getAxis4AnalyticAccount() == null
        && moveTemplateLine.getAxis5AnalyticAccount() == null) {
      clearAnalyticMoveLineList(moveTemplateLine);
    }
    analyticLineComputeService.clearAnalyticAccountingIfEmpty(moveTemplateLine);
    return moveTemplateLine;
  }

  protected void clearAnalyticMoveLineList(MoveTemplateLine moveTemplateLine) {
    if (!CollectionUtils.isEmpty(moveTemplateLine.getAnalyticMoveLineList())) {
      moveTemplateLine
          .getAnalyticMoveLineList()
          .forEach(analyticMoveLine -> analyticMoveLine.setMoveTemplateLine(null));
    }
  }
}

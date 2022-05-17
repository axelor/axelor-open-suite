/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.exception.service.TraceBackService;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;

public class AccountAnalyticDistributionTemplateRepository
    extends AnalyticDistributionTemplateRepository {

  @Override
  public AnalyticDistributionTemplate save(
      AnalyticDistributionTemplate analyticDistributionTemplate) {
    try {
      if (analyticDistributionTemplate.getId() == null) {
        return super.save(analyticDistributionTemplate);
      }
      if (analyticDistributionTemplate.getAnalyticDistributionLineList().size() == 1) {
        analyticDistributionTemplate
            .getAnalyticDistributionLineList()
            .get(0)
            .setPercentage(new BigDecimal(100));
      }
      return super.save(analyticDistributionTemplate);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}

/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.cash.management.db.repo;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.cash.management.db.ForecastRecap;
import com.axelor.apps.cash.management.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;

public class CashManagementForecastRecapRepository extends ForecastRecapRepository {

  @Override
  public ForecastRecap save(ForecastRecap entity) {

    try {

      if (entity.getForecastRecapSeq() == null) {
        Company company = entity.getCompany();

        String sequence =
            Beans.get(SequenceService.class)
                .getSequenceNumber(SequenceRepository.FORECAST_RECAP_SEQUENCE, company);

        if (sequence == null) {
          throw new AxelorException(
              company,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.FORCAST_RECAP_SEQUENCE_ERROR),
              company.getName());

        } else {
          entity.setForecastRecapSeq(sequence);
        }
      }

      return super.save(entity);
    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }

  @Override
  public ForecastRecap copy(ForecastRecap entity, boolean deep) {

    ForecastRecap copy = super.copy(entity, deep);

    copy.clearForecastRecapLineList();
    copy.setCalculationDate(null);
    copy.setForecastRecapSeq(null);
    copy.setEndingBalance(BigDecimal.ZERO);
    return copy;
  }
}

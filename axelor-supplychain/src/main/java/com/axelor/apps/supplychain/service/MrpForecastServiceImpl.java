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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.supplychain.db.MrpForecast;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class MrpForecastServiceImpl implements MrpForecastService {

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void confirm(MrpForecast mrpForecast) throws AxelorException {
    if (mrpForecast.getStatusSelect() == null
        || mrpForecast.getStatusSelect() != MrpForecastRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.MRP_FORECAST_CONFIRM_WRONG_STATUS));
    }
    mrpForecast.setStatusSelect(MrpForecastRepository.STATUS_CONFIRMED);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void cancel(MrpForecast mrpForecast) throws AxelorException {
    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(MrpForecastRepository.STATUS_DRAFT);
    authorizedStatus.add(MrpForecastRepository.STATUS_CONFIRMED);
    if (mrpForecast.getStatusSelect() == null
        || !authorizedStatus.contains(mrpForecast.getStatusSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.MRP_FORECAST_CANCEL_WRONG_STATUS));
    }
    mrpForecast.setStatusSelect(MrpForecastRepository.STATUS_CANCELLED);
  }
}

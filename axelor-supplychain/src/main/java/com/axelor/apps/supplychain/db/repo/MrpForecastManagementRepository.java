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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.supplychain.db.MrpForecast;

public class MrpForecastManagementRepository extends MrpForecastRepository {

  @Override
  public MrpForecast copy(MrpForecast entity, boolean deep) {

    MrpForecast copy = super.copy(entity, deep);
    copy.setStatusSelect(MrpForecastRepository.STATUS_DRAFT);
    return copy;
  }
}

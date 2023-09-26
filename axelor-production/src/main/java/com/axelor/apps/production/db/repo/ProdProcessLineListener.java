/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class ProdProcessLineListener {

  @PrePersist
  @PreUpdate
  public void convertDurations(ProdProcessLine prodProcessLine) {
    if (Beans.get(AppProductionService.class).getAppProduction().getIsInputInHundredthHours()) {
      prodProcessLine.setDurationPerCycle(
          prodProcessLine
              .getDurationPerCycleDecimal()
              .multiply(BigDecimal.valueOf(3600))
              .longValue());
      prodProcessLine.setHumanDuration(
          prodProcessLine.getHumanDurationDecimal().multiply(BigDecimal.valueOf(3600)).longValue());
    } else {
      prodProcessLine.setDurationPerCycleDecimal(
          BigDecimal.valueOf(prodProcessLine.getDurationPerCycle())
              .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP));
      prodProcessLine.setHumanDurationDecimal(
          BigDecimal.valueOf(prodProcessLine.getHumanDuration())
              .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP));
    }
  }
}

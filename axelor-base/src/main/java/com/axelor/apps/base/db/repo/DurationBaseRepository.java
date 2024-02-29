/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import javax.persistence.PersistenceException;

public class DurationBaseRepository extends DurationRepository {

  @Override
  public Duration save(Duration duration) {
    try {

      duration.setName(this.computeName(duration.getTypeSelect(), duration.getValue()));

      return super.save(duration);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  public String computeName(int typeSelect, int value) {

    String name = "";

    switch (typeSelect) {
      case TYPE_MONTH:
        name += "month";
        break;

      case TYPE_DAY:
        name += "day";
        break;

      default:
        break;
    }

    if (value > 1) {
      name += "s";
    }

    return value + " " + I18n.get(name);
  }
}

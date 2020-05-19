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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Duration;
import com.axelor.i18n.I18n;
import javax.persistence.PersistenceException;

public class DurationBaseRepository extends DurationRepository {

  @Override
  public Duration save(Duration duration) {
    try {

      duration.setName(this.computeName(duration.getTypeSelect(), duration.getValue()));

      return super.save(duration);
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
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

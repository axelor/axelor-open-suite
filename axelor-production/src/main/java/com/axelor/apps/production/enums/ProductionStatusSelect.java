/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.enums;

import com.axelor.db.ValueEnum;
import java.util.Objects;

public enum ProductionStatusSelect implements ValueEnum<String> {
  PRODUCTION_STATUS_FINISHED("FINISHED"),
  PRODUCTION_STATUS_STANDBY("STANDBY"),
  PRODUCTION_STATUS_IN_PROGRESS("IN_PROGRESS"),
  PRODUCTION_STATUS_PLANNED("PLANNED"),
  PRODUCTION_STATUS_CANCELED("CANCELED"),
  PRODUCTION_STATUS_DRAFT("DRAFT");

  private final String value;

  ProductionStatusSelect(String value) {
    this.value = Objects.requireNonNull(value);
  }

  @Override
  public String getValue() {
    return value;
  }
}

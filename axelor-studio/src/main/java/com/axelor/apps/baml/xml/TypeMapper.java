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
package com.axelor.apps.baml.xml;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class TypeMapper {

  public static Class<?> getType(String simpleType) {

    switch (simpleType) {
      case "integer":
        return Integer.class;
      case "boolean":
        return Boolean.class;
      case "string":
        return String.class;
      case "long":
        return Long.class;
      case "decimal":
        return BigDecimal.class;
      case "date":
        return LocalDate.class;
      case "datetime":
        return LocalDateTime.class;
      case "zdatetime":
        return ZonedDateTime.class;
      default:
        break;
    }

    return null;
  }
}

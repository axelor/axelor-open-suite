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
package com.axelor.apps.base.service.indicator.utils;

import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.util.Optional;

public final class ResultTransformHelper {

  private ResultTransformHelper() {}

  /**
   * Safely converts a result object into BigDecimal.
   *
   * @param result the raw result object (may be null, Number, String, BigDecimal)
   * @return an Optional containing BigDecimal if conversion succeeds, or empty if result is null
   */
  public static Optional<BigDecimal> toBigDecimal(Object result) {
    if (result == null) {
      return Optional.empty();
    }

    if (result instanceof BigDecimal) {
      return Optional.of((BigDecimal) result);
    }

    if (result instanceof Number) {
      return Optional.of(BigDecimal.valueOf(((Number) result).doubleValue()));
    }

    if (result instanceof String) {
      try {
        return Optional.of(new BigDecimal((String) result));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            String.format(I18n.get("Expression returned a non-numeric String: %s"), result), e);
      }
    }

    throw new IllegalArgumentException(
        String.format(I18n.get("Unsupported result type: %s"), result.getClass().getName()));
  }
}

/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.partner.registrationnumber;

import java.util.List;

public class RegistrationCodeHelper {

  private static final List<String> SEPARATORS = List.of(" ", ".", "/", "-");

  private RegistrationCodeHelper() {}

  public static String normalizeRegistrationCode(String registrationCode) {
    if (registrationCode == null) {
      return "";
    }
    for (String separator : SEPARATORS) {
      registrationCode = registrationCode.replace(separator, "");
    }
    return registrationCode;
  }

  public static String getNormalizedRegistrationCodeExpression(String fieldName) {
    String expression = fieldName;
    for (String separator : SEPARATORS) {
      expression = String.format("REPLACE(%s, '%s', '')", expression, separator);
    }
    return expression;
  }
}

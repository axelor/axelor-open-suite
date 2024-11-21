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
package com.axelor.apps.businessproject.rest.dto;

import java.math.BigDecimal;

public class ProjectReportingIndicatorResponse {

  protected String title;
  protected BigDecimal value;
  protected String unit;

  public ProjectReportingIndicatorResponse(String title, BigDecimal value, String unit) {
    this.title = title;
    this.value = value;
    this.unit = unit;
  }

  public String getTitle() {
    return title;
  }

  public BigDecimal getValue() {
    return value;
  }

  public String getUnit() {
    return unit;
  }
}

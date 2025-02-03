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
package com.axelor.apps.hr.rest.dto;

import com.axelor.utils.api.RequestPostStructure;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

public class ProjectUnitConversionPutRequest extends RequestPostStructure {
  @NotNull protected Long startingUnitId;
  @NotNull protected BigDecimal startingValue;
  @NotNull protected Long destinationUnitId;

  public Long getStartingUnitId() {
    return startingUnitId;
  }

  public void setStartingUnitId(Long startingUnitId) {
    this.startingUnitId = startingUnitId;
  }

  public BigDecimal getStartingValue() {
    return startingValue;
  }

  public void setStartingValue(BigDecimal startingValue) {
    this.startingValue = startingValue;
  }

  public Long getDestinationUnitId() {
    return destinationUnitId;
  }

  public void setDestinationUnitId(Long destinationUnitId) {
    this.destinationUnitId = destinationUnitId;
  }
}

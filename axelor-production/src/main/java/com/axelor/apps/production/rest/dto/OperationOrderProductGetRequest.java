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
package com.axelor.apps.production.rest.dto;

import com.axelor.apps.production.db.OperationOrder;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OperationOrderProductGetRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long operationOrderId;

  @NotNull
  @Min(0)
  private Integer operationOrderVersion;

  public Long getOperationOrderId() {
    return operationOrderId;
  }

  public void setOperationOrderId(Long operationOrderId) {
    this.operationOrderId = operationOrderId;
  }

  public Integer getOperationOrderVersion() {
    return operationOrderVersion;
  }

  public void setOperationOrderVersion(Integer operationOrderVersion) {
    this.operationOrderVersion = operationOrderVersion;
  }

  public OperationOrder fetchOperationOrder() {
    return ObjectFinder.find(OperationOrder.class, operationOrderId, operationOrderVersion);
  }
}

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
package com.axelor.apps.businessproduction.rest.dto;

import com.axelor.apps.hr.rest.dto.TimesheetLinePutRequest;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.utils.api.ObjectFinder;
import javax.validation.constraints.Min;

public class TimesheetLineBusinessPutRequest extends TimesheetLinePutRequest {

  @Min(0)
  private Long manufOrderId;

  @Min(0)
  private Long operationOrderId;

  public Long getManufOrderId() {
    return manufOrderId;
  }

  public void setManufOrderId(Long manufOrderId) {
    this.manufOrderId = manufOrderId;
  }

  public Long getOperationOrderId() {
    return operationOrderId;
  }

  public void setOperationOrderId(Long operationOrderId) {
    this.operationOrderId = operationOrderId;
  }

  public ManufOrder fetchManufOrder() {
    if (manufOrderId == null || manufOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(ManufOrder.class, manufOrderId, ObjectFinder.NO_VERSION);
  }

  public OperationOrder fetchOperationOrder() {
    if (operationOrderId == null || operationOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(OperationOrder.class, operationOrderId, ObjectFinder.NO_VERSION);
  }
}

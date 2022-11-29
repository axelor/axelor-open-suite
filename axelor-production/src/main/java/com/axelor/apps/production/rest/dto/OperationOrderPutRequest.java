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
package com.axelor.apps.production.rest.dto;

import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.tool.api.RequestStructure;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class OperationOrderPutRequest extends RequestStructure {

  @NotNull
  @Min(OperationOrderRepository.STATUS_DRAFT)
  @Max(OperationOrderRepository.STATUS_FINISHED)
  private Integer status;

  public OperationOrderPutRequest() {}

  public int getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }
}

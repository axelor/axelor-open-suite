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
package com.axelor.apps.production.rest.dto;

import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.utils.api.RequestStructure;
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

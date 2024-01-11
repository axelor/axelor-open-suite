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

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ManufOrderProductGetRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long manufOrderId;

  @NotNull
  @Min(0)
  private Integer manufOrderVersion;

  public ManufOrderProductGetRequest() {}

  public Long getManufOrderId() {
    return manufOrderId;
  }

  public void setManufOrderId(Long manufOrderId) {
    this.manufOrderId = manufOrderId;
  }

  public Integer getManufOrderVersion() {
    return manufOrderVersion;
  }

  public void setManufOrderVersion(Integer manufOrderVersion) {
    this.manufOrderVersion = manufOrderVersion;
  }

  // Transform id to object
  public ManufOrder fetchManufOrder() {
    return ObjectFinder.find(ManufOrder.class, manufOrderId, manufOrderVersion);
  }
}

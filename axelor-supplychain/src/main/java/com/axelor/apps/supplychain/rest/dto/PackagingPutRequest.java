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
package com.axelor.apps.supplychain.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class PackagingPutRequest extends RequestStructure {

  @NotNull
  @Min(0)
  private Long packageUsedId;

  public Long getPackageUsedId() {
    return packageUsedId;
  }

  public void setPackageUsedId(Long packageUsedId) {
    this.packageUsedId = packageUsedId;
  }

  public Product fetchPackageUsed() {
    if (packageUsedId == null || packageUsedId == 0L) {
      return null;
    }
    return ObjectFinder.find(Product.class, packageUsedId, ObjectFinder.NO_VERSION);
  }
}

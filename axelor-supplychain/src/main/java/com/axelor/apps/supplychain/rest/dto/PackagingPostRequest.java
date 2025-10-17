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
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class PackagingPostRequest extends RequestPostStructure {

  @Min(0)
  private Long logisticalFormId;

  @Min(0)
  private Long parentPackagingId;

  @NotNull
  @Min(0)
  private Long packageUsedId;

  public Long getLogisticalFormId() {
    return logisticalFormId;
  }

  public void setLogisticalFormId(Long logisticalFormId) {
    this.logisticalFormId = logisticalFormId;
  }

  public Long getParentPackagingId() {
    return parentPackagingId;
  }

  public void setParentPackagingId(Long parentPackagingId) {
    this.parentPackagingId = parentPackagingId;
  }

  public Long getPackageUsedId() {
    return packageUsedId;
  }

  public void setPackageUsedId(Long packageUsedId) {
    this.packageUsedId = packageUsedId;
  }

  public LogisticalForm fetchLogisticalForm() {
    if (logisticalFormId == null || logisticalFormId == 0L) {
      return null;
    }
    return ObjectFinder.find(LogisticalForm.class, logisticalFormId, ObjectFinder.NO_VERSION);
  }

  public Packaging fetchPackaging() {
    if (parentPackagingId == null || parentPackagingId == 0L) {
      return null;
    }
    return ObjectFinder.find(Packaging.class, parentPackagingId, ObjectFinder.NO_VERSION);
  }

  public Product fetchPackageUsed() {
    if (packageUsedId == null || packageUsedId == 0L) {
      return null;
    }
    return ObjectFinder.find(Product.class, packageUsedId, ObjectFinder.NO_VERSION);
  }
}

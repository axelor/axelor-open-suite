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
package com.axelor.apps.base.rest.dto;

import com.axelor.utils.api.ResponseStructure;

public class MetaPermissionRuleResponse extends ResponseStructure {

  protected final Long id;
  protected final String field;
  protected final boolean canRead;
  protected final boolean canWrite;
  protected final String metaPermissionName;
  protected final String metaPermissionObject;

  public MetaPermissionRuleResponse(
      int version,
      Long id,
      String field,
      boolean canRead,
      boolean canWrite,
      String metaPermissionName,
      String metaPermissionObject) {
    super(version);
    this.id = id;
    this.field = field;
    this.canRead = canRead;
    this.canWrite = canWrite;
    this.metaPermissionName = metaPermissionName;
    this.metaPermissionObject = metaPermissionObject;
  }

  public Long getId() {
    return id;
  }

  public String getField() {
    return field;
  }

  public boolean isCanRead() {
    return canRead;
  }

  public boolean isCanWrite() {
    return canWrite;
  }

  public String getMetaPermissionName() {
    return metaPermissionName;
  }

  public String getMetaPermissionObject() {
    return metaPermissionObject;
  }
}

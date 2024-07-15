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

public class PermissionResponse {

  protected final Long id;
  protected final String name;
  protected final String object;
  protected final boolean canRead;
  protected final boolean canWrite;
  protected final boolean canCreate;
  protected final boolean canRemove;

  public PermissionResponse(
      Long id,
      String name,
      String object,
      boolean canRead,
      boolean canWrite,
      boolean canCreate,
      boolean canRemove) {
    this.id = id;
    this.name = name;
    this.object = object;
    this.canRead = canRead;
    this.canWrite = canWrite;
    this.canCreate = canCreate;
    this.canRemove = canRemove;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getObject() {
    return object;
  }

  public boolean isCanRead() {
    return canRead;
  }

  public boolean isCanWrite() {
    return canWrite;
  }

  public boolean isCanCreate() {
    return canCreate;
  }

  public boolean isCanRemove() {
    return canRemove;
  }
}

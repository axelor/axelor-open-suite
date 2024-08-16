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
package com.axelor.apps.base.service.event;

import java.util.Map;

public class ProductPopulate {

  private final Map<String, Object> json;
  private final Map<String, Object> context;

  public ProductPopulate(Map<String, Object> json, Map<String, Object> context) {
    this.json = json;
    this.context = context;
  }

  public Map<String, Object> getJson() {
    return json;
  }

  public Map<String, Object> getContext() {
    return context;
  }
}

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
package com.axelor.apps.base.service;

import com.axelor.app.AppSettings;

public class SwaggerService {
  public static boolean isSwaggerEnabled() {
    AppSettings appSettings = AppSettings.get();
    String resourcePackages = appSettings.get("aos.swagger.resource-packages");
    boolean resourcesEmpty = resourcePackages == null || resourcePackages.isEmpty();
    return Boolean.parseBoolean(appSettings.get("aos.swagger.enable"))
        && Boolean.parseBoolean(appSettings.get("utils.api.enable"))
        && !resourcesEmpty;
  }
}

/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.app;

import com.axelor.db.Model;
import jakarta.inject.Inject;

public class ScriptAppServiceImpl implements ScriptAppService {

  protected final AppService appService;

  @Inject
  public ScriptAppServiceImpl(AppService appService) {
    this.appService = appService;
  }

  @Override
  public Model getApp(String code) {
    return appService.getApp(code);
  }

  @Override
  public boolean isApp(String code) {
    return appService.isApp(code);
  }
}

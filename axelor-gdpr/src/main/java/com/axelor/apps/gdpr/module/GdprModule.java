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
package com.axelor.apps.gdpr.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.gdpr.service.GdprAnonymizeService;
import com.axelor.apps.gdpr.service.GdprAnonymizeServiceImpl;
import com.axelor.apps.gdpr.service.GdprErasureLogService;
import com.axelor.apps.gdpr.service.GdprErasureLogServiceImpl;
import com.axelor.apps.gdpr.service.GdprSearchEngineService;
import com.axelor.apps.gdpr.service.GdprSearchEngineServiceImpl;
import com.axelor.apps.gdpr.service.app.AppGdprService;
import com.axelor.apps.gdpr.service.app.AppGdprServiceImpl;
import com.axelor.apps.gdpr.service.response.GdprResponseAccessService;
import com.axelor.apps.gdpr.service.response.GdprResponseAccessServiceImpl;
import com.axelor.apps.gdpr.service.response.GdprResponseErasureService;
import com.axelor.apps.gdpr.service.response.GdprResponseErasureServiceImpl;
import com.axelor.apps.gdpr.service.response.GdprResponseService;
import com.axelor.apps.gdpr.service.response.GdprResponseServiceImpl;

public class GdprModule extends AxelorModule {
  @Override
  protected void configure() {
    bind(GdprResponseAccessService.class).to(GdprResponseAccessServiceImpl.class);
    bind(GdprResponseErasureService.class).to(GdprResponseErasureServiceImpl.class);
    bind(GdprErasureLogService.class).to(GdprErasureLogServiceImpl.class);
    bind(GdprResponseService.class).to(GdprResponseServiceImpl.class);
    bind(AppGdprService.class).to(AppGdprServiceImpl.class);
    bind(GdprAnonymizeService.class).to(GdprAnonymizeServiceImpl.class);
    bind(GdprSearchEngineService.class).to(GdprSearchEngineServiceImpl.class);
  }
}

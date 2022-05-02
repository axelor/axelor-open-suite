/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.tool.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.tool.api.HttpExceptionHandler;
import com.axelor.apps.tool.api.HttpExceptionHandlerImpl;
import com.axelor.apps.tool.service.ArchivingToolService;
import com.axelor.apps.tool.service.ArchivingToolServiceImpl;
import com.axelor.apps.tool.service.CipherService;
import com.axelor.apps.tool.service.CipherServiceImpl;
import com.axelor.apps.tool.service.ConvertBinaryToMetafileService;
import com.axelor.apps.tool.service.ConvertBinaryToMetafileServiceImpl;
import com.axelor.apps.tool.service.ListToolService;
import com.axelor.apps.tool.service.ListToolServiceImpl;
import com.axelor.apps.tool.service.TranslationService;
import com.axelor.apps.tool.service.TranslationServiceImpl;
import com.google.inject.matcher.Matchers;

public class ToolModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(CipherService.class).to(CipherServiceImpl.class);
    bind(TranslationService.class).to(TranslationServiceImpl.class);
    bind(ArchivingToolService.class).to(ArchivingToolServiceImpl.class);
    bind(ListToolService.class).to(ListToolServiceImpl.class);
    bind(ConvertBinaryToMetafileService.class).to(ConvertBinaryToMetafileServiceImpl.class);
    bindInterceptor(
        Matchers.any(),
        Matchers.annotatedWith(HttpExceptionHandler.class),
        new HttpExceptionHandlerImpl());
  }
}

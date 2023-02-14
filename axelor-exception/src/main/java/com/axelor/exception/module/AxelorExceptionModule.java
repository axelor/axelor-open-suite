/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.exception.module;

import com.axelor.app.AxelorModule;
import com.axelor.exception.service.HandleExceptionResponse;
import com.axelor.exception.service.HandleExceptionResponseImpl;
import com.google.inject.matcher.Matchers;

public class AxelorExceptionModule extends AxelorModule {

  @Override
  protected void configure() {
    bindInterceptor(
        Matchers.any(),
        Matchers.annotatedWith(HandleExceptionResponse.class),
        new HandleExceptionResponseImpl());
  }
}

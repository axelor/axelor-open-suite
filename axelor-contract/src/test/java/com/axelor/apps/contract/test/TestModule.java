/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.contract.test;

import com.axelor.app.AppModule;
import com.axelor.auth.AuthModule;
import com.axelor.db.JpaModule;
import com.axelor.rpc.ObjectMapperProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;

public class TestModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class);
    install(new JpaModule("testUnit", true, true));
    install(new AuthModule());
    install(new AppModule());
  }
}

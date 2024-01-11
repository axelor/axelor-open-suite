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
package com.axelor.apps.sale.db.repo;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorService;
import com.axelor.inject.Beans;
import javax.persistence.PostPersist;

public class ConfiguratorCreatorListener {

  @PostPersist
  protected void onPostPersist(ConfiguratorCreator creator) {
    try {
      Beans.get(ConfiguratorCreatorService.class).init(creator);
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw e;
    }
  }
}

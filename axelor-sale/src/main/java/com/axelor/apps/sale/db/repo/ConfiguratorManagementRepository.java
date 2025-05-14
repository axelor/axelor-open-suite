/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.sale.db.Configurator;

public class ConfiguratorManagementRepository extends ConfiguratorRepository {

  @Override
  public Configurator copy(Configurator entity, boolean deep) {
    var copy = super.copy(entity, deep);
    copy.setProduct(null);
    return copy;
  }

  @Override
  public Configurator save(Configurator entity) {
    entity = super.save(entity);
    if (entity.getConfiguratorCreator() != null && entity.getConfiguratorVersion() == null) {
      entity.setConfiguratorVersion(entity.getConfiguratorCreator().getConfiguratorVersion());
    }
    return entity;
  }
}

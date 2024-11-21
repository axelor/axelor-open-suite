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
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorImportService;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaJsonField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.PersistenceException;

public class ConfiguratorCreatorSaleRepository extends ConfiguratorCreatorRepository {

  @Override
  public ConfiguratorCreator copy(ConfiguratorCreator entity, boolean deep) {
    ConfiguratorCreator copy = super.copy(entity, deep);
    copy.setCopyNeedingUpdate(true);
    copy.setName(copy.getName() + " (" + I18n.get(SaleExceptionMessage.COPY) + ")");
    addTemporalOnAttributesAndIndicatorsName(copy);
    return copy;
  }

  @Override
  public ConfiguratorCreator save(ConfiguratorCreator entity) {
    try {
      if (entity.getCopyNeedingUpdate()) {
        entity = super.save(entity);
        entity.setCopyNeedingUpdate(false);
        ConfiguratorCreatorService configuratorCreatorService =
            Beans.get(ConfiguratorCreatorService.class);
        Beans.get(ConfiguratorCreatorImportService.class).fixAttributesName(entity);
        configuratorCreatorService.updateAttributes(entity);
        configuratorCreatorService.removeTemporalAttributesAndIndicators(entity);
        configuratorCreatorService.updateIndicators(entity);
        return entity;
      } else {
        return super.save(entity);
      }
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  /**
   * Quick fix for constrainst issue
   *
   * @param entity
   */
  protected void addTemporalOnAttributesAndIndicatorsName(ConfiguratorCreator entity) {
    List<MetaJsonField> metaJsonFields = new ArrayList<>();
    metaJsonFields.addAll(
        Optional.ofNullable(entity.getAttributes()).orElse(Collections.emptyList()));
    metaJsonFields.addAll(
        Optional.ofNullable(entity.getIndicators()).orElse(Collections.emptyList()));
    for (MetaJsonField metaJsonField : metaJsonFields) {
      String name = metaJsonField.getName();
      if (name != null) {
        // FIX FOR CONSTRAINT ISSUE
        metaJsonField.setName(name + "$AXELORTMP");
      }
    }
  }
}

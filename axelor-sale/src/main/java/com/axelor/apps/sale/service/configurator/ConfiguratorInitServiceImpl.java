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
package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.repo.ConfiguratorRepository;
import com.axelor.meta.db.MetaJsonField;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.List;

public class ConfiguratorInitServiceImpl implements ConfiguratorInitService {

  protected ConfiguratorRepository configuratorRepository;

  @Inject
  public ConfiguratorInitServiceImpl(ConfiguratorRepository configuratorRepository) {
    this.configuratorRepository = configuratorRepository;
  }

  @Override
  @Transactional
  public Configurator create(ConfiguratorCreator configuratorCreator) {
    ObjectMapper mapper = new ObjectMapper();

    Configurator configurator = new Configurator();
    configurator.setConfiguratorCreator(configuratorCreator);
    configurator.setConfiguratorCreatorName(configuratorCreator.getName());

    // Generate default attributes JSON
    ObjectNode attributesJson = getDefaultValuesAsJson(configuratorCreator, mapper);
    configurator.setAttributes(attributesJson.toString());

    return configuratorRepository.save(configurator);
  }

  protected ObjectNode getDefaultValuesAsJson(
      ConfiguratorCreator configuratorCreator, ObjectMapper mapper) {
    List<MetaJsonField> attributes = configuratorCreator.getAttributes();
    ObjectNode attributesJson = mapper.createObjectNode();

    for (MetaJsonField attribute : attributes) {
      try {
        attributesJson.put(attribute.getName(), attribute.getDefaultValue());
      } catch (Exception e) {
        // Ignore exceptions
      }
    }
    return attributesJson;
  }
}

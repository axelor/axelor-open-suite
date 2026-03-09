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
package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.common.Inflector;
import com.axelor.common.StringUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.rpc.Context;
import com.axelor.studio.db.AppProject;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetaJsonFieldProjectServiceImpl implements MetaJsonFieldProjectService {

  protected final MetaSelectRepository metaSelectRepository;

  @Inject
  public MetaJsonFieldProjectServiceImpl(MetaSelectRepository metaSelectRepository) {
    this.metaSelectRepository = metaSelectRepository;
  }

  @Override
  public String computeSelectName(MetaJsonField jsonField, String typeSelect) {
    String selection = jsonField.getSelection();
    if (StringUtils.isEmpty(jsonField.getName())
        || !("select".equals(typeSelect) || "multiselect".equals(typeSelect))
        || StringUtils.notEmpty(selection)) {
      return selection;
    }

    Inflector inflector = Inflector.getInstance();
    String model =
        inflector
            .dasherize(jsonField.getModel().substring(jsonField.getModel().lastIndexOf('.') + 1))
            .replace("-", ".");
    String fieldName = inflector.dasherize(jsonField.getName()).replace("-", ".");

    return String.format("project.%s.json.field.%s.type.select", model, fieldName);
  }

  @Override
  public Map<String, Object> computeContextValues(
      Map<String, Object> contextValues, Context parentContext) {
    if (parentContext == null) {
      return contextValues;
    }

    // per project custom field
    String contextField = "";
    String modelField = "attrs";

    if (Project.class.getName().equals(parentContext.get("_model"))) {
      contextField = "project";
      modelField = "projectJson";
    } else if (ProjectTaskCategory.class.getName().equals(parentContext.get("_model"))) {
      contextField = "projectTaskCategory";
      modelField = "categoryJson";
    } else if (AppProject.class.getName().equals(parentContext.get("_model"))) {
      modelField = "appJson";
    }

    if (!StringUtils.isEmpty(contextField)) {
      final Mapper mapper = Mapper.of(ProjectTask.class);
      final Property property = mapper.getProperty(contextField);
      final String target = property == null ? null : property.getTarget().getName();
      final String targetName = property == null ? null : property.getTargetName();

      contextValues.put("contextField", contextField);
      contextValues.put("contextFieldTarget", target);
      contextValues.put("contextFieldTargetName", targetName);
      contextValues.put("contextFieldValue", parentContext.get("id").toString());
      contextValues.put("contextFieldTitle", parentContext.get(targetName).toString());
    }

    contextValues.put("modelField", modelField);

    return contextValues;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void saveSelection(MetaSelect metaSelect) {
    metaSelectRepository.save(metaSelect);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void saveSelectionItems(Long metaSelectId, List<Map<String, Object>> items) {
    MetaSelect select = metaSelectRepository.find(metaSelectId);
    if (select == null) {
      return;
    }

    List<MetaSelectItem> currentItems = new ArrayList<>(select.getItems());
    for (MetaSelectItem item : currentItems) {
      select.removeItem(item);
    }

    if (items != null) {
      int order = 0;
      for (Map<String, Object> itemMap : items) {
        String itemTitle = (String) itemMap.get("title");
        if (StringUtils.isEmpty(itemTitle)) {
          continue;
        }
        MetaSelectItem item = new MetaSelectItem();
        item.setTitle(itemTitle);
        String value = (String) itemMap.get("value");
        item.setValue(StringUtils.notEmpty(value) ? value : itemTitle);
        item.setColor((String) itemMap.get("color"));
        item.setOrder(
            itemMap.get("order") != null ? ((Number) itemMap.get("order")).intValue() : order);
        select.addItem(item);
        order++;
      }
    }

    metaSelectRepository.save(select);
  }
}

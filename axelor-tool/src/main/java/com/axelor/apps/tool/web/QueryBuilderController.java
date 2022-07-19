/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool.web;

import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class QueryBuilderController {

  @Inject MetaFieldRepository metaFieldRepo;

  @SuppressWarnings({"unchecked", "serial"})
  public void getCommonFields(ActionRequest request, ActionResponse response)
      throws ClassNotFoundException {
    if (request.getContext().get("package") == null) {
      return;
    }
    Map<String, Object> meta = Maps.newHashMap();
    List<MetaField> commonFieldList = new ArrayList<>();

    String packageName = request.getContext().get("package").toString();
    packageName = StringUtils.substringBeforeLast(packageName, ".");

    long totalModels =
        Beans.get(MetaModelRepository.class)
            .all()
            .filter("self.packageName = ?", packageName)
            .count();

    List<MetaField> metaFields =
        metaFieldRepo.all().filter("self.metaModel.packageName = ?", packageName).fetch();

    commonFieldList =
        metaFields.stream()
            .collect(Collectors.groupingBy(MetaField::getName))
            .entrySet()
            .stream()
            .filter(e -> e.getValue().size() == totalModels)
            .flatMap(e -> e.getValue().stream())
            .collect(Collectors.toList());

    Map<String, String> checkFieldMap = Maps.newHashMap();
    List<Object> fields = new ArrayList<>();

    for (MetaField metaField : commonFieldList) {
      Class<?> modelClass = Class.forName(metaField.getMetaModel().getFullName());
      Mapper mapper = Mapper.of(modelClass);
      String name = metaField.getName();
      Property property = mapper.getProperty(name);
      Map<String, Object> map = property.toMap();
      if (StringUtils.equals(checkFieldMap.get("name"), name)
          && StringUtils.equals(checkFieldMap.get("type"), map.get("type").toString())) {
        continue;
      }
      checkFieldMap.put("name", name);
      checkFieldMap.put("type", map.get("type").toString());
      map.put("name", name);

      if (property.getSelection() != null && !"".equals(property.getSelection().trim())) {
        map.put("selection", property.getSelection());
        map.put("selectionList", MetaStore.getSelectionList(property.getSelection()));
      }

      if (property.isEnum()) {
        map.put("selectionList", MetaStore.getSelectionList(property.getEnumType()));
      }

      if (property.getTarget() != null) {
        map.put("perms", MetaStore.getPermissions(property.getTarget()));
      }

      if (property.isMassUpdate() && !name.contains(".")) {
        Map<String, Object> perms = (Map<String, Object>) map.get("perms");
        if (perms == null) {
          perms = Maps.newHashMap();
        }
        perms.put("massUpdate", true);
      }

      // find the default value
      if (!property.isTransient() && !property.isVirtual()) {
        Object obj = null;
        try {
          obj = modelClass.newInstance();
        } catch (Exception e) {
        }
        if (obj != null) {
          Object defaultValue = property.get(obj);
          if (defaultValue != null) {
            map.put("defaultValue", defaultValue);
          }
        }
      }
      if (name.contains(".")) {
        map.put("readonly", true);
      }
      fields.add(map);
    }

    meta.putAll(
        new HashMap<String, Object>() {
          {
            put("fields", fields);
          }
        });

    response.setData(meta);
  }
}

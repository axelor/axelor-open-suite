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
package com.axelor.meta.service;

import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MetaServiceBaseImpl implements MetaBaseService {

  protected MetaModelRepository metaModelRepository;

  @Inject
  public MetaServiceBaseImpl(MetaModelRepository metaModelRepository) {
    this.metaModelRepository = metaModelRepository;
  }

  @Override
  public String checkMetaModels() {
    Set<String> modelSet = initModelList();
    return metaModelRepository
        .all()
        .filter("self.fullName NOT IN :modelSet")
        .bind("modelSet", modelSet)
        .fetchStream()
        .map(MetaModel::getId)
        .map(Object::toString)
        .collect(Collectors.joining(","));
  }

  protected Set<String> initModelList() {
    return JPA.models().stream().map(Class::getName).collect(Collectors.toSet());
  }

  @Override
  public String checkMetaFields() {
    List<String> fieldsNotExist = new ArrayList<>();
    List<MetaModel> modelList = metaModelRepository.all().fetch();

    for (MetaModel metaModel : modelList) {
      List<MetaField> metaFieldList = metaModel.getMetaFields();
      List<String> fieldList = initFieldList(metaModel);

      for (MetaField metaField : metaFieldList) {
        if (!fieldList.contains(metaField.getName())) {
          fieldsNotExist.add(metaField.getId().toString());
        }
      }
    }
    return String.join(",", fieldsNotExist);
  }

  @SuppressWarnings("unchecked")
  protected <T extends Model> List<String> initFieldList(MetaModel metaModel) {
    List<String> fieldList = new ArrayList<>();

    Class<T> model = (Class<T>) JPA.model(metaModel.getFullName());
    if (model != null) {
      for (Property property : JPA.fields(model)) {
        fieldList.addAll(Arrays.asList(property.getName()));
      }
    }
    return fieldList;
  }
}

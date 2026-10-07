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
package com.axelor.apps.base.service.filter;

import com.axelor.apps.base.db.Filter;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import jakarta.inject.Inject;

public class FilterTargetServiceImpl implements FilterTargetService {

  protected MetaModelRepository metaModelRepository;
  protected MetaFieldRepository metaFieldRepository;

  @Inject
  public FilterTargetServiceImpl(
      MetaModelRepository metaModelRepository, MetaFieldRepository metaFieldRepository) {
    this.metaModelRepository = metaModelRepository;
    this.metaFieldRepository = metaFieldRepository;
  }

  @Override
  public String getTargetType(Filter filter) {
    MetaField field = filter.getMetaField();
    String targetField = filter.getTargetField();
    if (field == null || targetField == null) {
      return null;
    }

    String[] path = targetField.split("\\.");
    for (int i = 1; i < path.length && field.getRelationship() != null; i++) {
      MetaModel model = metaModelRepository.findByName(field.getTypeName());
      if (model == null) {
        return null;
      }
      field =
          metaFieldRepository
              .all()
              .filter("self.name = :name AND self.metaModel = :metaModel")
              .bind("name", path[i])
              .bind("metaModel", model)
              .fetchOne();
      if (field == null) {
        return null;
      }
    }

    return field.getRelationship() != null ? field.getRelationship() : field.getTypeName();
  }
}
